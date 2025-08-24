package org.doubao.user.server.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.doubao.mall.common.entity.BaseDel;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@TableName("user")
public class User extends BaseDel {
	@TableId(value = "id", type = IdType.AUTO)
	private Long id;
	@TableField(value = "username")
	@NotEmpty(message = "用户名不能为空")
	private String username;
	@TableField(value = "nickname")
	private String nickname;
	@TableField(value = "password")
	@NotEmpty(message = "密码不能为空")
	private String password;
	@TableField(value = "email")
	@Email(message = "邮箱格式不正确")
	private String email;
	@TableField(value = "signature")
	private String signature;
	@TableField(value = "avatar_key")
	private String avatarKey;
	@TableField(value = "storage_type")
	private String storageType = "local"; // 默认本地存储
	@TableField(value = "status")
	private int status = 0;
	@TableField(value = "role")
	private String role =  "USER";

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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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

	public String getAvatarKey() {
		return avatarKey;
	}

	public void setAvatarKey(String avatarKey) {
		this.avatarKey = avatarKey;
	}

	public String getStorageType() {
		return storageType;
	}

	public void setStorageType(String storageType) {
		this.storageType = storageType;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
}
