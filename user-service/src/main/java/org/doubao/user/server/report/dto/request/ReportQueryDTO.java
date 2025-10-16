package org.doubao.user.server.report.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 举报查询请求DTO（管理员用）
 */
@ApiModel(value = "举报查询请求参数")
public class ReportQueryDTO {

	@ApiModelProperty(value = "页码（默认1）")
	private Integer pageNum = 1;

	@ApiModelProperty(value = "每页条数（默认10）")
	private Integer pageSize = 10;

	@ApiModelProperty(value = "举报状态：0=待审核，1=审核中，2=审核通过，3=审核不通过，4=待复核")
	private Integer status;

	@ApiModelProperty(value = "被举报对象类型：1=内容，2=用户，3=评论")
	private Integer reportedType;

	@ApiModelProperty(value = "举报人ID")
	private Long userId;

	@ApiModelProperty(value = "查询开始时间")
	private LocalDateTime startTime;

	@ApiModelProperty(value = "查询结束时间")
	private LocalDateTime endTime;

	public Integer getPageNum() {
		return pageNum;
	}

	public void setPageNum(Integer pageNum) {
		this.pageNum = pageNum;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
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

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}
}