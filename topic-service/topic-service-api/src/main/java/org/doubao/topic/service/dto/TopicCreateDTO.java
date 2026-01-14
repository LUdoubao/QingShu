package org.doubao.topic.service.dto;

/**
 * 话题创建数据传输对象
 * 用于接收创建话题时提交的相关信息
 */
public class TopicCreateDTO {
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
     * 标签ID数组
     */
    private Long[] tagIds;

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

    public Long[] getTagIds() {
        return tagIds;
    }

    public void setTagIds(Long[] tagIds) {
        this.tagIds = tagIds;
    }
}