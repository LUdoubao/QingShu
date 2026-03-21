package org.doubao.recommend.service.job;

import org.doubao.recommend.service.common.RedisKeys;
import org.doubao.recommend.service.domain.UserProfile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
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
     * 刷新活跃用户的画像数据
     * 每小时第 20 分钟执行一次（cron: 0 20 * * * ?）
     * 从 Redis 活跃用户有序集合中获取 Top 1000 的活跃用户
     * 更新他们的最后活跃时间到当前时间，并持久化到 Redis
     */
    // @Scheduled(cron = "0 20 * * * ?")
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
            if (profileObj instanceof UserProfile) {
                UserProfile profile = (UserProfile) profileObj;
                
                // 更新最后活跃时间为当前时间
                profile.setLastActiveTime(LocalDateTime.now());
                
                // 将更新后的画像写回 Redis
                redisTemplate.opsForValue().set(RedisKeys.PROFILE_PREFIX + userIdentity, profile);
            }
        }
    }
}
