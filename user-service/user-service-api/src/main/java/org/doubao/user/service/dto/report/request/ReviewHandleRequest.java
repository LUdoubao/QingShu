package org.doubao.user.service.dto.report.request;




import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * 审核处理请求DTO
 */
//@ApiModel(value = "审核处理请求参数")
public class ReviewHandleRequest {

    //@ApiModelProperty(value = "举报ID", required = true)
    @NotNull(message = "举报ID不能为空")
    private Long reportId;

    private String reportedId;

    //@ApiModelProperty(value = "审核员ID", required = true)
    @NotNull(message = "审核员ID不能为空")
    private Long reviewerId;

    //@ApiModelProperty(value = "审核结果：1=通过（违规），2=不通过（无违规），3=打回待补充证据", required = true)
    @NotNull(message = "审核结果不能为空")
    private Integer reviewResult;

    //@ApiModelProperty(value = "审核意见（可选，最长500字）")
    @Length(max = 500, message = "审核意见不能超过500字")
    private String reviewOpinion;

    private Integer reportedType;

    public Integer getReportedType() {
        return reportedType;
    }

    public void setReportedType(Integer reportedType) {
        this.reportedType = reportedType;
    }

    public String getReportedId() {
        return reportedId;
    }

    public void setReportedId(String reportedId) {
        this.reportedId = reportedId;
    }

    public @NotNull(message = "举报ID不能为空") Long getReportId() {
        return reportId;
    }

    public void setReportId(@NotNull(message = "举报ID不能为空") Long reportId) {
        this.reportId = reportId;
    }

    public @NotNull(message = "审核员ID不能为空") Long getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(@NotNull(message = "审核员ID不能为空") Long reviewerId) {
        this.reviewerId = reviewerId;
    }

    public @NotNull(message = "审核结果不能为空") Integer getReviewResult() {
        return reviewResult;
    }

    public void setReviewResult(@NotNull(message = "审核结果不能为空") Integer reviewResult) {
        this.reviewResult = reviewResult;
    }

    public @Length(max = 500, message = "审核意见不能超过500字") String getReviewOpinion() {
        return reviewOpinion;
    }

    public void setReviewOpinion(@Length(max = 500, message = "审核意见不能超过500字") String reviewOpinion) {
        this.reviewOpinion = reviewOpinion;
    }
}
