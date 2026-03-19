package org.doubao.recommend.service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.doubao.recommend.service.common.RedisKeys;
import org.doubao.recommend.service.domain.BehaviorEvent;
import org.doubao.recommend.service.mapper.UserBehaviorMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
public class BehaviorService {

    /**
     * 用户行为 Mapper
     * 用于将用户行为事件持久化到数据库
     */
    @Resource
    private UserBehaviorMapper behaviorMapper;
    
    /**
     * 用户画像服务
     * 用于根据用户行为更新用户兴趣画像
     */
    @Resource
    private UserProfileService userProfileService;
    
    /**
     * Redis String 模板
     * 用于记录用户已读内容集合，支持过滤已读功能
     */
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * JSON 对象映射器
     * 用于将额外信息 Map 序列化为 JSON 字符串存储
     */
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    /**
     * 记录用户行为事件
     * 完整的处理流程：数据校验 -> 持久化到数据库 -> 更新 Redis 已读集合 -> 更新用户画像
     *
     * @param event 用户行为事件对象
     * @throws RuntimeException 如果记录过程失败
     */
    public void record(BehaviorEvent event) {
        // 设置默认创建时间（如果为空）
        if (event.getCreatedTime() == null) {
            event.setCreatedTime(LocalDateTime.now());
        }
        // 设置默认行为数值（如果为空）
        if (event.getActionValue() == null) {
            event.setActionValue(1);
        }
        // 设置默认持续时间（如果为空）
        if (event.getDuration() == null) {
            event.setDuration(0);
        }
        try {
            // 将额外信息 Map 序列化为 JSON 字符串
            String extraJson = event.getExtra() == null ? null : objectMapper.writeValueAsString(event.getExtra());
            
            // 插入行为记录到数据库
            behaviorMapper.insert(
                    event.getUserIdentity(),
                    event.getUserId(),
                    event.getContentId(),
                    event.getScene(),
                    event.getActionType(),
                    event.getActionValue(),
                    event.getDuration(),
                    extraJson,
                    event.getCreatedTime()
            );

            // 如果是浏览行为，需要更新用户已读集合
            if ("view".equalsIgnoreCase(event.getActionType())) {
                // 将内容 ID 添加到用户的已读集合中
                stringRedisTemplate.opsForSet().add(RedisKeys.SEEN_PREFIX + event.getUserIdentity(), String.valueOf(event.getContentId()));
                // 设置已读集合的过期时间为 30 天
                stringRedisTemplate.expire(RedisKeys.SEEN_PREFIX + event.getUserIdentity(), java.time.Duration.ofDays(30));
            }

            // 更新用户画像（基于用户行为调整兴趣权重）
            userProfileService.updateProfile(event);
        } catch (Exception e) {
            throw new RuntimeException("record behavior failed", e);
        }
    }
}
