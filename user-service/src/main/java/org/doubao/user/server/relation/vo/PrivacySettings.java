package org.doubao.user.server.relation.vo;

public class PrivacySettings {
	private Integer followerVisibility;
	private Integer followingVisibility;
	private Integer profileVisibility;
	private Integer workVisibility;

	public Integer getWorkVisibility() {
		return workVisibility;
	}

	public void setWorkVisibility(Integer workVisibility) {
		this.workVisibility = workVisibility;
	}

	public Integer getProfileVisibility() {
		return profileVisibility;
	}

	public void setProfileVisibility(Integer profileVisibility) {
		this.profileVisibility = profileVisibility;
	}

	public Integer getFollowerVisibility() {
		return followerVisibility;
	}

	public void setFollowerVisibility(Integer followerVisibility) {
		this.followerVisibility = followerVisibility;
	}

	public Integer getFollowingVisibility() {
		return followingVisibility;
	}

	public void setFollowingVisibility(Integer followingVisibility) {
		this.followingVisibility = followingVisibility;
	}

	public static class SeeAccessType {
		public static final int FOLLOWERS = 1;
		public static final int FOLLOWING = 2;
		public static final int PROFILE = 3;
		public static final int WORK = 4;
	}
}
