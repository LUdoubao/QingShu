package org.doubao.user.service.dto.report.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 举报记录详情DTO
 */
@ApiModel(value = "举报记录详情数据")
public class ReportRecordDTO {

	@ApiModelProperty(value = "举报ID")
	private Long id;

	@ApiModelProperty(value = "举报人ID")
	private Long userId;

	@ApiModelProperty(value = "被举报对象类型：1=内容，2=用户，3=评论")
	private Integer reportedType;

	@ApiModelProperty(value = "被举报对象类型名称")
	private String reportedTypeName;

	@ApiModelProperty(value = "被举报对象ID")
	private String reportedId;

	@ApiModelProperty(value = "一级分类名称")
	private String firstCategoryName;

	@ApiModelProperty(value = "二级分类名称")
	private String secondCategoryName;

	@ApiModelProperty(value = "三级分类名称")
	private String thirdCategoryName;

	@ApiModelProperty(value = "AI预检测风险等级：0=正常，1=低风险，2=高风险")
	private Integer riskLevel;

	@ApiModelProperty(value = "风险等级名称")
	private String riskLevelName;

	@ApiModelProperty(value = "举报状态：0=待审核，1=审核中，2=审核通过，3=审核不通过，4=待复核")
	private Integer status;

	@ApiModelProperty(value = "举报状态描述")
	private String statusDesc;

	@ApiModelProperty(value = "处理结果描述")
	private String handleResult;

	@ApiModelProperty(value = "处理时间")
	private LocalDateTime handleTime;

	@ApiModelProperty(value = "创建时间")
	private LocalDateTime createdTime;

	@ApiModelProperty(value = "补充描述")
	private String description;

	@ApiModelProperty(value = "证据链接列表")
	private List<String> evidenceUrls;

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

	public String getReportedTypeName() {
		return reportedTypeName;
	}

	public void setReportedTypeName(String reportedTypeName) {
		this.reportedTypeName = reportedTypeName;
	}

	public String getThirdCategoryName() {
		return thirdCategoryName;
	}

	public void setThirdCategoryName(String thirdCategoryName) {
		this.thirdCategoryName = thirdCategoryName;
	}

	public String getFirstCategoryName() {
		return firstCategoryName;
	}

	public void setFirstCategoryName(String firstCategoryName) {
		this.firstCategoryName = firstCategoryName;
	}

	public String getSecondCategoryName() {
		return secondCategoryName;
	}

	public void setSecondCategoryName(String secondCategoryName) {
		this.secondCategoryName = secondCategoryName;
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

	public String getRiskLevelName() {
		return riskLevelName;
	}

	public void setRiskLevelName(String riskLevelName) {
		this.riskLevelName = riskLevelName;
	}

	public LocalDateTime getHandleTime() {
		return handleTime;
	}

	public void setHandleTime(LocalDateTime handleTime) {
		this.handleTime = handleTime;
	}

	public String getHandleResult() {
		return handleResult;
	}

	public void setHandleResult(String handleResult) {
		this.handleResult = handleResult;
	}

	public String getStatusDesc() {
		return statusDesc;
	}

	public void setStatusDesc(String statusDesc) {
		this.statusDesc = statusDesc;
	}

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

	public List<String> getEvidenceUrls() {
		return evidenceUrls;
	}

	public void setEvidenceUrls(List<String> evidenceUrls) {
		this.evidenceUrls = evidenceUrls;
	}
}