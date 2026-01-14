package org.doubao.topic.service.dto;

/**
 * 话题更新数据传输对象
 * 用于接收更新话题时提交的相关信息
 */
public class TopicUpdateDTO {
    /**
     * 话题ID
     */
    private Long id;
    /**
     * 话题名称
     */
    private String name;
    /**
     * 话题描述
     */
    private String description;
    /**
     * 话题封面图KEY
     */
    private String coverKey;
    /**
     * 话题分类ID
     */
    private Long categoryId;
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

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }
}