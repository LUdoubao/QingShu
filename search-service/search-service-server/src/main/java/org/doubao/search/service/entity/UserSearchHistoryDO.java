package org.doubao.search.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("user_search_history")
public class UserSearchHistoryDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("query_text")
    private String queryText;

    @TableField("query_text_normalized")
    private String queryTextNormalized;

    @TableField("search_count")
    private Integer searchCount;

    @TableField("last_search_time")
    private LocalDateTime lastSearchTime;

    @TableField("deleted")
    private Integer deleted;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getQueryText() {
        return queryText;
    }

    public void setQueryText(String queryText) {
        this.queryText = queryText;
    }

    public String getQueryTextNormalized() {
        return queryTextNormalized;
    }

    public void setQueryTextNormalized(String queryTextNormalized) {
        this.queryTextNormalized = queryTextNormalized;
    }

    public Integer getSearchCount() {
        return searchCount;
    }

    public void setSearchCount(Integer searchCount) {
        this.searchCount = searchCount;
    }

    public LocalDateTime getLastSearchTime() {
        return lastSearchTime;
    }

    public void setLastSearchTime(LocalDateTime lastSearchTime) {
        this.lastSearchTime = lastSearchTime;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }
}
