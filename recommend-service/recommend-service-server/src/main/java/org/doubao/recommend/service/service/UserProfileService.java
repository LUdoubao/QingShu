package org.doubao.recommend.service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.doubao.recommend.service.common.RedisKeys;
import org.doubao.recommend.service.domain.BehaviorEvent;
import org.doubao.recommend.service.domain.ContentFeature;
import org.doubao.recommend.service.domain.UserProfile;
import org.doubao.recommend.service.domain.UserProfileSnapshotRow;
import org.doubao.recommend.service.mapper.UserProfileSnapshotMapper;
import org.doubao.recommend.service.util.ScoreUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserProfileService {

    /**
     * Redis 模板
     * 用于缓存用户画像数据，提高查询性能
     */
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 用户画像快照 Mapper
     * 用于持久化存储用户画像到数据库
     */
    @Autowired
    private UserProfileSnapshotMapper snapshotMapper;
    
    /**
     * 内容特征服务
     * 用于获取内容的特征信息，支持画像更新
     */
    @Autowired
    private ContentFeatureService contentFeatureService;
    
    /**
     * JSON 对象映射器
     * 用于序列化和反序列化用户画像中的 Map 和 List 字段
     */
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    /**
     * 获取用户画像
     * 采用缓存优先策略：先查 Redis，未命中则查数据库并回写缓存
     *
     * @param userIdentity 用户身份标识
     * @return 用户画像对象，如果不存在则返回 null
     */
    public UserProfile loadProfile(String userIdentity) {
        if (userIdentity == null || userIdentity.isEmpty()) {
            return null;
        }
        // 从 Redis 缓存中查询
        Object cached = redisTemplate.opsForValue().get(RedisKeys.PROFILE_PREFIX + userIdentity);
        if (cached instanceof UserProfile) {
            return (UserProfile) cached;
        }

        // 缓存未命中，从数据库查询快照
        UserProfileSnapshotRow row = snapshotMapper.selectByUserIdentity(userIdentity);
        if (row == null) {
            return null;
        }

        // 将数据库行转换为 UserProfile 对象
        UserProfile profile = new UserProfile();
        profile.setUserIdentity(row.getUserIdentity());
        profile.setUserId(row.getUserId());
        profile.setTagWeights(readMap(row.getTagWeights()));
        profile.setTopicWeights(readMap(row.getTopicWeights()));
        profile.setAuthorWeights(readMap(row.getAuthorWeights()));
        profile.setDynastyWeights(readMap(row.getDynastyWeights()));
        profile.setCategoryWeights(readMap(row.getCategoryWeights()));
        profile.setRecentContentIds(readList(row.getRecentContentIds()));
        profile.setLastActiveTime(row.getLastActiveTime());
        profile.setUpdatedTime(row.getUpdatedTime());

        // 写入 Redis 缓存
        redisTemplate.opsForValue().set(RedisKeys.PROFILE_PREFIX + userIdentity, profile);
        return profile;
    }

    /**
     * 根据用户行为事件更新用户画像
     * 基于用户对内容的操作（浏览、点赞、收藏等）调整用户在各维度的兴趣权重
     * 权重计算考虑因素：行为类型、时间衰减、停留时长
     *
     * @param event 用户行为事件
     */
    public void updateProfile(BehaviorEvent event) {
        // 参数校验
        if (event == null || event.getUserIdentity() == null || event.getUserIdentity().isEmpty() || event.getContentId() == null) {
            return;
        }
        
        // 获取或创建用户画像
        UserProfile profile = getOrCreate(event.getUserIdentity(), event.getUserId());
        
        // 获取内容的特征信息
        ContentFeature feature = contentFeatureService.getById(event.getContentId());
        if (feature == null) {
            return;
        }

        // 根据行为类型确定基础权重
        double actionWeight;
        switch (event.getActionType()) {
            case "view":      // 浏览：基础权重 1.0
                actionWeight = 1.0;
                break;
            case "like":      // 点赞：基础权重 3.0
                actionWeight = 3.0;
                break;
            case "favorite":  // 收藏：基础权重 5.0
                actionWeight = 5.0;
                break;
            case "comment":   // 评论：基础权重 6.0
                actionWeight = 6.0;
                break;
            case "share":     // 分享：基础权重 8.0
                actionWeight = 8.0;
                break;
            default:
                actionWeight = 1.0;
        }
        
        // 计算最终权重：基础权重 × 时间衰减因子 × 停留时长系数
        double weight = actionWeight * ScoreUtils.timeDecay(event.getCreatedTime()) * (event.getDuration() >= 10 ? 1.2 : 1.0);

        // 更新标签权重
        for (String tag : feature.tagList()) {
            profile.addTag(tag, weight);
        }
        
        // 更新话题权重
        for (Long topicId : feature.topicIdList()) {
            profile.addTopic(String.valueOf(topicId), weight);
        }
        
        // 更新作者权重：降低作者维度的放大效应，同时跳过“佚名”这类弱标识作者
        if (feature.getAuthor() != null && !feature.getAuthor().isEmpty() && !Objects.equals(feature.getAuthor(), "佚名")) {
            profile.addAuthor(feature.getAuthor(), weight * 0.7);
        }
        
        // 更新朝代权重
        if (feature.getDynasty() != null && !feature.getDynasty().isEmpty()) {
            profile.addDynasty(feature.getDynasty(), weight);
        }
        
        // 更新分类权重
        if (feature.getPoetryCategory() != null && !feature.getPoetryCategory().isEmpty()) {
            profile.addCategory(feature.getPoetryCategory(), weight);
        }
        
        // 更新最近浏览记录
        profile.pushRecentContent(feature.getContentId(), 50);
        
        // 更新活跃时间和画像更新时间
        profile.setLastActiveTime(LocalDateTime.now());
        profile.setUpdatedTime(LocalDateTime.now());

        // 持久化到数据库和 Redis
        persist(profile);
    }

    /**
     * 获取用户最近的內容 ID 列表
     * 限制返回数量，用于实时推荐场景
     *
     * @param userIdentity 用户身份标识
     * @param limit 最大返回数量
     * @return 最近的内容 ID 列表
     */
    public UserProfile getProfile(String userIdentity) {
        return loadProfile(userIdentity);
    }

    public void persistProfile(UserProfile profile) {
        if (profile == null) {
            return;
        }
        persist(profile);
    }

    public List<Long> getRecentContentIds(String userIdentity, int limit) {
        UserProfile profile = loadProfile(userIdentity);
        if (profile == null || profile.getRecentContentIds() == null) {
            return new ArrayList<>();
        }
        return profile.getRecentContentIds().stream().limit(limit).collect(Collectors.toList());
    }

    /**
     * 获取或创建用户画像
     * 如果用户画像不存在，则创建一个新的空画像
     *
     * @param userIdentity 用户身份标识
     * @param userId 用户 ID
     * @return 用户画像对象
     */
    private UserProfile getOrCreate(String userIdentity, Long userId) {
        UserProfile profile = loadProfile(userIdentity);
        if (profile == null) {
            profile = new UserProfile();
            profile.setUserIdentity(userIdentity);
            profile.setUserId(userId);
        }
        return profile;
    }

    /**
     * 从 JSON 字符串读取 Map<String, Double>
     * 用于从数据库快照中恢复用户画像的权重字段
     *
     * @param json JSON 字符串
     * @return Map<String, Double>，如果解析失败则返回空 Map
     */
    private Map<String, Double> readMap(String json) {
        try {
            if (json == null || json.isEmpty()) {
                return new HashMap<>();
            }
            return objectMapper.readValue(json, new TypeReference<Map<String, Double>>() {});
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    /**
     * 从 JSON 字符串读取 List<Long>
     * 用于从数据库快照中恢复用户画像的最近浏览记录
     *
     * @param json JSON 字符串
     * @return List<Long>，如果解析失败则返回空 List
     */
    private List<Long> readList(String json) {
        try {
            if (json == null || json.isEmpty()) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(json, new TypeReference<List<Long>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * 持久化用户画像到数据库和 Redis
     * 将用户画像的各维度权重序列化为 JSON 字符串存储到数据库
     * 同时更新 Redis 缓存和活跃用户有序集合
     *
     * @param profile 用户画像对象
     */
    private void persist(UserProfile profile) {
        try {
            // 序列化各维度权重为 JSON 字符串
            String tagProfile = objectMapper.writeValueAsString(profile.getTagWeights());
            String topicProfile = objectMapper.writeValueAsString(profile.getTopicWeights());
            String authorProfile = objectMapper.writeValueAsString(profile.getAuthorWeights());
            String dynastyProfile = objectMapper.writeValueAsString(profile.getDynastyWeights());
            String categoryProfile = objectMapper.writeValueAsString(profile.getCategoryWeights());
            String recentContentIds = objectMapper.writeValueAsString(profile.getRecentContentIds());

            // 插入或更新到数据库
            snapshotMapper.upsert(
                    profile.getUserIdentity(),
                    profile.getUserId(),
                    tagProfile,
                    topicProfile,
                    authorProfile,
                    dynastyProfile,
                    categoryProfile,
                    recentContentIds,
                    profile.getLastActiveTime()
            );
            
            // 更新 Redis 缓存
            redisTemplate.opsForValue().set(RedisKeys.PROFILE_PREFIX + profile.getUserIdentity(), profile);
            
            // 更新活跃用户有序集合（用于定时任务）
            redisTemplate.opsForZSet().add(RedisKeys.USER_ACTIVE_ZSET, profile.getUserIdentity(), System.currentTimeMillis());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to persist profile", e);
        }
    }

    /**
     * 计算用户对内容的兴趣分数
     * 基于用户画像中的各维度权重与内容特征的匹配程度进行评分
     * 评分权重：标签 45% + 话题 20% + 作者 10% + 朝代 12% + 分类 13%
     * 最终使用 tanh 函数将分数压缩到 [0, 1] 区间
     *
     * @param profile 用户画像
     * @param feature 内容特征
     * @return 兴趣分数（范围：0.0 ~ 1.0）
     */
    public double interestScore(UserProfile profile, ContentFeature feature) {
        if (profile == null || feature == null) {
            return 0.0;
        }
        double score = 0.0;
        
        // 标签匹配得分
        for (String tag : feature.tagList()) {
            score += profile.getTagWeights().getOrDefault(tag, 0.0) * 0.45;
        }
        
        // 话题匹配得分
        for (Long topicId : feature.topicIdList()) {
            score += profile.getTopicWeights().getOrDefault(String.valueOf(topicId), 0.0) * 0.20;
        }
        
        // 作者匹配得分
        score += profile.getAuthorWeights().getOrDefault(feature.getAuthor(), 0.0) * 0.10;
        
        // 朝代匹配得分
        score += profile.getDynastyWeights().getOrDefault(feature.getDynasty(), 0.0) * 0.12;
        
        // 分类匹配得分
        score += profile.getCategoryWeights().getOrDefault(feature.getPoetryCategory(), 0.0) * 0.13;
        
        // 使用 tanh 函数归一化到 [0, 1]
        return Math.tanh(score / 50.0);
    }
}
