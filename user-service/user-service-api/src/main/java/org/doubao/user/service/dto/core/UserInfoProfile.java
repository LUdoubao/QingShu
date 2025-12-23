package org.doubao.user.service.dto.core;

import org.doubao.mall.common.entity.UserInfoDes;

public class UserInfoProfile extends UserInfoDes {
	private String username;
	private String bgUrl;


	public UserInfoProfile(Long id, String nickname, String username) {
		super(id, nickname);
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getBgUrl() {
		return bgUrl;
	}

	public void setBgUrl(String bgUrl) {
		this.bgUrl = bgUrl;
	}
}
