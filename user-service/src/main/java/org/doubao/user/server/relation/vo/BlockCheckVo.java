package org.doubao.user.server.relation.vo;

public class BlockCheckVo {
	private boolean isBlocked;
	private Long targetUserId;

	public boolean isBlocked() {
		return isBlocked;
	}

	public void setBlocked(boolean blocked) {
		isBlocked = blocked;
	}

	public Long getTargetUserId() {
		return targetUserId;
	}

	public void setTargetUserId(Long targetUserId) {
		this.targetUserId = targetUserId;
	}
}
