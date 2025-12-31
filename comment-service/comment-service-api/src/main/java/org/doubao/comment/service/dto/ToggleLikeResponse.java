package org.doubao.comment.service.dto;

/**
 * 点赞服务返回结果的适配类
 * <p>
 * 用于封装点赞/取消点赞操作的返回结果
 * 与点赞服务的ToggleLikeResponse字段保持一致
 */
public class ToggleLikeResponse {
	/**
	 * 操作是否成功
	 * <p>
	 * true表示点赞/取消点赞操作执行成功，false表示操作失败
	 */
	private boolean success;
	/**
	 * 操作类型
	 * <p>
	 * "LIKE"表示点赞操作，"CANCEL"表示取消点赞操作
	 */
	private String action;
	/**
	 * 当前点赞总数
	 * <p>
	 * 执行操作后该实体的最新点赞总数
	 */
	private Long currentCount;
	/**
	 * 返回消息
	 * <p>
	 * 操作执行结果的描述信息
	 */
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