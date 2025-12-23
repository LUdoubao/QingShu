package org.doubao.user.service.entity.report;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审核记录实体类
 */
@TableName("report_review_log")
public class ReportReviewLog {

    @TableId(type = IdType.AUTO)
    private Long id; // 日志ID

    private Long reportId; // 举报ID

    private Long reviewerId; // 审核员ID

    private String reviewOpinion; // 审核意见

    private Integer reviewResult; // 审核结果：1=通过，2=不通过，3=打回补充证据

    private String handleAction; // 处理动作

    private LocalDateTime createdTime; // 审核时间

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public Long getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
    }

    public String getReviewOpinion() {
        return reviewOpinion;
    }

    public void setReviewOpinion(String reviewOpinion) {
        this.reviewOpinion = reviewOpinion;
    }

    public Integer getReviewResult() {
        return reviewResult;
    }

    public void setReviewResult(Integer reviewResult) {
        this.reviewResult = reviewResult;
    }

    public String getHandleAction() {
        return handleAction;
    }

    public void setHandleAction(String handleAction) {
        this.handleAction = handleAction;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }
}
