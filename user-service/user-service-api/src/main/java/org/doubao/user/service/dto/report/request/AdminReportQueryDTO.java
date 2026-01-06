package org.doubao.user.service.dto.report.request;



/**
 * 管理员举报记录查询请求DTO
 */
//@ApiModel(value = "管理员举报记录查询请求参数")
public class AdminReportQueryDTO extends ReportQueryDTO {

	//@ApiModelProperty(value = "一级分类ID")
	private Integer firstCategoryId;

	//@ApiModelProperty(value = "二级分类ID")
	private Integer secondCategoryId;

	//@ApiModelProperty(value = "三级分类ID")
	private Integer thirdCategoryId;

	//@ApiModelProperty(value = "AI预检测风险等级：0=正常，1=低风险，2=高风险")
	private Integer riskLevel;

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
}
