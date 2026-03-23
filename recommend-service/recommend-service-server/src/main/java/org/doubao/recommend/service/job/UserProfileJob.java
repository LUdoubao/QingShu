package org.doubao.recommend.service.job;

import org.doubao.mall.common.util.DoubaoUtils;
import org.doubao.recommend.service.common.RedisKeys;
import org.doubao.recommend.service.domain.BehaviorEvent;
import org.doubao.recommend.service.domain.ContentFeature;
import org.doubao.recommend.service.domain.UserProfile;
import org.doubao.recommend.service.mapper.UserBehaviorMapper;
import org.doubao.recommend.service.service.ContentFeatureService;
import org.doubao.recommend.service.service.UserProfileService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

@Component
public class UserProfileJob {

    /**
     * Redis 模板
     * 用于查询活跃用户列表和更新用户画像数据
     */
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 用户行为 Mapper
     * 用于从数据库查询用户行为记录
     */
    @Resource
    private UserBehaviorMapper userBehaviorMapper;

    /**
     * 用户画像服务
     * 用于加载和持久化用户画像
     */
    @Resource
    private UserProfileService userProfileService;

    /**
     * 内容特征服务
     * 用于获取内容的特征信息，支持画像更新
     */
    @Resource
    private ContentFeatureService contentFeatureService;

    /**
     * 刷新活跃用户的画像数据
     * 每小时第 20 分钟执行一次（cron: 0 20 * * * ?）
     * 从 Redis 活跃用户有序集合中获取 Top 1000 的活跃用户
     * 更新他们的最后活跃时间到当前时间，并持久化到 Redis
     */
    @Scheduled(cron = "0 20 * * * ?")
    public void flushActiveProfiles() {
        // 从 Redis 有序集合中按活跃度倒序获取前 1000 个活跃用户
        Set<Object> active = redisTemplate.opsForZSet().reverseRangeByScore(RedisKeys.USER_ACTIVE_ZSET, Double.MAX_VALUE, 0, 0, 1000);
        if (active == null || active.isEmpty()) {
            return;
        }
        
        // 遍历每个活跃用户，更新其画像的最后活跃时间
        for (Object userIdentityObj : active) {
            String userIdentity = String.valueOf(userIdentityObj);
            
            // 从 Redis 获取用户画像
            Object profileObj = redisTemplate.opsForValue().get(RedisKeys.PROFILE_PREFIX + userIdentity);
            if (DoubaoUtils.isNotEmpty(profileObj) && profileObj instanceof UserProfile) {
                UserProfile profile = (UserProfile) profileObj;
                
                // 更新最后活跃时间为当前时间
                profile.setLastActiveTime(LocalDateTime.now());
                
                // 根据用户行为记录重新计算用户偏好
                calculateUserPreferences(profile);
                
                // 更新画像更新时间
                profile.setUpdatedTime(LocalDateTime.now());
                
                // 将更新后的画像写回 Redis 和数据库
                userProfileService.persistProfile(profile);
            }
        }
    }

    /**
     * 根据用户行为记录计算用户偏好
     * 从数据库查询用户最近的行为记录，基于行为类型和内容特征更新用户画像
     *
     * @param profile 用户画像
     */
    private void calculateUserPreferences(UserProfile profile) {
        String userIdentity = profile.getUserIdentity();
        
        // 查询用户最近一周的行为记录
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        List<BehaviorEvent> recentBehaviors = userBehaviorMapper.selectRecentBehaviors(userIdentity, oneWeekAgo);
        
        if (recentBehaviors == null || recentBehaviors.isEmpty()) {
            return;
        }

        // 不再清空当前权重，而是直接累加新权重到现有权重上
        
        // 遍历行为记录，计算用户偏好并累加到现有权重
        for (BehaviorEvent event : recentBehaviors) {
            // 获取内容的特征信息
            ContentFeature feature = contentFeatureService.getById(event.getContentId());
            if (feature == null) {
                continue;
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
            double weight = actionWeight * (1.0 - event.getCreatedTime().until(LocalDateTime.now(), ChronoUnit.HOURS) / (7.0 * 24)) *
                           (event.getDuration() >= 10 ? 1.2 : 1.0);

            // 更新标签权重
            for (String tag : feature.tagList()) {
                profile.addTag(tag, weight);
            }
            
            // 更新话题权重
            for (Long topicId : feature.topicIdList()) {
                profile.addTopic(String.valueOf(topicId), weight);
            }
            
            // 更新作者权重
            if (feature.getAuthor() != null && !feature.getAuthor().isEmpty() && !"佚名".equals(feature.getAuthor())) {
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
        }
    }
}