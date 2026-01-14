package org.doubao.topic.service.controller;

import org.doubao.mall.common.entity.Result;
import org.doubao.topic.service.service.TopicCategoryService;
import org.doubao.topic.service.vo.TopicCategoryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 话题分类控制器
 * 提供话题分类相关的REST API接口
 *
 * @author lingma
 * @since 1.0.0
 */
@RestController
@RequestMapping("/topic-category")
public class TopicCategoryController {

    @Autowired
    private TopicCategoryService topicCategoryService;

    /**
     * 创建分类
     * 接收分类信息并创建新的分类记录
     *
     * @param name 分类名称
     * @param description 分类描述
     * @param parentId 父分类ID，0表示一级分类
     * @param sort 排序权重
     * @param status 状态：0-禁用 1-启用
     * @return 分类ID
     */
    @PostMapping("/create")
    public Result<Long> createCategory(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false) Integer sort,
            @RequestParam(required = false) Integer status) {
        return topicCategoryService.createCategory(name, description, parentId, sort, status);
    }

    /**
     * 更新分类
     * 更新指定分类的信息
     *
     * @param id 分类ID
     * @param name 分类名称
     * @param description 分类描述
     * @param parentId 父分类ID
     * @param sort 排序权重
     * @param status 状态：0-禁用 1-启用
     * @return 操作结果
     */
    @PostMapping("/update/{id}")
    public Result<Boolean> updateCategory(
            @PathVariable("id") Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false) Integer sort,
            @RequestParam(required = false) Integer status) {
        return topicCategoryService.updateCategory(id, name, description, parentId, sort, status);
    }

    /**
     * 删除分类
     * 删除指定分类，如果存在子分类则不允许删除
     *
     * @param id 分类ID
     * @return 操作结果
     */
    @DeleteMapping("/delete/{id}")
    public Result<Boolean> deleteCategory(@PathVariable("id") Long id) {
        return topicCategoryService.deleteCategory(id);
    }

    /**
     * 获取分类详情
     * 根据分类ID获取分类详细信息
     *
     * @param id 分类ID
     * @return 分类详情
     */
    @GetMapping("/{id}")
    public Result<TopicCategoryVO> getCategoryById(@PathVariable("id") Long id) {
        return topicCategoryService.getCategoryById(id);
    }

    /**
     * 获取所有分类
     * 返回所有分类的列表
     *
     * @return 分类列表
     */
    @GetMapping("/all")
    public Result<List<TopicCategoryVO>> getAllCategories() {
        return topicCategoryService.getAllCategories();
    }

    /**
     * 获取启用的分类
     * 返回状态为启用的分类列表，并构建树形结构
     *
     * @return 启用的分类树形列表
     */
    @GetMapping("/enabled")
    public Result<List<TopicCategoryVO>> getEnabledCategories() {
        return topicCategoryService.getEnabledCategories();
    }

    /**
     * 根据父分类ID获取子分类
     * 获取指定父分类下的所有子分类
     *
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    @GetMapping("/children/{parentId}")
    public Result<List<TopicCategoryVO>> getChildrenByParentId(@PathVariable("parentId") Long parentId) {
        return topicCategoryService.getChildrenByParentId(parentId);
    }
}