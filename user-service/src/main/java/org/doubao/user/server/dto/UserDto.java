package org.doubao.user.server.dto;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;

public class UserDto {
	private String username;
	@Length(min = 6, max = 20)
	private String password;
	@Email
	private String email;
	private String code;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public UserDto() {
	}

	public UserDto(String username, String password, String email, String code) {
		this.username = username;
		this.password = password;
		this.email = email;
		this.code = code;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public @Email String getEmail() {
		return email;
	}

	public void setEmail(@Email String email) {
		this.email = email;
	}
}
