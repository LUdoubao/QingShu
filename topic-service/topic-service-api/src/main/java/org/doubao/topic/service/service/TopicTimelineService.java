package org.doubao.topic.service.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.doubao.mall.common.entity.Result;
import org.doubao.topic.service.entity.TopicTimeline;
import org.doubao.topic.service.vo.TopicTimelineVO;

/**
 * 话题动态服务接口
 * 提供话题动态的创建、查询、删除等功能
 */
public interface TopicTimelineService extends IService<TopicTimeline> {
    /**
     * 创建话题动态事件
     * 记录话题相关的活动，如新增文案、热门文案等
     *
     * @param topicId 话题ID
     * @param eventType 事件类型：1-新增文案 2-热门文案 3-官方推荐 4-话题更新
     * @param targetId 目标ID（如文案ID、话题ID）
     * @param actorId 事件发起者ID（用户/管理员）
     * @return 动态事件ID
     */
    Result<Long> createTimelineEvent(Long topicId, Integer eventType, Long targetId, Long actorId);

    /**
     * 根据话题ID获取动态列表
     * 查询指定话题的动态信息
     *
     * @param topicId 话题ID
     * @param page 页码
     * @param size 每页大小
     * @return 动态分页列表
     */
    Result<Page<TopicTimelineVO>> getTimelineByTopicId(Long topicId, Integer page, Integer size);

    /**
     * 删除话题动态事件
     * 逻辑删除指定的动态事件
     *
     * @param timelineId 动态ID
     * @return 操作结果
     */
    Result<Boolean> deleteTimelineEvent(Long timelineId);
}