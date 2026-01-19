package org.doubao.topic.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.enums.ErrorCode;
import org.doubao.mall.common.exception.BusinessException;
import org.doubao.topic.service.entity.TopicStatistics;
import org.doubao.topic.service.mapper.TopicStatisticsMapper;
import org.doubao.topic.service.service.TopicStatisticsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 话题统计服务实现类
 * 提供话题相关统计数据的增减和查询功能的具体实现
 */
@Service
public class TopicStatisticsServiceImpl extends ServiceImpl<TopicStatisticsMapper, TopicStatistics> implements TopicStatisticsService {

    @Resource
    private TopicStatisticsMapper topicStatisticsMapper;

    /**
     * 增加话题文案引用数
     * 当有新文案绑定到话题时调用
     *
     * @param topicId 话题ID
     */
    @Override
    @Transactional
    public void incrementQuoteCount(Long topicId) {
        TopicStatistics statistics = this.getById(topicId);
        if (statistics == null) {
            throw new BusinessException(ErrorCode.TOPIC_STATISTICS_NOT_FOUND);
        }

        topicStatisticsMapper.incrementQuoteCount(topicId);
    }

    /**
     * 减少话题文案引用数
     * 当文案从话题解绑时调用
     *
     * @param topicId 话题ID
     */
    @Override
    @Transactional
    public void decrementQuoteCount(Long topicId) {
        TopicStatistics statistics = this.getById(topicId);
        if (statistics == null) {
            throw new BusinessException(ErrorCode.TOPIC_STATISTICS_NOT_FOUND);
        }

        if (statistics.getQuoteCount() <= 0) {
            throw new BusinessException(ErrorCode.TOPIC_QUOTE_COUNT_ZERO);
        }

        topicStatisticsMapper.decrementQuoteCount(topicId);
    }

    @Override
    public void decrementQuoteCount(Set<Long> topicIds) {
        if (topicIds == null || topicIds.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        List<Long> newTopicIds = new ArrayList<>(topicIds);
        List<TopicStatistics> topicStatistics = this.listByIds(topicIds);
        for (TopicStatistics topicStatistic : topicStatistics) {
            if (topicStatistic.getQuoteCount() > 0) {
                newTopicIds.add(topicStatistic.getTopicId());
            }
        }
        topicStatisticsMapper.decrementQuoteCountBatch(newTopicIds);
    }

    /**
     * 增加话题关注数
     * 当用户关注话题时调用
     *
     * @param topicId 话题ID
     * @return 操作结果
     */
    @Override
    @Transactional
    public Result<Boolean> incrementFollowCount(Long topicId) {
        TopicStatistics statistics = this.getById(topicId);
        if (statistics == null) {
            throw new BusinessException(ErrorCode.TOPIC_STATISTICS_NOT_FOUND);
        }

        LambdaUpdateWrapper<TopicStatistics> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(TopicStatistics::getTopicId, topicId)
                .setSql("follow_count = follow_count + 1");

        boolean result = this.update(updateWrapper);
        return Result.success(result);
    }

    /**
     * 减少话题关注数
     * 当用户取消关注话题时调用
     *
     * @param topicId 话题ID
     * @return 操作结果
     */
    @Override
    @Transactional
    public Result<Boolean> decrementFollowCount(Long topicId) {
        TopicStatistics statistics = this.getById(topicId);
        if (statistics == null) {
            throw new BusinessException(ErrorCode.TOPIC_STATISTICS_NOT_FOUND);
        }

        if (statistics.getFollowCount() <= 0) {
            throw new BusinessException(ErrorCode.TOPIC_FOLLOW_COUNT_ZERO);
        }

        LambdaUpdateWrapper<TopicStatistics> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(TopicStatistics::getTopicId, topicId)
                .setSql("follow_count = follow_count - 1");

        boolean result = this.update(updateWrapper);
        return Result.success(result);
    }

    /**
     * 增加话题浏览量
     * 当话题被访问时调用
     *
     * @param topicId 话题ID
     * @return 操作结果
     */
    @Override
    @Transactional
    public Result<Boolean> incrementViewCount(Long topicId) {
        TopicStatistics statistics = this.getById(topicId);
        if (statistics == null) {
            throw new BusinessException(ErrorCode.TOPIC_STATISTICS_NOT_FOUND);
        }

        LambdaUpdateWrapper<TopicStatistics> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(TopicStatistics::getTopicId, topicId)
                .setSql("view_count = view_count + 1");

        boolean result = this.update(updateWrapper);
        return Result.success(result);
    }

    /**
     * 根据话题ID获取统计信息
     *
     * @param topicId 话题ID
     * @return 话题统计信息
     */
    @Override
    public Result<TopicStatistics> getStatisticsByTopicId(Long topicId) {
        TopicStatistics statistics = this.getById(topicId);
        if (statistics == null) {
            throw new BusinessException(ErrorCode.TOPIC_STATISTICS_NOT_FOUND);
        }

        return Result.success(statistics);
    }

    /**
     * 更新话题今日新增文案数
     * 增加话题今日新增的文案数量
     *
     * @param topicId 话题ID
     * @return 操作结果
     */
    @Override
    @Transactional
    public Result<Boolean> updateTodayQuoteCount(Long topicId) {
        TopicStatistics statistics = this.getById(topicId);
        if (statistics == null) {
            throw new BusinessException(ErrorCode.TOPIC_STATISTICS_NOT_FOUND);
        }

        LambdaUpdateWrapper<TopicStatistics> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(TopicStatistics::getTopicId, topicId)
                .setSql("today_quote_count = today_quote_count + 1");

        boolean result = this.update(updateWrapper);
        return Result.success(result);
    }
}