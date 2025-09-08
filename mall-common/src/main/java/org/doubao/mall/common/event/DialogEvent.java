package org.doubao.mall.common.event;

/**
 *  私信事件
 */
public class DialogEvent extends SystemEvent {
	private String extra;
	private String dialogContent;
	public DialogEvent() {
		super();
	}


	public DialogEvent(Long userId,
					   String targetId, String title, String content, String extra) {
		super(userId, "NEW_MESSAGE", "message", targetId, "", title);
		this.dialogContent = content;
		this.extra = extra;
	}

	public String getDialogContent() {
		return dialogContent;
	}

	public void setDialogContent(String dialogContent) {
		this.dialogContent = dialogContent;
	}

	public String getExtra() {
		return extra;
	}

	public void setExtra(String extra) {
		this.extra = extra;
	}
}
