package org.doubao.topic.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.doubao.topic.service.entity.Topic;
@Mapper
public interface TopicMapper extends BaseMapper<Topic> {
}