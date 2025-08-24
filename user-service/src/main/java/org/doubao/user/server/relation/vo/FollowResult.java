package org.doubao.user.server.relation.vo;

public class FollowResult {
	private Long targetUserId;
	private Boolean isFollow;

	public Long getTargetUserId() {
		return targetUserId;
	}

	public void setTargetUserId(Long targetUserId) {
		this.targetUserId = targetUserId;
	}

	public Boolean getFollow() {
		return isFollow;
	}

	public void setFollow(Boolean follow) {
		isFollow = follow;
	}
}