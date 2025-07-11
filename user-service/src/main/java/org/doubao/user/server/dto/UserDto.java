package org.doubao.user.server.dto;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;

public class UserDto {
	private String username;
	@Length(min = 6, max = 20)
	private String password;
	@Email
	private String email;

	public UserDto(@Email String email, String s, String s1) {

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
