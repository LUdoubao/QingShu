package org.doubao.user.server.log.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.user.server.log.entity.OperationLog;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作日志Mapper
 */
public interface OperationLogMapper extends BaseMapper<OperationLog> {

	/**
	 * 批量插入日志
	 */
	int batchInsert(@Param("logs") List<OperationLog> logs);

	/**
	 * 删除指定时间之前的日志
	 */
	int deleteBeforeTime(@Param("time") LocalDateTime time);
}