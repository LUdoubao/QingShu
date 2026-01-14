package org.doubao.topic.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.doubao.mall.common.entity.Result;
import org.doubao.topic.service.entity.TopicCategory;
import org.doubao.topic.service.vo.TopicCategoryVO;

import java.util.List;

/**
 * 话题分类服务接口
 * 提供话题分类的创建、管理、查询等功能
 */
public interface TopicCategoryService extends IService<TopicCategory> {
    /**
     * 创建话题分类
     * 验证参数并创建分类记录
     *
     * @param name 分类名称
     * @param description 分类描述
     * @param parentId 父分类ID，0表示一级分类
     * @param sort 排序权重
     * @param status 状态：0-禁用 1-启用
     * @return 分类ID
     */
    Result<Long> createCategory(String name, String description, Long parentId, Integer sort, Integer status);

    /**
     * 更新话题分类
     * 检查分类存在性并更新信息
     *
     * @param id 分类ID
     * @param name 分类名称
     * @param description 分类描述
     * @param parentId 父分类ID
     * @param sort 排序权重
     * @param status 状态：0-禁用 1-启用
     * @return 操作结果
     */
    Result<Boolean> updateCategory(Long id, String name, String description, Long parentId, Integer sort, Integer status);

    /**
     * 删除话题分类
     * 检查是否有子分类，存在子分类则不允许删除
     *
     * @param id 分类ID
     * @return 操作结果
     */
    Result<Boolean> deleteCategory(Long id);

    /**
     * 根据ID获取分类详情
     *
     * @param id 分类ID
     * @return 分类详情
     */
    Result<TopicCategoryVO> getCategoryById(Long id);

    /**
     * 获取所有分类
     * 返回所有分类列表
     *
     * @return 分类列表
     */
    Result<List<TopicCategoryVO>> getAllCategories();

    /**
     * 获取启用的分类
     * 返回状态为启用的分类列表，并构建树形结构
     *
     * @return 启用的分类树形列表
     */
    Result<List<TopicCategoryVO>> getEnabledCategories();

    /**
     * 根据父分类ID获取子分类
     * 获取指定父分类下的所有子分类
     *
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    Result<List<TopicCategoryVO>> getChildrenByParentId(Long parentId);
}