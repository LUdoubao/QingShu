package org.doubao.dialog.service.vo;


import java.time.LocalDate;

/**
 * 对话查询视图对象
 */
public class DialogQuery {

	/**
	 * 状态：0-活跃 1-已解决 2-待跟进
	 */
	private Integer status;

	/**
	 * 用户ID
	 */
	private Long userId;

	/**
	 * 开始时间
	 */
	private LocalDate startTime;

	/**
	 * 结束时间
	 */
	private LocalDate endTime;

	/**
	 * 关键词搜索
	 */
	private String keyword;

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public LocalDate getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalDate startTime) {
		this.startTime = startTime;
	}

	public LocalDate getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalDate endTime) {
		this.endTime = endTime;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
}