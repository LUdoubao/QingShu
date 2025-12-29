package org.doubao.search.service.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.doubao.mall.common.entity.BaseDel;

import java.time.LocalDateTime;

@TableName("search_history")
public class SearchHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("user_id")
    private Long userId;

    @TableField("keyword")
    private String keyword;
    
    @TableField("created_time")
    private LocalDateTime createdTime;

    @TableLogic
    @TableField("deleted")
    private int deleted;

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

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }
}