package org.doubao.user.server.core.vo;

import org.doubao.user.server.core.entity.User;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

public class UserVo {
	private Long id;
	private String nickname;
	private String avatarUrl;

	// 排除敏感字段
	public static UserVo from(User user) {
		UserVo vo = new UserVo();
		BeanUtils.copyProperties(user, vo);
		return vo;
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
