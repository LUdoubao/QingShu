package org.doubao.user.service.dto.core;

public class UserInfoProfileEdit extends UserInfoProfile{
	private String signature;

	public UserInfoProfileEdit(Long id, String nickname, String username) {
		super(id, nickname, username);
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}
}
