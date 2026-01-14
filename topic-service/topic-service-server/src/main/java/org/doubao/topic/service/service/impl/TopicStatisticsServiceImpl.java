package org.doubao.topic.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.enums.ErrorCode;
import org.doubao.mall.common.exception.BusinessException;
import org.doubao.mall.common.util.DoubaoUtils;
import org.doubao.topic.service.entity.TopicStatistics;
import org.doubao.topic.service.mapper.TopicStatisticsMapper;
import org.doubao.topic.service.service.TopicStatisticsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

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
     * @param userId 用户ID
     * @return 操作结果
     */
    @Override
    @Transactional
    public Result<Boolean> incrementQuoteCount(Long topicId, Long userId) {
        TopicStatistics statistics = this.getById(topicId);
        if (statistics == null) {
            throw new BusinessException(ErrorCode.TOPIC_STATISTICS_NOT_FOUND);
        }

        // 更新引用数量和活跃用户数
        LambdaUpdateWrapper<TopicStatistics> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(TopicStatistics::getTopicId, topicId)
                .setSql("quote_count = quote_count + 1")
                .setSql("active_user_count = CASE WHEN active_user_count = 0 THEN 1 ELSE active_user_count END"); // 简化处理，实际可能需要更复杂的逻辑

        boolean result = this.update(updateWrapper);
        
        // 更新今日新增数量
        updateTodayQuoteCount(topicId);
        
        return Result.success(result);
    }

    /**
     * 减少话题文案引用数
     * 当文案从话题解绑时调用
     *
     * @param topicId 话题ID
     * @param userId 用户ID
     * @return 操作结果
     */
    @Override
    @Transactional
    public Result<Boolean> decrementQuoteCount(Long topicId, Long userId) {
        TopicStatistics statistics = this.getById(topicId);
        if (statistics == null) {
            throw new BusinessException(ErrorCode.TOPIC_STATISTICS_NOT_FOUND);
        }

        if (statistics.getQuoteCount() <= 0) {
            throw new BusinessException(ErrorCode.TOPIC_QUOTE_COUNT_ZERO);
        }

        LambdaUpdateWrapper<TopicStatistics> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(TopicStatistics::getTopicId, topicId)
                .setSql("quote_count = quote_count - 1");

        boolean result = this.update(updateWrapper);
        return Result.success(result);
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
     * 更新话题热门文案
     * 设置话题中最受欢迎的文案ID
     *
     * @param topicId 话题ID
     * @param quoteId 文案ID
     * @return 操作结果
     */
    @Override
    @Transactional
    public Result<Boolean> updateHotQuote(Long topicId, Long quoteId) {
        TopicStatistics statistics = this.getById(topicId);
        if (statistics == null) {
            throw new BusinessException(ErrorCode.TOPIC_STATISTICS_NOT_FOUND);
        }

        statistics.setHotQuoteId(quoteId);

        boolean result = this.updateById(statistics);
        return Result.success(result);
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