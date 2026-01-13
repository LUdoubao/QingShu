package org.doubao.mall.common.event;

public class LikeEvent extends NotificationEvent{
	private String target;
	private String targetId;
	private String content;
	private Long operatorUserId;
	private String operatorUserName;
	private String operatorUserAvatar;

	public LikeEvent(String action, Long userId, String target, String targetId, String content,
					 Long operatorUserId, String operatorUserName, String operatorUserAvatar) {
		super("LIKE", userId, action);
		this.target = target;
		this.targetId = targetId;
		this.content = content;
		this.operatorUserId = operatorUserId;
		this.operatorUserName = operatorUserName;
		this.operatorUserAvatar = operatorUserAvatar;
	}

	public String getOperatorUserName() {
		return operatorUserName;
	}

	public void setOperatorUserName(String operatorUserName) {
		this.operatorUserName = operatorUserName;
	}

	public String getOperatorUserAvatar() {
		return operatorUserAvatar;
	}

	public void setOperatorUserAvatar(String operatorUserAvatar) {
		this.operatorUserAvatar = operatorUserAvatar;
	}

	public Long getOperatorUserId() {
		return operatorUserId;
	}

	public void setOperatorUserId(Long operatorUserId) {
		this.operatorUserId = operatorUserId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getTargetId() {
		return targetId;
	}

	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}
}
