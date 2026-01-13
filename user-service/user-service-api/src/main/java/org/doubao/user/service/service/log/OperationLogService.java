package org.doubao.user.service.service.log;

import com.baomidou.mybatisplus.extension.service.IService;
import org.doubao.user.service.entity.log.OperationLog;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作日志Service接口
 */
public interface OperationLogService extends IService<OperationLog> {

	/**
	 * 记录操作日志
	 */
	boolean recordLog(OperationLog log);

	/**
	 * 批量记录操作日志
	 */
	boolean batchRecordLogs(List<OperationLog> logs);

	/**
	 * 清理指定时间之前的日志
	 */
	int cleanLogsBeforeTime(LocalDateTime time);

	/**
	 * 按条件查询日志
	 */
	List<OperationLog> queryLogs(String username, String operationType,
								 LocalDateTime startTime, LocalDateTime endTime,
								 Integer pageNum, Integer pageSize);
}
