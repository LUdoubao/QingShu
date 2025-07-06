package org.doubao.notification.service.dto;

public class UnreadCountDTO {
	private Integer unreadCount;

	public UnreadCountDTO(Integer count) {
		this.unreadCount = count;
	}

	public Integer getUnreadCount() {
		return unreadCount;
	}

	public void setUnreadCount(Integer unreadCount) {
		this.unreadCount = unreadCount;
	}
}