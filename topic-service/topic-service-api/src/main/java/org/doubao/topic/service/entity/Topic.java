package org.doubao.topic.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.doubao.mall.common.entity.BaseDel;

/**
 * 话题实体类
 * 存储话题的核心信息，包括名称、描述、分类、状态等
 */
@TableName("topic")
public class Topic extends BaseDel {
    /**
     * 话题ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 话题名称
     */
    @TableField(value = "name")
    private String name;
    /**
     * 话题描述
     */
    @TableField(value = "description")
    private String description;
    /**
     * 话题封面图KEY
     */
    @TableField(value = "cover_key")
    private String coverKey;
    /**
     * 分类ID
     */
    @TableField(value = "category_id")
    private Long categoryId;
    /**
     * 状态
     */
    @TableField(value = "status")
    private Integer status;
    /**
     * 是否推荐
     */
    @TableField(value = "is_recommend")
    private Integer isRecommend;
    /**
     * 排序权重
     */
    @TableField(value = "weight")
    private Integer weight;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getCoverKey() {
        return coverKey;
    }

    public void setCoverKey(String coverKey) {
        this.coverKey = coverKey;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getIsRecommend() {
        return isRecommend;
    }

    public void setIsRecommend(Integer isRecommend) {
        this.isRecommend = isRecommend;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }
}