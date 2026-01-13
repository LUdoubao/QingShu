package org.doubao.user.service.dto.core;

public class ForgotPasswordVerifyDto {
	private String code;
	private String email;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
