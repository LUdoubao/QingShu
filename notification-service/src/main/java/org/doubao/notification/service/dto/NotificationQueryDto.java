package org.doubao.notification.service.dto;

public class NotificationQueryDto {
	/**
	 * 用户id
	 */
	private Long userId;
	/**
	 * 状态
	 */
	private String status;
	/**
	 * 页码
	 */
	private int page;
	/**
	 * 每页数量
	 */
	private int size;
	/**
	 *  类型
	 */
	private String type;
	/**
	 * 动作
	 */
	private String action;


	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}

