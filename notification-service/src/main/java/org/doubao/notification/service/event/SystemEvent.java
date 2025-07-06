package org.doubao.notification.service.event;


import java.time.LocalDateTime;

/**
 * 系统事件：表示系统操作成功/失败的事件
 */
public class SystemEvent extends NotificationEvent {
	private String action;         // 系统操作类型 (QUOTE_CREATED, QUOTE_UPDATED, etc.)
	private String target;         // 操作目标对象 (引文, 用户, 评论等)
	private Long targetId;         // 目标对象ID
	private String result;         // 操作结果 (SUCCESS, FAILURE, etc.)
	private String details;        // 操作详情
	private LocalDateTime actionTime; // 操作时间

	// 构造函数
	public SystemEvent() {
		super("SYSTEM");
	}

	public SystemEvent(Long userId, String action, String target,
					   Long targetId, String result, String details) {
		super("SYSTEM", userId);
		this.action = action;
		this.target = target;
		this.targetId = targetId;
		this.result = result;
		this.details = details;
		this.actionTime = LocalDateTime.now();
	}

	// Getters and Setters
	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public Long getTargetId() {
		return targetId;
	}

	public void setTargetId(Long targetId) {
		this.targetId = targetId;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public LocalDateTime getActionTime() {
		return actionTime;
	}

	public void setActionTime(LocalDateTime actionTime) {
		this.actionTime = actionTime;
	}

	@Override
	public String toString() {
		return "SystemEvent{" +
				"action='" + action + '\'' +
				", target='" + target + '\'' +
				", targetId=" + targetId +
				", result='" + result + '\'' +
				", details='" + details + '\'' +
				", actionTime=" + actionTime +
				"} " + super.toString();
	}
}