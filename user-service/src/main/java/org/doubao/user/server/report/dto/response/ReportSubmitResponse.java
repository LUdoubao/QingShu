package org.doubao.user.server.report.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 举报提交响应DTO
 */
@ApiModel(value = "举报提交响应数据")
public class ReportSubmitResponse {

    @ApiModelProperty(value = "举报ID")
    private Long reportId;

    @ApiModelProperty(value = "举报状态：0=待审核")
    private Integer status;

    @ApiModelProperty(value = "提示信息")
    private String tips;

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

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }
}
