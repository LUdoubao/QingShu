package org.doubao.topic.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.doubao.mall.common.dto.TopicBindDTO;
import org.doubao.mall.common.dto.TopicNameVo;
import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.enums.ErrorCode;
import org.doubao.mall.common.exception.BusinessException;
import org.doubao.mall.common.util.DoubaoUtils;
import org.doubao.mall.common.util.UserContext;
import org.doubao.topic.service.dto.*;
import org.doubao.topic.service.entity.Topic;
import org.doubao.topic.service.entity.TopicStatistics;
import org.doubao.topic.service.entity.UserTopicFollow;
import org.doubao.topic.service.entity.QuoteTopic;
import org.doubao.topic.service.mapper.TopicMapper;
import org.doubao.topic.service.service.QuoteTopicService;
import org.doubao.topic.service.service.TopicService;
import org.doubao.topic.service.service.TopicStatisticsService;
import org.doubao.topic.service.service.UserTopicFollowService;
import org.doubao.topic.service.vo.TopicFollowVo;
import org.doubao.topic.service.vo.TopicSelectVo;
import org.doubao.topic.service.vo.TopicVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 话题服务实现类
 * 提供话题创建、管理、关注等功能的具体实现
 */
@Service
public class TopicServiceImpl extends ServiceImpl<TopicMapper, Topic> implements TopicService {

    @Resource
    private TopicMapper topicMapper;
    @Resource
    private QuoteTopicService quoteTopicService;


    @Autowired
    private TopicStatisticsService topicStatisticsService;

    @Autowired
    private UserTopicFollowService userTopicFollowService;


    /**
     * 创建话题
     * 验证参数、检查名称唯一性、创建话题记录和统计记录
     *
     * @param dto 话题创建DTO
     * @param userId 创建者ID
     * @return 话题ID
     */
    @Override
    @Transactional
    public Result<Long> createTopic(TopicCreateDTO dto, Long userId) {
        // 验证参数
        if (DoubaoUtils.isEmpty(dto.getName())) {
            throw new BusinessException(ErrorCode.TOPIC_NAME_EMPTY);
        }

        // 检查话题名称是否已存在
        LambdaQueryWrapper<Topic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Topic::getName, dto.getName());
        wrapper.eq(Topic::getDeleted, 0); // 未删除的
        Topic existingTopic = this.getOne(wrapper);
        if (existingTopic != null) {
            throw new BusinessException(ErrorCode.TOPIC_NAME_EXISTS);
        }

        // 创建话题
        Topic topic = new Topic();
        topic.setName(dto.getName());
        topic.setDescription(dto.getDescription());
        topic.setCoverKey(dto.getCoverKey());
        topic.setCreatedId(userId);
        topic.setCategoryId(dto.getCategoryId());
        topic.setStatus(0); // 审核中
        topic.setIsRecommend(0);
        topic.setWeight(0);

        this.save(topic);

        // 创建统计记录
        TopicStatistics statistics = new TopicStatistics();
        statistics.setTopicId(topic.getId());
        statistics.setQuoteCount(0);
        statistics.setFollowCount(0);
        statistics.setViewCount(0L);
        statistics.setTodayQuoteCount(0);
        topicStatisticsService.save(statistics);

        // 创建审核记录
        createAuditLog(topic.getId(), 0, "待审核", userId);

