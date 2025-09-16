package org.doubao.search.service.db.dto;

import org.doubao.mall.common.entity.BaseEntity;
import org.doubao.mall.common.entity.UserInfo;

import java.util.List;

public class SearchResultDTO extends BaseEntity {
    /**
     * 引文id
     */
    private Long id;
    /**
     * 引文内容
     */
    private String content;
    /**
     * 引文作者
     */
    private String author;
    /**
     * 引文来源
     */
    private String source;
    /**
     * 分类名称
     */
    private String categoryName;
    /**
     * 分类id
     */
    private Long categoryId;
    /**
     * 标签列表
     */
    private List<Tag> tags;
    /**
     * 是否原创
     */
    private int original;

    /**
     * 用户信息
     */
    private UserInfo userInfo;

    /**
     * 是否关注
     */
    private boolean follow;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public int getOriginal() {
        return original;
    }

    public void setOriginal(int original) {
        this.original = original;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public boolean isFollow() {
        return follow;
    }

    public void setFollow(boolean follow) {
        this.follow = follow;
    }

    public static class Tag {
        private Long id;
        private String name;

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
    }
}
