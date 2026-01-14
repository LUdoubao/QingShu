package org.doubao.topic.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.doubao.mall.common.entity.Result;
import org.doubao.topic.service.entity.TopicStatistics;

/**
 * 话题统计服务接口
 * 提供话题相关统计数据的增减和查询功能
 */
public interface TopicStatisticsService extends IService<TopicStatistics> {
    /**
     * 增加话题文案引用数
     * 当有新文案绑定到话题时调用
     *
     * @param topicId 话题ID
     * @param userId 用户ID
     * @return 操作结果
     */
    Result<Boolean> incrementQuoteCount(Long topicId, Long userId);

    /**
     * 减少话题文案引用数
     * 当文案从话题解绑时调用
     *
     * @param topicId 话题ID
     * @param userId 用户ID
     * @return 操作结果
     */
    Result<Boolean> decrementQuoteCount(Long topicId, Long userId);

    /**
     * 增加话题关注数
     * 当用户关注话题时调用
     *
     * @param topicId 话题ID
     * @return 操作结果
     */
    Result<Boolean> incrementFollowCount(Long topicId);

    /**
     * 减少话题关注数
     * 当用户取消关注话题时调用
     *
     * @param topicId 话题ID
     * @return 操作结果
     */
    Result<Boolean> decrementFollowCount(Long topicId);

    /**
     * 增加话题浏览量
     * 当话题被访问时调用
     *
     * @param topicId 话题ID
     * @return 操作结果
     */
    Result<Boolean> incrementViewCount(Long topicId);

    /**
     * 根据话题ID获取统计信息
     *
     * @param topicId 话题ID
     * @return 话题统计信息
     */
    Result<TopicStatistics> getStatisticsByTopicId(Long topicId);

    /**
     * 更新话题热门文案
     * 设置话题中最受欢迎的文案ID
     *
     * @param topicId 话题ID
     * @param quoteId 文案ID
     * @return 操作结果
     */
    Result<Boolean> updateHotQuote(Long topicId, Long quoteId);

    /**
     * 更新话题今日新增文案数
     * 增加话题今日新增的文案数量
     *
     * @param topicId 话题ID
     * @return 操作结果
     */
    Result<Boolean> updateTodayQuoteCount(Long topicId);
}