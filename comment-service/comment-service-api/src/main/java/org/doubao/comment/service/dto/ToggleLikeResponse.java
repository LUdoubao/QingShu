package org.doubao.comment.service.dto;

// 点赞服务返回结果的适配类（与点赞服务的ToggleLikeResponse字段一致）
public class ToggleLikeResponse {
	private boolean success;
	private String action;  // "LIKE"或"CANCEL"
	private Long currentCount;  // 当前点赞总数
	private String message;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public Long getCurrentCount() {
		return currentCount;
	}

	public void setCurrentCount(Long currentCount) {
		this.currentCount = currentCount;
	}
}