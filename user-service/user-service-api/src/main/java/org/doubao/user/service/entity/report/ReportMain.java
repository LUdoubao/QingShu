package org.doubao.user.service.entity.report;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 举报主表实体类
 */
@TableName("report_main")
public class ReportMain {

    @TableId(type = IdType.AUTO)
    private Long id; // 举报ID

    private Long userId; // 举报人ID

    private Integer reportedType; // 被举报对象类型：1=内容，2=用户，3=评论

    private String reportedId; // 被举报对象ID

    private Integer firstCategoryId; // 一级分类ID

    private Integer secondCategoryId; // 二级分类ID

    private Integer thirdCategoryId; // 三级分类ID

    private Integer riskLevel; // AI预检测风险等级：0=正常，1=低风险，2=高风险

    private Integer status; // 举报状态：0=待审核，1=审核中，2=审核通过，3=审核不通过，4=待复核

    private String handleResult; // 处理结果描述

    private LocalDateTime handleTime; // 处理时间

    private LocalDateTime createdTime; // 创建时间

    private LocalDateTime updatedTime; // 更新时间

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getReportedType() {
        return reportedType;
    }

    public void setReportedType(Integer reportedType) {
        this.reportedType = reportedType;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getReportedId() {
        return reportedId;
    }

    public void setReportedId(String reportedId) {
        this.reportedId = reportedId;
    }

    public Integer getFirstCategoryId() {
        return firstCategoryId;
    }

    public void setFirstCategoryId(Integer firstCategoryId) {
        this.firstCategoryId = firstCategoryId;
    }

    public Integer getSecondCategoryId() {
        return secondCategoryId;
    }

    public void setSecondCategoryId(Integer secondCategoryId) {
        this.secondCategoryId = secondCategoryId;
    }

    public Integer getThirdCategoryId() {
        return thirdCategoryId;
    }

    public void setThirdCategoryId(Integer thirdCategoryId) {
        this.thirdCategoryId = thirdCategoryId;
    }

    public Integer getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(Integer riskLevel) {
        this.riskLevel = riskLevel;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getHandleResult() {
        return handleResult;
    }

    public void setHandleResult(String handleResult) {
        this.handleResult = handleResult;
    }

    public LocalDateTime getHandleTime() {
        return handleTime;
    }

    public void setHandleTime(LocalDateTime handleTime) {
        this.handleTime = handleTime;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }
}
