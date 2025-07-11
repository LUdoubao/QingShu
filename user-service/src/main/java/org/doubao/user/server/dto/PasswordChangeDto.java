package org.doubao.user.server.dto;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;

public class PasswordChangeDto {
	@NotEmpty
	private String oldPassword;

	@NotEmpty
	@Length(min = 6, max = 20)
	private String newPassword;

	public @NotEmpty String getOldPassword() {
		return oldPassword;
	}

	public void setOldPassword(@NotEmpty String oldPassword) {
		this.oldPassword = oldPassword;
	}

	public @NotEmpty @Length(min = 6, max = 20) String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(@NotEmpty @Length(min = 6, max = 20) String newPassword) {
		this.newPassword = newPassword;
	}
}
