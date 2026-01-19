package org.doubao.topic.service.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.doubao.mall.common.dto.TopicBindDTO;
import org.doubao.mall.common.dto.TopicNameVo;
import org.doubao.mall.common.entity.Result;
import org.doubao.topic.service.dto.*;
import org.doubao.topic.service.entity.Topic;
import org.doubao.topic.service.vo.TopicSelectVo;
import org.doubao.topic.service.vo.TopicVO;

import java.util.List;

/**
 * 话题服务接口
 * 提供话题创建、管理、关注等功能
 */
public interface TopicService extends IService<Topic> {
    /**
     * 创建话题
     * 验证参数、检查名称唯一性、创建话题记录和统计记录
     *
     * @param dto 话题创建DTO
     * @param userId 创建者ID
     * @return 话题ID
     */
    Result<Long> createTopic(TopicCreateDTO dto, Long userId);

    /**
     * 更新话题
     * 检查权限（仅创建者或管理员可修改）并更新话题信息
     *
     * @param dto 话题更新DTO
     * @param userId 操作用户ID
     * @return 操作结果
     */
    Result<Boolean> updateTopic(TopicUpdateDTO dto, Long userId);

    /**
     * 删除话题
     * 逻辑删除，仅创建者或管理员可删除
     *
     * @param topicId 话题ID
     * @param userId 操作用户ID
     * @return 操作结果
     */
    Result<Boolean> deleteTopic(Long topicId, Long userId);

    /**
     * 根据ID获取话题详情
     * 包含话题基本信息和统计信息
     *
     * @param topicId 话题ID
     * @return 话题详情
     */
    Result<TopicVO> getTopicById(Long topicId);

    /**
     * 查询话题列表
     * 支持关键词、分类、状态等条件筛选
     *
     * @param queryDTO 查询条件DTO
     * @return 话题分页列表
     */
    Result<Page<TopicVO>> queryTopics(TopicQueryDTO queryDTO);

    /**
     * 绑定文案到话题
     * 建立文案与话题的关联关系，并更新统计信息
     *
     * @param dto 绑定DTO
     * @return 操作结果
     */
    void bindQuoteToTopic(TopicBindDTO dto);

    /**
     * 关注话题
     * 创建用户与话题的关注关系
     *
     * @param topicId 话题ID
     * @param userId 用户ID
     * @return 操作结果
     */
    Result<Boolean> followTopic(Long topicId, Long userId);

    /**
     * 取消关注话题
     * 移除用户与话题的关注关系
     *
     * @param topicId 话题ID
     * @param userId 用户ID
     * @return 操作结果
     */
    Result<Boolean> unfollowTopic(Long topicId, Long userId);

    /**
     * 增加话题浏览量
     * 更新话题统计中的浏览次数
     *
     * @param topicId 话题ID
     * @return 操作结果
     */
    Result<Boolean> incrementViewCount(Long topicId);

    /**
     * 获取推荐话题
     * 查询已发布且推荐的话题列表
     *
     * @param queryDTO 查询条件DTO
     * @return 推荐话题分页列表
     */
    Result<Page<TopicVO>> getRecommendedTopics(TopicQueryDTO queryDTO);

    /**
     * 获取用户关注的话题列表
     * 查询指定用户关注的所有话题
     *
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 关注话题分页列表
     */
    Result<Page<TopicVO>> getFollowedTopics(Long userId, Integer page, Integer size);

    Result<List<TopicSelectVo>> querySelectTopics(TopicSelectQuery queryDTO);

	void deleteQuoteBind(List<Long> quoteIds);

    Result<TopicNameVo> getNameById(Long id);
}