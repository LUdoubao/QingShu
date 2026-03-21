package org.doubao.recommend.service.job;

import org.doubao.recommend.service.common.RedisKeys;
import org.doubao.recommend.service.domain.RecommendItem;
import org.doubao.recommend.service.domain.RecommendRequest;
import org.doubao.recommend.service.mapper.UserBehaviorMapper;
import org.doubao.recommend.service.service.RecommendService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class RecommendPrecomputeJob {

    /**
     * 用户行为 Mapper
     * 用于查询活跃用户列表
     */
    @Resource
    private UserBehaviorMapper behaviorMapper;
    
    /**
     * 推荐服务
     * 用于执行推荐算法，生成推荐结果
     */
    @Resource
    private RecommendService recommendService;
    
    /**
     * Redis 模板
     * 用于将预计算的推荐结果缓存到 Redis
     */
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 为活跃用户预计算首页推荐结果
     * 每小时第 40 分钟执行一次（cron: 0 40 * * * ?）
     * 获取最近 7 天内最活跃的 1000 个用户，为他们预先计算首页推荐结果并缓存
     * 这样当用户访问时可以直接从缓存中快速获取推荐结果，提升响应速度
     */
    // @Scheduled(cron = "0 40 * * * ?")
    public void precomputeHomeForActiveUsers() {
        // 查询最近 7 天内最活跃的 1000 个用户
        List<String> activeUsers = behaviorMapper.selectActiveUsers(LocalDateTime.now().minusDays(7), 1000);
        
        // 为每个活跃用户预计算首页推荐
        for (String userIdentity : activeUsers) {
            // 构建推荐请求参数
            RecommendRequest request = new RecommendRequest();
            request.setUserIdentity(userIdentity);
            request.setScene("home");
            request.setPage(1);
            request.setPageSize(20);
            
            // 调用推荐服务生成推荐结果
            List<RecommendItem> items = recommendService.recommend(request);
            
            // 将推荐结果缓存到 Redis
            redisTemplate.opsForValue().set(RedisKeys.PRECOMPUTED_HOME_PREFIX + userIdentity, items);
        }
    }
}
