package org.doubao.view.count.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.view.count.service.entity.UserViewLog;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface UserViewLogMapper extends BaseMapper<UserViewLog> {
	/**
	 * 查询指定文章在多个日期的每日有效浏览量总和
	 * @param contentIds 文章ID列表
	 * @param dates 日期列表
	 */
	List<Map<String, Object>> selectDailyViewCounts(@Param("contentIds") List<Long> contentIds,
													@Param("dates") List<LocalDate> dates);
}