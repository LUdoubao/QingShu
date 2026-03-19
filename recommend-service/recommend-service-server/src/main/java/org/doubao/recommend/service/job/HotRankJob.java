package org.doubao.recommend.service.job;

import org.doubao.recommend.service.common.RedisKeys;
import org.doubao.recommend.service.domain.HotStat;
import org.doubao.recommend.service.mapper.UserBehaviorMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 热度排行定时任务类
 * 负责定期计算和更新热门内容的排行榜数据
 * 使用 Spring 的@Scheduled 注解实现定时调度
 */
@Component
public class HotRankJob {

    /**
     * 用户行为 Mapper
     * 用于查询用户行为统计数据，计算内容热度分数
     */
    @Resource
    private UserBehaviorMapper behaviorMapper;
    
    /**
     * Redis String 模板
     * 用于将计算好的热度排行榜写入 Redis 有序集合
     */
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 重建首页热门内容排行榜
     * 每小时的第 10 分钟执行一次（cron: 0 10 * * * ?）
     * 从数据库中统计最近 7 天内热度最高的 500 个内容，并写入 Redis 有序集合
     */
    @Scheduled(cron = "0 10 * * * ?")
    public void rebuildHotHome() {
        // 查询最近 7 天内的热门内容统计，最多返回 500 条
        List<HotStat> stats = behaviorMapper.selectHotStats(LocalDateTime.now().minusDays(7), 500);
        
        // 删除旧的排行榜数据
        stringRedisTemplate.delete(RedisKeys.HOT_HOME_ZSET);
        
        // 将新的热度数据逐个添加到 Redis 有序集合中
        for (HotStat stat : stats) {
            stringRedisTemplate.opsForZSet().add(RedisKeys.HOT_HOME_ZSET, String.valueOf(stat.getContentId()), stat.getScore());
        }
    }
}
