package org.doubao.mall.common.event;


import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 系统事件：表示系统操作成功/失败的事件
 */
public class SystemEvent extends NotificationEvent {
	private String title;
	private String target;         // 操作目标对象 (引文, 用户, 评论等)
	private Long targetId;       // 目标对象ID
	private String result;       // 操作结果 (SUCCESS, FAILURE, etc.)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
	private LocalDateTime actionTime; // 操作时间

	public SystemEvent(Long userId, String action, String target,
					   Long targetId, String result, String title) {
		super("SYSTEM", userId, action);
		this.target = target;
		this.targetId = targetId;
		this.result = result;
		this.title = title;
		this.actionTime = LocalDateTime.now();
	}

	// Getters and Setters
	public String getTarget() {
		return target;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
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



	public LocalDateTime getActionTime() {
		return actionTime;
	}

	public void setActionTime(LocalDateTime actionTime) {
		this.actionTime = actionTime;
	}

	@Override
	public String toString() {
		return "SystemEvent{" +
				", target='" + target + '\'' +
				", targetId=" + targetId +
				", result='" + result + '\'' +
				", actionTime=" + actionTime +
				"} " + super.toString();
	}
}