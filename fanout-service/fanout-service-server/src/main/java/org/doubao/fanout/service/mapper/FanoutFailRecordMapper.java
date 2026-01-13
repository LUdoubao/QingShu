package org.doubao.fanout.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.fanout.service.model.FanoutFailRecord;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface FanoutFailRecordMapper extends BaseMapper<FanoutFailRecord> {
	/**
	 * 查询需要重试的失败记录
	 */
	List<FanoutFailRecord> findByRetryCountLessThanAndLastRetryTimeBeforeOrLastRetryTimeIsNull(
			@Param("maxRetryCount") int maxRetryCount, @Param("timeThreshold") LocalDateTime timeThreshold);

	/**
	 * 根据事件ID查询记录
	 */
	List<FanoutFailRecord> findByEventId(@Param("eventId") String eventId);

	List<FanoutFailRecord> findAll();
}
