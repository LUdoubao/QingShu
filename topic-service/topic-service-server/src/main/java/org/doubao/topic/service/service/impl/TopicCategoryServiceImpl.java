package org.doubao.topic.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.util.DoubaoUtils;
import org.doubao.topic.service.entity.TopicCategory;
import org.doubao.topic.service.mapper.TopicCategoryMapper;
import org.doubao.topic.service.service.TopicCategoryService;
import org.doubao.topic.service.vo.TopicCategoryVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 话题分类服务实现类
 * 提供话题分类的创建、管理、查询等功能的具体实现
 *
 * @author lingma
 * @since 1.0.0
 */
@Service
public class TopicCategoryServiceImpl extends ServiceImpl<TopicCategoryMapper, TopicCategory> implements TopicCategoryService {

    @Resource
    private TopicCategoryMapper topicCategoryMapper;

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
    @Override
    public Result<Long> createCategory(String name, String description, Long parentId, Integer sort, Integer status) {
        if (DoubaoUtils.isEmpty(name)) {
            return Result.error("分类名称不能为空");
        }

        TopicCategory category = new TopicCategory();
        category.setName(name);
        category.setDescription(description);
        category.setParentId(parentId != null ? parentId : 0L);
        category.setSort(sort != null ? sort : 0);
        category.setStatus(status != null ? status : 1);

        this.save(category);
        return Result.success(category.getId());
    }

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
    @Override
    public Result<Boolean> updateCategory(Long id, String name, String description, Long parentId, Integer sort, Integer status) {
        TopicCategory category = this.getById(id);
        if (category == null) {
            return Result.error("分类不存在");
        }

        category.setName(name);
        category.setDescription(description);
        category.setParentId(parentId != null ? parentId : category.getParentId());
        category.setSort(sort != null ? sort : category.getSort());
        category.setStatus(status != null ? status : category.getStatus());

        boolean result = this.updateById(category);
        return Result.success(result);
    }

    /**
     * 删除话题分类
     * 检查是否有子分类，存在子分类则不允许删除
     *
     * @param id 分类ID
     * @return 操作结果
     */
    @Override
    public Result<Boolean> deleteCategory(Long id) {
        TopicCategory category = this.getById(id);
        if (category == null) {
            return Result.error("分类不存在");
        }

        // 检查是否有子分类
        LambdaQueryWrapper<TopicCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TopicCategory::getParentId, id);
        int count = this.count(wrapper);
        if (count > 0) {
            return Result.error("该分类下存在子分类，无法删除");
        }

        boolean result = this.removeById(id);
        return Result.success(result);
    }

    /**
     * 根据ID获取分类详情
     *
     * @param id 分类ID
     * @return 分类详情
     */
    @Override
    public Result<TopicCategoryVO> getCategoryById(Long id) {
        TopicCategory category = this.getById(id);
        if (category == null) {
            return Result.error("分类不存在");
        }

        TopicCategoryVO vo = new TopicCategoryVO();
        BeanUtils.copyProperties(category, vo);

        return Result.success(vo);
    }

    /**
     * 获取所有分类
     * 返回所有分类列表
     *
     * @return 分类列表
     */
    @Override
    public Result<List<TopicCategoryVO>> getAllCategories() {
        List<TopicCategory> categories = this.list();
        List<TopicCategoryVO> vos = categories.stream().map(category -> {
            TopicCategoryVO vo = new TopicCategoryVO();
            BeanUtils.copyProperties(category, vo);
            return vo;
        }).collect(Collectors.toList());

        return Result.success(vos);
    }

    /**
     * 获取启用的分类
     * 返回状态为启用的分类列表，并构建树形结构
     *
     * @return 启用的分类树形列表
     */
    @Override
    public Result<List<TopicCategoryVO>> getEnabledCategories() {
        LambdaQueryWrapper<TopicCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TopicCategory::getStatus, 1) // 启用状态
                .orderByAsc(TopicCategory::getSort); // 按排序字段升序

        List<TopicCategory> categories = this.list(wrapper);
        List<TopicCategoryVO> vos = categories.stream().map(category -> {
            TopicCategoryVO vo = new TopicCategoryVO();
            BeanUtils.copyProperties(category, vo);
            return vo;
        }).collect(Collectors.toList());

        // 构建树形结构
        List<TopicCategoryVO> tree = buildCategoryTree(vos, 0L);
        return Result.success(tree);
    }

    /**
     * 根据父分类ID获取子分类
     * 获取指定父分类下的所有子分类
     *
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    @Override
    public Result<List<TopicCategoryVO>> getChildrenByParentId(Long parentId) {
        LambdaQueryWrapper<TopicCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TopicCategory::getParentId, parentId != null ? parentId : 0L)
                .eq(TopicCategory::getStatus, 1) // 启用状态
                .orderByAsc(TopicCategory::getSort); // 按排序字段升序

        List<TopicCategory> categories = this.list(wrapper);
        List<TopicCategoryVO> vos = categories.stream().map(category -> {
            TopicCategoryVO vo = new TopicCategoryVO();
            BeanUtils.copyProperties(category, vo);
            return vo;
        }).collect(Collectors.toList());

        return Result.success(vos);
    }

    /**
     * 构建分类树形结构
     * 将平铺的分类列表转换为树形结构
     *
     * @param allCategories 所有分类列表
     * @param parentId 父分类ID
     * @return 树形结构的分类列表
     */
    private List<TopicCategoryVO> buildCategoryTree(List<TopicCategoryVO> allCategories, Long parentId) {
        List<TopicCategoryVO> result = new ArrayList<>();
        for (TopicCategoryVO category : allCategories) {
            if (category.getParentId().equals(parentId)) {
                // 递归获取子分类
                List<TopicCategoryVO> children = buildCategoryTree(allCategories, category.getId());
                category.setChildren(children);
                result.add(category);
            }
        }
        return result;
    }
}