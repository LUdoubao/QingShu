package org.doubao.topic.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.topic.service.entity.TopicStatistics;

import java.util.List;
import java.util.Set;

@Mapper
public interface TopicStatisticsMapper extends BaseMapper<TopicStatistics> {
	void incrementQuoteCount(@Param("topicId") Long topicId);

	void decrementQuoteCount(@Param("topicId") Long topicId);

	void decrementQuoteCountBatch(@Param("topicIds") List<Long> topicIds);
}