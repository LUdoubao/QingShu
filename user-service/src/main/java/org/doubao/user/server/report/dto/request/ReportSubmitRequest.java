package org.doubao.user.server.report.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 举报提交请求DTO
 */
@ApiModel(value = "举报提交请求参数")
public class ReportSubmitRequest {

    @ApiModelProperty(value = "被举报对象类型：1=内容，2=用户，3=评论", required = true)
    @NotNull(message = "被举报对象类型不能为空")
    private Integer reportedType;

    @ApiModelProperty(value = "被举报对象ID", required = true)
    @NotNull(message = "被举报对象ID不能为空")
    private Long reportedId;

    @ApiModelProperty(value = "一级分类ID", required = true)
    @NotNull(message = "一级分类ID不能为空")
    private Integer firstCategoryId;

    @ApiModelProperty(value = "二级分类ID", required = true)
    @NotNull(message = "二级分类ID不能为空")
    private Integer secondCategoryId;

    @ApiModelProperty(value = "三级分类ID（可选）")
    private Integer thirdCategoryId;

    @ApiModelProperty(value = "补充描述（可选，最长500字）")
    @Length(max = 500, message = "补充描述不能超过500字")
    private String description;

    @ApiModelProperty(value = "证据链接列表（可选）")
    private List<String> evidenceUrls;

    public @NotNull(message = "一级分类ID不能为空") Integer getFirstCategoryId() {
        return firstCategoryId;
    }

    public void setFirstCategoryId(@NotNull(message = "一级分类ID不能为空") Integer firstCategoryId) {
        this.firstCategoryId = firstCategoryId;
    }

    public @NotNull(message = "被举报对象ID不能为空") Long getReportedId() {
        return reportedId;
    }

    public void setReportedId(@NotNull(message = "被举报对象ID不能为空") Long reportedId) {
        this.reportedId = reportedId;
    }

    public @NotNull(message = "被举报对象类型不能为空") Integer getReportedType() {
        return reportedType;
    }

    public void setReportedType(@NotNull(message = "被举报对象类型不能为空") Integer reportedType) {
        this.reportedType = reportedType;
    }

    public @NotNull(message = "二级分类ID不能为空") Integer getSecondCategoryId() {
        return secondCategoryId;
    }

    public void setSecondCategoryId(@NotNull(message = "二级分类ID不能为空") Integer secondCategoryId) {
        this.secondCategoryId = secondCategoryId;
    }

    public List<String> getEvidenceUrls() {
        return evidenceUrls;
    }

    public void setEvidenceUrls(List<String> evidenceUrls) {
        this.evidenceUrls = evidenceUrls;
    }

    public @Length(max = 500, message = "补充描述不能超过500字") String getDescription() {
        return description;
    }

    public void setDescription(@Length(max = 500, message = "补充描述不能超过500字") String description) {
        this.description = description;
    }

    public Integer getThirdCategoryId() {
        return thirdCategoryId;
    }

    public void setThirdCategoryId(Integer thirdCategoryId) {
        this.thirdCategoryId = thirdCategoryId;
    }
}
