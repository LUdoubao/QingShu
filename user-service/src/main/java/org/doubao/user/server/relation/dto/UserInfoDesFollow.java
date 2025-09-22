package org.doubao.user.server.relation.dto;

import org.doubao.mall.common.entity.UserInfoDes;

public class UserInfoDesFollow extends UserInfoDes {
	private boolean follow;

	public boolean isFollow() {
		return follow;
	}

	public void setFollow(boolean follow) {
		this.follow = follow;
	}

	public static UserInfoDesFollow fromUserInfoDes(UserInfoDes userInfoDes, boolean follow) {
		UserInfoDesFollow userInfoDesFollow = new UserInfoDesFollow();
		userInfoDesFollow.setId(userInfoDes.getId());
		userInfoDesFollow.setAvatarUrl(userInfoDes.getAvatarUrl());
		userInfoDesFollow.setNickname(userInfoDes.getNickname());
		userInfoDesFollow.setFollow(follow);
		return userInfoDesFollow;
	}
}
