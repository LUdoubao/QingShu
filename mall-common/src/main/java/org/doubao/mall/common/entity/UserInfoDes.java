package org.doubao.mall.common.entity;

import java.io.Serializable;

public class UserInfoDes implements Serializable {
	private static final long serialVersionUID = 1L;
	private Long id;
	private String nickname;
	private String avatarUrl;

	public UserInfoDes() {
	}

	public UserInfoDes (Long id, String nickname) {
		this.id = id;
		this.nickname = nickname;
	}


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
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
