package org.doubao.user.service.mapper.log;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.user.service.entity.log.OperationLog;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作日志Mapper
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {

	/**
	 * 批量插入日志
	 */
	int batchInsert(@Param("logs") List<OperationLog> logs);


	int deleteBeforeTime(@Param("time") LocalDateTime time);
}
