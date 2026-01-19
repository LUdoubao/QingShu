package org.doubao.topic.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.doubao.mall.common.entity.Result;
import org.doubao.topic.service.entity.UserTopicFollow;
import org.doubao.topic.service.vo.TopicFollowVo;

import java.util.List;

/**
 * 用户话题关注服务接口
 * 提供用户关注话题、取消关注、查询关注状态等功能
 */
public interface UserTopicFollowService extends IService<UserTopicFollow> {
    /**
     * 关注话题
     * 创建用户与话题的关注关系
     *
     * @param userId 用户ID
     * @param topicId 话题ID
     * @return 操作结果
     */
    Result<Boolean> followTopic(Long userId, Long topicId);

    /**
     * 取消关注话题
     * 移除用户与话题的关注关系
     *
     * @param userId 用户ID
     * @param topicId 话题ID
     * @return 操作结果
     */
    Result<Boolean> unfollowTopic(Long userId, Long topicId);

    /**
     * 检查用户是否关注了话题
     * 判断用户与话题之间是否存在有效的关注关系
     *
     * @param userId 用户ID
     * @param topicIds 话题 ID集合
     * @return 是否关注
     */
    Result<List<TopicFollowVo>> isUserFollowingTopic(Long userId, List<Long> topicIds);

    /**
     * 获取用户关注的话题数量
     * 统计指定用户关注的话题总数
     *
     * @param userId 用户ID
     * @return 关注话题数量
     */
    Result<Integer> getUserFollowedTopicCount(Long userId);

    /**
     * 获取话题的关注者数量
     * 统计关注指定话题的用户总数
     *
     * @param topicId 话题ID
     * @return 关注者数量
     */
    Result<Integer> getTopicFollowersCount(Long topicId);
}