package org.doubao.topic.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.doubao.topic.service.entity.Topic;
import org.doubao.topic.service.vo.TopicSelectVo;

import java.util.List;

@Mapper
public interface TopicMapper extends BaseMapper<Topic> {
	List<TopicSelectVo> selectTopTopics(int i);
}