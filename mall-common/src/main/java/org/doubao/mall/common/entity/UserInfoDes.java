package org.doubao.mall.common.entity;

import java.io.Serializable;

public class UserInfoDes implements Serializable {
	private static final long serialVersionUID = 1L;
	private String id;
	private String nickname;
	private String avatarUrl;

	public UserInfoDes (UserInfo userInfo) {
		this.id = userInfo.getId();
		this.nickname = userInfo.getNickname();
		this.avatarUrl = userInfo.getAvatarUrl();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}
}
