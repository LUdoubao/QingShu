package org.doubao.user.server.report.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 举报状态响应DTO
 */
@ApiModel(value = "举报状态响应数据")
public class ReportStatusResponse {

    @ApiModelProperty(value = "举报ID")
    private Long reportId;

    @ApiModelProperty(value = "举报状态：0=待审核，1=审核中，2=审核通过，3=审核不通过，4=待复核")
    private Integer status;

    @ApiModelProperty(value = "举报状态描述")
    private String statusDesc;

    @ApiModelProperty(value = "处理结果描述")
    private String handleResult;

    @ApiModelProperty(value = "处理时间")
    private String handleTime;

    @ApiModelProperty(value = "证据链接列表")
    private List<String> evidenceUrls;

    private String description;

    private LocalDateTime createdTime;

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getStatusDesc() {
        return statusDesc;
    }

    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
    }

    public String getHandleResult() {
        return handleResult;
    }

    public void setHandleResult(String handleResult) {
        this.handleResult = handleResult;
    }

    public String getHandleTime() {
        return handleTime;
    }

    public void setHandleTime(String handleTime) {
        this.handleTime = handleTime;
    }

    public List<String> getEvidenceUrls() {
        return evidenceUrls;
    }

    public void setEvidenceUrls(List<String> evidenceUrls) {
        this.evidenceUrls = evidenceUrls;
    }
}
