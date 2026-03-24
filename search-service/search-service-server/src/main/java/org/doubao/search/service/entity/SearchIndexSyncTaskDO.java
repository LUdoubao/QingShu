package org.doubao.search.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("search_index_sync_task")
public class SearchIndexSyncTaskDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("biz_type")
    private String bizType;
    @TableField("biz_id")
    private Long bizId;
    @TableField("op_type")
    private String opType;
    @TableField("task_status")
    private String taskStatus;
    @TableField("retry_count")
    private Integer retryCount;
    @TableField("fail_reason")
    private String failReason;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBizType() { return bizType; }
    public void setBizType(String bizType) { this.bizType = bizType; }
    public Long getBizId() { return bizId; }
    public void setBizId(Long bizId) { this.bizId = bizId; }
    public String getOpType() { return opType; }
    public void setOpType(String opType) { this.opType = opType; }
    public String getTaskStatus() { return taskStatus; }
    public void setTaskStatus(String taskStatus) { this.taskStatus = taskStatus; }
    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
    public String getFailReason() { return failReason; }
    public void setFailReason(String failReason) { this.failReason = failReason; }
}
