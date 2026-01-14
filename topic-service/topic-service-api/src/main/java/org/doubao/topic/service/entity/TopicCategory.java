package org.doubao.topic.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.doubao.mall.common.entity.BaseEntity;

/**
 * 话题分类实体类
 * 支持多级分类，便于话题筛选和管理
 */
@TableName("topic_category")
public class TopicCategory extends BaseEntity {
    /**
     * 分类ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 父分类ID：0=一级分类，支持多级嵌套
     */
    @TableField(value = "parent_id")
    private Long parentId;
    /**
     * 分类名称
     */
    @TableField(value = "name")
    private String name;
    /**
     * 分类说明
     */
    @TableField(value = "description")
    private String description;
    /**
     * 排序权重：值越小越靠前
     */
    @TableField(value = "sort")
    private Integer sort;
    /**
     * 状态：0-禁用 1-启用
     */
    @TableField(value = "status")
    private Integer status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}