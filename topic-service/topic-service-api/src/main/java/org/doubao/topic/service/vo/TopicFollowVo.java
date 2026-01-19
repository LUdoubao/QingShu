package org.doubao.topic.service.vo;

public class TopicFollowVo {
	private Long topicId;
	private Long userId;
	private Boolean isFollowed;

	public Long getTopicId() {
		return topicId;
	}

	public void setTopicId(Long topicId) {
		this.topicId = topicId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Boolean getFollowed() {
		return isFollowed;
	}

	public void setFollowed(Boolean followed) {
		isFollowed = followed;
	}
}
