package org.doubao.topic.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.util.DoubaoUtils;
import org.doubao.topic.service.entity.TopicTimeline;
import org.doubao.topic.service.mapper.TopicTimelineMapper;
import org.doubao.topic.service.service.TopicTimelineService;
import org.doubao.topic.service.vo.TopicTimelineVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 话题动态服务实现类
 * 提供话题动态的创建、查询、删除等功能的具体实现
 *
 * @author lingma
 * @since 1.0.0
 */
@Service
public class TopicTimelineServiceImpl extends ServiceImpl<TopicTimelineMapper, TopicTimeline> implements TopicTimelineService {

    @Resource
    private TopicTimelineMapper topicTimelineMapper;

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
    @Override
    @Transactional
    public Result<Long> createTimelineEvent(Long topicId, Integer eventType, Long targetId, Long actorId) {
        if (DoubaoUtils.isEmpty(topicId) || DoubaoUtils.isEmpty(eventType) || DoubaoUtils.isEmpty(targetId) || DoubaoUtils.isEmpty(actorId)) {
            return Result.error("参数不能为空");
        }

        TopicTimeline timeline = new TopicTimeline();
        timeline.setTopicId(topicId);
        timeline.setEventType(eventType);
        timeline.setTargetId(targetId);
        timeline.setActorId(actorId);
        timeline.setEventTime(LocalDateTime.now());
        timeline.setWeight(BigDecimal.valueOf(System.currentTimeMillis())); // 使用时间戳作为初始权重

        this.save(timeline);
        return Result.success(timeline.getId());
    }

    /**
     * 根据话题ID获取动态列表
     * 查询指定话题的动态信息
     *
     * @param topicId 话题ID
     * @param page 页码
     * @param size 每页大小
     * @return 动态分页列表
     */
    @Override
    public Result<Page<TopicTimelineVO>> getTimelineByTopicId(Long topicId, Integer page, Integer size) {
        if (DoubaoUtils.isEmpty(topicId)) {
            return Result.error("话题ID不能为空");
        }

        LambdaQueryWrapper<TopicTimeline> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TopicTimeline::getTopicId, topicId)
                .eq(TopicTimeline::getDeleted, 0) // 未删除的
                .orderByDesc(TopicTimeline::getEventTime); // 按时间倒序

        Page<TopicTimeline> timelinePage = new Page<>(page, size);
        Page<TopicTimeline> result = this.page(timelinePage, wrapper);

        Page<TopicTimelineVO> voPage = new Page<>();
        BeanUtils.copyProperties(result, voPage);

        for (TopicTimeline timeline : result.getRecords()) {
            TopicTimelineVO vo = new TopicTimelineVO();
            BeanUtils.copyProperties(timeline, vo);
            voPage.getRecords().add(vo);
        }

        return Result.success(voPage);
    }

    /**
     * 删除话题动态事件
     * 逻辑删除指定的动态事件
     *
     * @param timelineId 动态ID
     * @return 操作结果
     */
    @Override
    @Transactional
    public Result<Boolean> deleteTimelineEvent(Long timelineId) {
        if (DoubaoUtils.isEmpty(timelineId)) {
            return Result.error("动态ID不能为空");
        }

        TopicTimeline timeline = this.getById(timelineId);
        if (timeline == null) {
            return Result.error("动态不存在");
        }

        // 逻辑删除
        TopicTimeline updateTimeline = new TopicTimeline();
        updateTimeline.setId(timelineId);
        updateTimeline.setDeleted(1);

        boolean result = this.updateById(updateTimeline);
        return Result.success(result);
    }
}