package org.doubao.topic.service.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.util.UserContext;
import org.doubao.topic.service.dto.*;
import org.doubao.topic.service.service.TopicService;
import org.doubao.topic.service.vo.TopicVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 话题控制器
 * 提供话题相关的REST API接口，包括话题创建、管理、关注等功能
 *
 * @author lingma
 * @since 1.0.0
 */
@RestController
@RequestMapping("/topic")
public class TopicController {

    @Autowired
    private TopicService topicService;

    /**
     * 创建话题
     * 通过TopicCreateDTO接收话题创建信息，使用当前用户ID作为创建者
     *
     * @param dto 话题创建DTO
     * @return 话题ID
     */
    @PostMapping("/create")
    public Result<Long> createTopic(@RequestBody TopicCreateDTO dto) {
        Long userId = UserContext.getUserId();
        return topicService.createTopic(dto, userId);
    }

    /**
     * 更新话题
     * 通过TopicUpdateDTO接收话题更新信息，验证用户权限后更新话题
     *
     * @param dto 话题更新DTO
     * @return 操作结果
     */
    @PostMapping("/update")
    public Result<Boolean> updateTopic(@RequestBody TopicUpdateDTO dto) {
        Long userId = UserContext.getUserId();
        return topicService.updateTopic(dto, userId);
    }

    /**
     * 删除话题
     * 逻辑删除指定话题，仅创建者或管理员可执行此操作
     *
     * @param id 话题ID
     * @return 操作结果
     */
    @DeleteMapping("/delete/{id}")
    public Result<Boolean> deleteTopic(@PathVariable("id") Long id) {
        Long userId = UserContext.getUserId();
        return topicService.deleteTopic(id, userId);
    }

    /**
     * 获取话题详情
     * 根据话题ID获取话题详细信息，包括统计信息
     *
     * @param id 话题ID
     * @return 话题详情
     */
    @GetMapping("/{id}")
    public Result<TopicVO> getTopicById(@PathVariable("id") Long id) {
        return topicService.getTopicById(id);
    }

    /**
     * 查询话题列表
     * 根据查询条件获取话题列表，支持关键词、分类、状态等筛选
     *
     * @param queryDTO 查询条件DTO
     * @return 话题分页列表
     */
    @GetMapping("/list")
    public Result<Page<TopicVO>> queryTopics(TopicQueryDTO queryDTO) {
        return topicService.queryTopics(queryDTO);
    }

    /**
     * 绑定文案到话题
     * 建立文案与话题的关联关系
     *
     * @param dto 绑定DTO
     * @return 操作结果
     */
    @PostMapping("/bind-quote")
    public Result<Boolean> bindQuoteToTopic(@RequestBody TopicBindDTO dto) {
        return topicService.bindQuoteToTopic(dto);
    }

    /**
     * 关注话题
     * 创建当前用户与指定话题的关注关系
     *
     * @param topicId 话题ID
     * @return 操作结果
     */
    @PostMapping("/follow/{topicId}")
    public Result<Boolean> followTopic(@PathVariable("topicId") Long topicId) {
        Long userId = UserContext.getUserId();
        return topicService.followTopic(topicId, userId);
    }

    /**
     * 取消关注话题
     * 移除当前用户与指定话题的关注关系
     *
     * @param topicId 话题ID
     * @return 操作结果
     */
    @PostMapping("/unfollow/{topicId}")
    public Result<Boolean> unfollowTopic(@PathVariable("topicId") Long topicId) {
        Long userId = UserContext.getUserId();
        return topicService.unfollowTopic(topicId, userId);
    }

    /**
     * 增加话题浏览量
     * 更新指定话题的浏览次数统计
     *
     * @param topicId 话题ID
     * @return 操作结果
     */
    @PostMapping("/view/{topicId}")
    public Result<Boolean> incrementViewCount(@PathVariable("topicId") Long topicId) {
        return topicService.incrementViewCount(topicId);
    }

    /**
     * 获取推荐话题
     * 查询已发布且推荐的话题列表
     *
     * @param queryDTO 查询条件DTO
     * @return 推荐话题分页列表
     */
    @GetMapping("/recommended")
    public Result<Page<TopicVO>> getRecommendedTopics(TopicQueryDTO queryDTO) {
        return topicService.getRecommendedTopics(queryDTO);
    }

    /**
     * 获取用户关注的话题列表
     * 查询当前用户关注的所有话题
     *
     * @param page 页码，默认为1
     * @param size 每页大小，默认为10
     * @return 关注话题分页列表
     */
    @GetMapping("/followed")
    public Result<Page<TopicVO>> getFollowedTopics(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = UserContext.getUserId();
        return topicService.getFollowedTopics(userId, page, size);
    }
}