package org.doubao.feed.service.mapper;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.feed.service.model.entity.EventTimeline;

import java.util.List;

@Mapper
public interface EventTimelineMapper extends BaseMapper<EventTimeline> {

	/**
	 * 分页查询大V事件
	 */
	List<EventTimeline> queryVipTimeline(@Param("actorIds") List<Long> actorIds,
										  @Param("pageNum") int pageNum, @Param("pageSize") int pageSize);

	/**
	 * 统计大V事件总数
	 */
	long countVipTimeline(@Param("actorIds") List<Long> actorIds);

	/**
	 * 批量插入大V事件
	 */
	int batchInsert(@Param("list") List<EventTimeline> list);
}
