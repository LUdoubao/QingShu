// report/dto/response/ReportPageResponse.java
package org.doubao.user.server.report.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.List;

/**
 * 举报记录分页响应DTO
 */
@ApiModel(value = "举报记录分页响应数据")
public class ReportPageResponse {

	@ApiModelProperty(value = "总条数")
	private Long total;

	@ApiModelProperty(value = "总页数")
	private Integer totalPages;

	@ApiModelProperty(value = "当前页码")
	private Integer pageNum;

	@ApiModelProperty(value = "每页条数")
	private Integer pageSize;

	@ApiModelProperty(value = "举报记录列表")
	private List<ReportRecordDTO> records;

	public Long getTotal() {
		return total;
	}

	public void setTotal(Long total) {
		this.total = total;
	}

	public Integer getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(Integer totalPages) {
		this.totalPages = totalPages;
	}

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

	public List<ReportRecordDTO> getRecords() {
		return records;
	}

	public void setRecords(List<ReportRecordDTO> records) {
		this.records = records;
	}
}