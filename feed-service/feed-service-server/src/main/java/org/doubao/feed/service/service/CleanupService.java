package org.doubao.feed.service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 清理服务 - 定时清理无效数据
 */
@Service
public class CleanupService {

    private static final Logger logger = LoggerFactory.getLogger(CleanupService.class);

    @Resource
    private UserTimelineService userTimelineService;

    /**
     * 每10分钟执行一次清理任务
     * 清理user_timeline表中is_valid为0的数据
     */
    @Scheduled(fixedRate = 600000) // 10分钟 = 600000毫秒
    public void cleanupInvalidTimelines() {
        logger.info("开始执行清理无效动态数据定时任务");
        try {
            int deletedCount = userTimelineService.cleanupInvalidTimelines();
            logger.info("清理无效动态数据定时任务执行完成，删除记录数：{}", deletedCount);
        } catch (Exception e) {
            logger.error("清理无效动态数据定时任务执行失败", e);
        }
    }
}