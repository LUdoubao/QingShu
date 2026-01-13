package org.doubao.user.service.service.impl.log;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.doubao.user.service.entity.log.OperationLog;
import org.doubao.user.service.mapper.log.OperationLogMapper;
import org.doubao.user.service.service.log.OperationLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 操作日志Service实现
 */
@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog>
		implements OperationLogService {

	private static final Logger LOGGER = LoggerFactory.getLogger(OperationLogServiceImpl.class);
	@Autowired
	private OperationLogMapper operationLogMapper;

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	// Redis缓存前缀
	private static final String LOG_CACHE_PREFIX = "log:cache:";
	// 缓存过期时间(小时)
	private static final long CACHE_EXPIRE_HOURS = 24;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean recordLog(OperationLog log) {
		try {

			LocalDateTime now = LocalDateTime.now();
			log.setOperationTime(now);
			log.setCreateTime(now);
			log.setUpdateTime(now);

			// 保存到数据库
			boolean result = save(log);

			// 热门操作日志缓存到Redis
			if (result && isHotOperation(log.getOperationType())) {
				String cacheKey = LOG_CACHE_PREFIX + log.getId();
				redisTemplate.opsForValue().set(cacheKey, log, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
			}

			return result;
		} catch (Exception e) {
			LOGGER.error("记录操作日志失败", e);
			return false;
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean batchRecordLogs(List<OperationLog> logs) {
		if (logs == null || logs.isEmpty()) {
			return true;
		}

		LocalDateTime now = LocalDateTime.now();
		logs.forEach(log -> {
			log.setOperationTime(now);
			log.setCreateTime(now);
			log.setUpdateTime(now);
		});

		try {
			return operationLogMapper.batchInsert(logs) > 0;
		} catch (Exception e) {
			LOGGER.error("批量记录操作日志失败", e);
			return false;
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int cleanLogsBeforeTime(LocalDateTime time) {
		try {
			// 删除数据库中的记�?
			int deleteCount = operationLogMapper.deleteBeforeTime(time);
			LOGGER.info("清理{}之前的日志，共删除{}条记", time, deleteCount);

			// 清理相关缓存
			// 实际应用中可能需要更复杂的缓存清理策�?
			return deleteCount;
		} catch (Exception e) {
			LOGGER.error("清理日志失败", e);
			return 0;
		}
	}

	@Override
	public List<OperationLog> queryLogs(String username, String operationType,
										LocalDateTime startTime, LocalDateTime endTime,
										Integer pageNum, Integer pageSize) {
		Page<OperationLog> page = new Page<>(pageNum, pageSize);
		QueryWrapper<OperationLog> queryWrapper = new QueryWrapper<>();

		if (username != null && !username.isEmpty()) {
			queryWrapper.eq("username", username);
		}
		if (operationType != null && !operationType.isEmpty()) {
			queryWrapper.eq("operation_type", operationType);
		}
		if (startTime != null) {
			queryWrapper.ge("operation_time", startTime);
		}
		if (endTime != null) {
			queryWrapper.le("operation_time", endTime);
		}

		queryWrapper.eq("deleted", 0)
				.orderByDesc("operation_time");

		return page(page, queryWrapper).getRecords();
	}

	/**
	 * 判断是否为热门操作类�?
	 */
	private boolean isHotOperation(String operationType) {
		// 这里可以根据实际业务定义哪些操作属于热门操作
		return "LOGIN".equals(operationType) || "CREATE".equals(operationType);
	}
}
