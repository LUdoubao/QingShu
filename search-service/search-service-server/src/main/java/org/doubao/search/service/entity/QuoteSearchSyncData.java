package org.doubao.search.service.entity;

public class QuoteSearchSyncData {
    private Long bizId;
    private String title;
    private String content;
    private String authorName;
    private String source;
    private Long categoryId;
    private String categoryName;
    private String tagNamesText;
    private Integer status;
    private Integer isOriginal;

    public Long getBizId() { return bizId; }
    public void setBizId(Long bizId) { this.bizId = bizId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getTagNamesText() { return tagNamesText; }
    public void setTagNamesText(String tagNamesText) { this.tagNamesText = tagNamesText; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getIsOriginal() { return isOriginal; }
    public void setIsOriginal(Integer isOriginal) { this.isOriginal = isOriginal; }
}
