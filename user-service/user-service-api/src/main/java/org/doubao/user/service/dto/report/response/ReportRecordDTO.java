package org.doubao.user.service.dto.report.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 举报记录详情DTO
 */
public class ReportRecordDTO {

	private Long id;

	private Long userId;

	private Integer reportedType;

	private String reportedTypeName;

	private String reportedId;

	private String firstCategoryName;

	private String secondCategoryName;

	private String thirdCategoryName;

	private Integer riskLevel;

	private String riskLevelName;

	private Integer status;

	private String statusDesc;

	private String handleResult;

	private LocalDateTime handleTime;

	private LocalDateTime createdTime;

	private String description;

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