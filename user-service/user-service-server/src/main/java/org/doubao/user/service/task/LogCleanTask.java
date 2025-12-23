package org.doubao.user.service.task;

import org.doubao.user.service.service.log.OperationLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 日志清理定时任务
 */
@Component
public class LogCleanTask {

	private static final Logger log = LoggerFactory.getLogger(LogCleanTask.class);
	@Autowired
	private OperationLogService operationLogService;

	// 保留日志的天数
	private static final int RETAIN_DAYS = 180;

	/**
	 * 每天凌晨2点执行日志清理
	 * 清理180天前的日志
	 */
	@Scheduled(cron = "0 0 2 * * ?")
	public void cleanExpiredLogs() {
		log.info("开始执行日志清理任务...");

		// 计算180天前的时间
		LocalDateTime expireTime = LocalDateTime.now().minusDays(RETAIN_DAYS);

		// 执行清理
		int deleteCount = operationLogService.cleanLogsBeforeTime(expireTime);

		log.info("日志清理任务执行完成，共清理{}条过期日志", deleteCount);
	}
}