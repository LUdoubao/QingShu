package org.doubao.auth.service.dto;

import java.io.Serializable;

public class UserInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	private Long id;
	private String username;
	private String token;

	public UserInfo() {
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

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}
