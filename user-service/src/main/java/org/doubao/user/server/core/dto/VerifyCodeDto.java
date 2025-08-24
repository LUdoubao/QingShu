package org.doubao.user.server.core.dto;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

public class VerifyCodeDto {
	@Email
	private String email;

	@NotEmpty
	@Length(min = 6, max = 6)
	private String code;


	public @Email String getEmail() {
		return email;
	}

	public void setEmail(@Email String email) {
		this.email = email;
	}

	public @NotEmpty @Length(min = 6, max = 6) String getCode() {
		return code;
	}

	public void setCode(@NotEmpty @Length(min = 6, max = 6) String code) {
		this.code = code;
	}
}