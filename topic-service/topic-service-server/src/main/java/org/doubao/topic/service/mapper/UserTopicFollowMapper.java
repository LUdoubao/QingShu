package org.doubao.topic.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.topic.service.entity.UserTopicFollow;
import org.doubao.topic.service.vo.TopicFollowVo;

import java.util.List;

@Mapper
public interface UserTopicFollowMapper extends BaseMapper<UserTopicFollow> {
	List<TopicFollowVo> isUserFollowingTopic(@Param("userId") Long userId, @Param("topicIds") List<Long> topicIds);
}