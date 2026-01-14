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
    @TableId(type = IdType.AUTO)
    /**
     * 话题ID
     */
    private Long id;
    @TableField(value = "name")
    /**
     * 话题名称
     */
    private String name;
    @TableField(value = "description")
    /**
     * 话题描述
     */
    private String description;
    @TableField(value = "cover_key")
    /**
     * 封面图KEY
     */
    private String coverKey;
    @TableField(value = "creator_id")
    /**
     * 创建人ID
     */
    private Long creatorId;
    @TableField(value = "category_id")
    /**
     * 分类ID
     */
    private Long categoryId;
    @TableField(value = "status")
    /**
     * 状态
     */
    private Integer status;
    @TableField(value = "is_recommend")
    /**
     * 是否推荐
     */
    private Integer isRecommend;
    @TableField(value = "weight")
    /**
     * 排序权重
     */
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

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
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