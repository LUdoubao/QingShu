package org.doubao.user.server.vo;

import org.doubao.user.server.entity.User;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

public class UserVo {
	private Long id;
	private String username;
	private String nickname;
	private String email;
	private String signature;
	private String avatarUrl;
	private LocalDateTime createdTime;
	private String token;
	private String role;

	// 排除敏感字段
	public static UserVo from(User user) {
		UserVo vo = new UserVo();
		BeanUtils.copyProperties(user, vo);
		return vo;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public LocalDateTime getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(LocalDateTime createdTime) {
		this.createdTime = createdTime;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}
}