        return Result.success(topic.getId());
    }

    /**
     * 更新话题
     * 检查权限（仅创建者或管理员可修改）并更新话题信息
     *
     * @param dto 话题更新DTO
     * @param userId 操作用户ID
     * @return 操作结果
     */
    @Override
    @Transactional
    public Result<Boolean> updateTopic(TopicUpdateDTO dto, Long userId) {
        Topic existingTopic = this.getById(dto.getId());
        if (existingTopic == null) {
            throw new BusinessException(ErrorCode.TOPIC_NOT_FOUND);
        }

        // 检查权限（只有创建者或管理员才能修改）
        if (!existingTopic.getCreatedId().equals(userId)) {
            throw new BusinessException(ErrorCode.TOPIC_NO_PERMISSION);
        }

        existingTopic.setName(dto.getName());
        existingTopic.setDescription(dto.getDescription());
        existingTopic.setCoverKey(dto.getCoverKey());
        existingTopic.setCategoryId(dto.getCategoryId());
        existingTopic.setWeight(dto.getWeight());

        boolean result = this.updateById(existingTopic);
        return Result.success(result);
    }

    /**
     * 删除话题
     * 逻辑删除，仅创建者或管理员可删除
     *
     * @param topicId 话题ID
     * @param userId 操作用户ID
     * @return 操作结果
     */
    @Override
    @Transactional
    public Result<Boolean> deleteTopic(Long topicId, Long userId) {
        Topic topic = this.getById(topicId);
        if (topic == null) {
            throw new BusinessException(ErrorCode.TOPIC_NOT_FOUND);
        }

        // 检查权限（只有创建者或管理员才能删除）
        if (!topic.getCreatedId().equals(userId)) {
            throw new BusinessException(ErrorCode.TOPIC_NO_PERMISSION);
        }

        // 逻辑删除
        Topic updateTopic = new Topic();
        updateTopic.setId(topicId);
        updateTopic.setDeleted(1);
        boolean result = this.updateById(updateTopic);

        return Result.success(result);
    }

    /**
     * 根据ID获取话题详情
     * 包含话题基本信息和统计信息
     *
     * @param topicId 话题ID
     * @return 话题详情
     */
    @Override
    public Result<TopicVO> getTopicById(Long topicId) {
        Topic topic = this.getById(topicId);
        if (topic == null) {
            throw new BusinessException(ErrorCode.TOPIC_NOT_FOUND);
        }

        TopicVO vo = new TopicVO();
        BeanUtils.copyProperties(topic, vo);

        Long userId = UserContext.getUserId();
        List<TopicFollowVo> data = userTopicFollowService.isUserFollowingTopic(userId, Collections.singletonList(topicId));
        if (DoubaoUtils.isNotEmpty(data)) {
            vo.setFollowed(data.get(0).getFollowed());
        }

        // 获取统计信息
        TopicStatistics statistics = topicStatisticsService.getById(topicId);
        if (statistics != null) {
            vo.setQuoteCount(statistics.getQuoteCount());
            vo.setFollowCount(statistics.getFollowCount());
            vo.setViewCount(statistics.getViewCount());
        }

        return Result.success(vo);
    }

    /**
     * 查询话题列表
     * 支持关键词、分类、状态等条件筛选
     *
     * @param queryDTO 查询条件DTO
     * @return 话题分页列表
     */
    @Override
    public Result<Page<TopicVO>> queryTopics(TopicQueryDTO queryDTO) {
        LambdaQueryWrapper<Topic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Topic::getDeleted, 0)
                .eq(Topic::getStatus, 1);

        if (DoubaoUtils.isNotEmpty(queryDTO.getKeyword())) {
            wrapper.and(w -> w.like(Topic::getName, queryDTO.getKeyword())
                    .or()
                    .like(Topic::getDescription, queryDTO.getKeyword()));
        }

        if (DoubaoUtils.isNotEmpty(queryDTO.getCategoryId())) {
            wrapper.eq(Topic::getCategoryId, queryDTO.getCategoryId());
        }

        if (DoubaoUtils.isNotEmpty(queryDTO.getStatus())) {
            wrapper.eq(Topic::getStatus, queryDTO.getStatus());
        }

        if (DoubaoUtils.isNotEmpty(queryDTO.getIsRecommend())) {
            wrapper.eq(Topic::getIsRecommend, queryDTO.getIsRecommend());
        }

        Page<Topic> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        Page<Topic> topicPage = this.page(page, wrapper);

        Page<TopicVO> voPage = new Page<>();
        BeanUtils.copyProperties(topicPage, voPage);

        List<Long> topicIds = topicPage.getRecords().stream().map(Topic::getId).collect(Collectors.toList());
        Long userId = UserContext.getUserId();
        List<TopicFollowVo> data = userTopicFollowService.isUserFollowingTopic(userId, topicIds);
        List<Topic> records = topicPage.getRecords();
        List<TopicVO> voList = new ArrayList<>();
        for (Topic topic : records) {
            TopicVO vo = new TopicVO();
            BeanUtils.copyProperties(topic, vo);
            voList.add(vo);
        }
        for (TopicVO vo : voList) {
            // 获取统计信息
            TopicStatistics statistics = topicStatisticsService.getById(vo.getId());
            if (statistics != null) {
                vo.setQuoteCount(statistics.getQuoteCount());
                vo.setFollowCount(statistics.getFollowCount());
                vo.setViewCount(statistics.getViewCount());
            }

            data.stream().filter(d -> d.getTopicId().equals(vo.getId()))
                    .findFirst()
                    .ifPresent(d -> vo.setFollowed(d.getFollowed()));
        }
        voPage.setRecords(voList);

        return Result.success(voPage);
    }

    /**
     * 绑定文案到话题
     * 建立文案与话题的关联关系，并更新统计信息
     *
     * @param dto 绑定DTO
     * @return 操作结果
     */
    @Override
    @Transactional
    public void bindQuoteToTopic(TopicBindDTO dto) {
        quoteTopicService.bindQuoteToTopic(dto);
    }

    /**
     * 关注话题
     * 创建用户与话题的关注关系
     *
     * @param topicId 话题ID
     * @param userId 用户ID
     * @return 操作结果
     */
    @Override
    @Transactional
    public Result<Boolean> followTopic(Long topicId, Long userId) {
        return userTopicFollowService.followTopic(userId, topicId);
    }

    /**
     * 取消关注话题
     * 移除用户与话题的关注关系
     *
     * @param topicId 话题ID
     * @param userId 用户ID
     * @return 操作结果
     */
    @Override
    @Transactional
    public Result<Boolean> unfollowTopic(Long topicId, Long userId) {
        return userTopicFollowService.unfollowTopic(userId, topicId);
    }

    /**
     * 增加话题浏览量
     * 更新话题统计中的浏览次数
     *
     * @param topicId 话题ID
     * @return 操作结果
     */
    @Override
    public Result<Boolean> incrementViewCount(Long topicId) {
        return topicStatisticsService.incrementViewCount(topicId);
    }

    /**
     * 获取推荐话题
     * 查询已发布且推荐的话题列表
     *
     * @param queryDTO 查询条件DTO
     * @return 推荐话题分页列表
     */
    @Override
    public Result<Page<TopicVO>> getRecommendedTopics(TopicQueryDTO queryDTO) {
        LambdaQueryWrapper<Topic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Topic::getDeleted, 0) // 未删除的
                .eq(Topic::getStatus, 1) // 已发布的
                .eq(Topic::getIsRecommend, 1); // 推荐的

        if (DoubaoUtils.isNotEmpty(queryDTO.getCategoryId())) {
            wrapper.eq(Topic::getCategoryId, queryDTO.getCategoryId());
        }

        if (DoubaoUtils.isNotEmpty(queryDTO.getKeyword())) {
            wrapper.and(w -> w.like(Topic::getName, queryDTO.getKeyword())
                    .or()
                    .like(Topic::getDescription, queryDTO.getKeyword()));
        }

        // 按权重、引用数、关注数排序
        wrapper.orderByDesc(Topic::getWeight);

        Page<Topic> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        Page<Topic> topicPage = this.page(page, wrapper);

        Page<TopicVO> voPage = new Page<>();
        BeanUtils.copyProperties(topicPage, voPage);

        for (Topic topic : topicPage.getRecords()) {
            TopicVO vo = new TopicVO();
            BeanUtils.copyProperties(topic, vo);

            // 获取统计信息
            TopicStatistics statistics = topicStatisticsService.getById(topic.getId());
            if (statistics != null) {
                vo.setQuoteCount(statistics.getQuoteCount());
                vo.setFollowCount(statistics.getFollowCount());
                vo.setViewCount(statistics.getViewCount());
            }

            voPage.getRecords().add(vo);
        }

        return Result.success(voPage);
    }

    /**
     * 获取用户关注的话题列表
     * 查询指定用户关注的所有话题
     *
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 关注话题分页列表
     */
    @Override
    public Result<Page<TopicVO>> getFollowedTopics(Long userId, Integer page, Integer size) {
        // 获取用户关注的话题ID列表
        LambdaQueryWrapper<UserTopicFollow> followWrapper = new LambdaQueryWrapper<>();
        followWrapper.eq(UserTopicFollow::getUserId, userId)
                .eq(UserTopicFollow::getIsValid, 1);
        List<Long> topicIdList = userTopicFollowService.list(followWrapper)
                .stream()
                .map(UserTopicFollow::getTopicId)
                .collect(Collectors.toList());
        if (DoubaoUtils.isEmpty(topicIdList)) {
            return Result.success(new Page<>());
        }
        Page<Topic> topicPage = new Page<>(page, size);
        LambdaQueryWrapper<Topic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Topic::getDeleted, 0) // 未删除的
                .eq(Topic::getStatus, 1) // 已发布的
                .in(Topic::getId, topicIdList);
        this.page(topicPage, wrapper);
        Page<TopicVO> voPage = new Page<>();
        BeanUtils.copyProperties(topicPage, voPage);

        for (Topic topic : topicPage.getRecords()) {
            TopicVO vo = new TopicVO();
            BeanUtils.copyProperties(topic, vo);

            // 获取统计信息
            TopicStatistics statistics = topicStatisticsService.getById(topic.getId());
            if (statistics != null) {
                vo.setQuoteCount(statistics.getQuoteCount());
                vo.setFollowCount(statistics.getFollowCount());
                vo.setViewCount(statistics.getViewCount());
            }

            voPage.getRecords().add(vo);
        }

        return Result.success(voPage);
    }

    @Override
    public Result<List<TopicSelectVo>> querySelectTopics(TopicSelectQuery queryDTO) {
        String topicName = queryDTO.getTopicName();
        List<Long> ids = queryDTO.getIds();
        List<TopicSelectVo> topicSelectVos  = new ArrayList<>();
        if ((topicName == null || topicName.isEmpty()) && (ids == null || ids.isEmpty())) {
            // 查询被绑定的标签最多的10条
            topicSelectVos = topicMapper.selectTopTopics(10);
        } else {
            if (topicName != null && !topicName.isEmpty()) {
                // 按名称模糊查询
                List<Topic> list = this.list(new LambdaQueryWrapper<Topic>()
                        .like(Topic::getName, topicName)
                        .eq(Topic::getStatus, 1));
                topicSelectVos = list.stream().map(topic -> {
                    TopicSelectVo topicSelectVo = new TopicSelectVo();
                    topicSelectVo.setId(topic.getId());
                    topicSelectVo.setName(topic.getName());
                    return topicSelectVo;
                }).collect(Collectors.toList());
            } else {
                // 按id查询
                topicSelectVos = this.listByIds(ids).stream().map(topic -> {
                    TopicSelectVo topicSelectVo = new TopicSelectVo();
                    topicSelectVo.setId(topic.getId());
                    topicSelectVo.setName(topic.getName());
                    return topicSelectVo;
                }).collect(Collectors.toList());
            }
        }
        return Result.success(topicSelectVos);
    }

    @Override
    public void deleteQuoteBind(List<Long> quoteIds) {
       quoteTopicService.deleteQuoteBind(quoteIds);
    }

    @Override
    public Result<TopicNameVo> getNameById(Long id) {
        LambdaQueryWrapper<QuoteTopic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuoteTopic::getQuoteId, id);
        List<QuoteTopic> quoteTopics = quoteTopicService.list(wrapper);
        if (DoubaoUtils.isEmpty(quoteTopics)) {
            return Result.error("该引用未绑定话题");
        }
        Long topicId = quoteTopics.get(0).getTopicId();
        Topic topic = this.getById(topicId);
        if (DoubaoUtils.isEmpty(topic)) {
            return Result.error("话题不存在");
        }

        return Result.success(new TopicNameVo(topic.getId(), topic.getName()));
    }

    /**
     * 创建审核日志
     * 记录话题的审核状态和相关信息
     *
     * @param topicId 话题ID
     * @param status 审核状态
     * @param reason 审核理由
     * @param auditorId 审核员ID
     */
    private void createAuditLog(Long topicId, Integer status, String reason, Long auditorId) {
        // 这里应该调用审核日志服务，为简化暂时留空
        // 实际实现中需要创建TopicAuditLog记录
    }
}