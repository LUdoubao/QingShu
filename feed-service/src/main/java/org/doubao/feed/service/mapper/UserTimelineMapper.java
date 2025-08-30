package org.doubao.feed.service.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.feed.service.model.entity.UserTimeline;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserTimelineMapper extends BaseMapper<UserTimeline> {

	/**
	 * 分页查询用户时间线
	 */
	List<UserTimeline> queryUserTimeline(@Param("userId")Long userId, @Param("actorIds") List<Long> actorIds,
										  @Param("pageNum") int pageNum, @Param("pageSize")	int pageSize);

	/**
	 * 统计用户时间线总数
	 */
	long countUserTimeline(@Param("userId") Long userId,@Param("actorIds") List<Long> actorIds);

	/**
	 * 统计未读动态数量
	 */
	long countUnreadTimeline(Long userId, List<Long> actorIds, LocalDateTime lastReadTime);

}
