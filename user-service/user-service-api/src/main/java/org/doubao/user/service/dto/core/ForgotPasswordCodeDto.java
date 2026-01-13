package org.doubao.user.service.dto.core;

import javax.validation.Valid;

public class ForgotPasswordCodeDto {
	private String email;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
