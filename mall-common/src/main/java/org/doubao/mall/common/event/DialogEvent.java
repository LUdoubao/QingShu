package org.doubao.mall.common.event;

/**
 *  私信事件(用于APP通知等)
 */
public class DialogEvent extends NotificationEvent {
	/** 私信目标 */
	private String target;
	/** 私信目标ID */
	private String targetId;
	/** 私信内容 */
	private String content;
	/** 发送私信的用户ID */
	private Long operatorUserId;
	/** 私信发送用户名称 */
	private String operatorUserName;
	/** 私信发送用户头像 */
	private String operatorUserAvatar;
	/** 额外信息 */
	private String extra;
	/** 私信标题 */
	private String title;

	public DialogEvent(String action, Long userId, String target, String targetId, String content,
					 Long operatorUserId, String operatorUserName, String operatorUserAvatar, String extra, String title) {
		super("CHAT", userId, action);
		this.target = target;
		this.targetId = targetId;
		this.content = content;
		this.operatorUserId = operatorUserId;
		this.operatorUserName = operatorUserName;
		this.operatorUserAvatar = operatorUserAvatar;
		this.extra = extra;
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
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

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Long getOperatorUserId() {
		return operatorUserId;
	}

	public void setOperatorUserId(Long operatorUserId) {
		this.operatorUserId = operatorUserId;
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

	public String getExtra() {
		return extra;
	}

	public void setExtra(String extra) {
		this.extra = extra;
	}
}
