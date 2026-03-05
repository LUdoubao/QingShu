package org.doubao.topic.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.enums.ErrorCode;
import org.doubao.mall.common.exception.BusinessException;
import org.doubao.mall.common.util.DoubaoUtils;
import org.doubao.topic.service.entity.TopicStatistics;
import org.doubao.topic.service.entity.UserTopicFollow;
import org.doubao.topic.service.mapper.UserTopicFollowMapper;
import org.doubao.topic.service.service.TopicStatisticsService;
import org.doubao.topic.service.service.UserTopicFollowService;
import org.doubao.topic.service.vo.TopicFollowVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户话题关注服务实现类
 * 提供用户关注话题、取消关注、查询关注状态等功能的具体实现
 */
@Service
public class UserTopicFollowServiceImpl extends ServiceImpl<UserTopicFollowMapper, UserTopicFollow> implements UserTopicFollowService {

    private static final Logger log = LoggerFactory.getLogger(UserTopicFollowServiceImpl.class);
    @Resource
    private UserTopicFollowMapper userTopicFollowMapper;

    @Autowired
    private TopicStatisticsService topicStatisticsService;

    /**
     * 关注话题
     * 创建用户与话题的关注关系
     *
     * @param userId 用户ID
     * @param topicId 话题ID
     * @return 操作结果
     */
    @Override
    @Transactional
    public Result<Boolean> followTopic(Long userId, Long topicId) {
        if (DoubaoUtils.isEmpty(userId) || DoubaoUtils.isEmpty(topicId)) {
            throw new BusinessException(ErrorCode.TOPIC_OR_USER_ID_EMPTY);
        }

        // 检查是否已关注
        LambdaQueryWrapper<UserTopicFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserTopicFollow::getUserId, userId)
                .eq(UserTopicFollow::getTopicId, topicId);
        UserTopicFollow existingFollow = this.getOne(wrapper);

        if (existingFollow != null) {
            if (existingFollow.getIsValid() == 1) {
                throw new BusinessException(ErrorCode.USER_ALREADY_FOLLOWED_TOPIC);
            } else {
                // 重新关注，更新状态
                UserTopicFollow updateFollow = new UserTopicFollow();
                updateFollow.setId(existingFollow.getId());
                updateFollow.setIsValid(1);
                updateFollow.setFollowTime(LocalDateTime.now());
                boolean result = this.updateById(updateFollow);

                if (result) {
                    // 更新统计信息
                    topicStatisticsService.incrementFollowCount(topicId);
                }

                return Result.success(result);
            }
        }

        // 创建新的关注记录
        UserTopicFollow follow = new UserTopicFollow();
        follow.setUserId(userId);
        follow.setTopicId(topicId);
        follow.setFollowTime(LocalDateTime.now());
        follow.setIsValid(1);

        boolean result = this.save(follow);

        if (result) {
            // 更新统计信息
            topicStatisticsService.incrementFollowCount(topicId);
        }

        return Result.success(result);
    }

    /**
     * 取消关注话题
     * 移除用户与话题的关注关系
     *
     * @param userId 用户ID
     * @param topicId 话题ID
     * @return 操作结果
     */
    @Override
    @Transactional
    public Result<Boolean> unfollowTopic(Long userId, Long topicId) {
        if (DoubaoUtils.isEmpty(userId) || DoubaoUtils.isEmpty(topicId)) {
            throw new BusinessException(ErrorCode.TOPIC_OR_USER_ID_EMPTY);
        }

        LambdaQueryWrapper<UserTopicFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserTopicFollow::getUserId, userId)
                .eq(UserTopicFollow::getTopicId, topicId)
                .eq(UserTopicFollow::getIsValid, 1); // 只处理有效的关注

        UserTopicFollow follow = this.getOne(wrapper);
        if (follow == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOLLOW_TOPIC);
        }

        // 更新关注状态
        UserTopicFollow updateFollow = new UserTopicFollow();
        updateFollow.setId(follow.getId());
        updateFollow.setIsValid(0);
        updateFollow.setUnfollowTime(LocalDateTime.now());

        boolean result = this.updateById(updateFollow);

        if (result) {
            // 更新统计信息
            // topicStatisticsService.decrementFollowCount(topicId);
        }

        return Result.success(result);
    }

    /**
     * 检查用户是否关注了话题
     * 判断用户与话题之间是否存在有效的关注关系
     *
     * @param userId 用户ID
     * @param topicIds 话题ID
     * @return 是否关注
     */
    @Override
    public List<TopicFollowVo> isUserFollowingTopic(Long userId, List<Long> topicIds) {
        if (DoubaoUtils.isEmpty(userId) || DoubaoUtils.isEmpty(topicIds)) {
            log.error(ErrorCode.TOPIC_OR_USER_ID_EMPTY.getMessage());
            return new ArrayList<>();
        }
		return userTopicFollowMapper.isUserFollowingTopic(userId, topicIds);
    }

    /**
     * 获取用户关注的话题数量
     * 统计指定用户关注的话题总数
     *
     * @param userId 用户ID
     * @return 关注话题数量
     */
    @Override
    public Result<Integer> getUserFollowedTopicCount(Long userId) {
        if (DoubaoUtils.isEmpty(userId)) {
            throw new BusinessException(ErrorCode.USER_ID_EMPTY);
        }

        LambdaQueryWrapper<UserTopicFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserTopicFollow::getUserId, userId)
                .eq(UserTopicFollow::getIsValid, 1);

        int count = this.count(wrapper);
        return Result.success(count);
    }

    /**
     * 获取话题的关注者数量
     * 统计关注指定话题的用户总数
     *
     * @param topicId 话题ID
     * @return 关注者数量
     */
    @Override
    public Result<Integer> getTopicFollowersCount(Long topicId) {
        if (DoubaoUtils.isEmpty(topicId)) {
            throw new BusinessException(ErrorCode.TOPIC_ID_EMPTY);
        }

        LambdaQueryWrapper<UserTopicFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserTopicFollow::getTopicId, topicId)
                .eq(UserTopicFollow::getIsValid, 1);

        int count = this.count(wrapper);
        return Result.success(count);
    }
}