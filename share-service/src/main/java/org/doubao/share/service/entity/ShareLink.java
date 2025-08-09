package org.doubao.share.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import org.doubao.mall.common.entity.BaseEntity;

import java.time.LocalDateTime;

@TableName("share_link")
public class ShareLink extends BaseEntity {

	@TableId(type = IdType.AUTO)
	private Long id;

	@TableField("quote_id")
	private String quoteId;

	@TableField("share_type")
	private Integer shareType; // 1:原创, 2:非原创

	@TableField("access_control")
	private Integer accessControl; // 1:公开, 2:密码, 3:指定用户

	@TableField("encrypt_password")
	private String encryptPassword;

	@TableField("authorized_users")
	private String authorizedUsers; // JSON格式存储用户ID列表

	@TableField("share_url")
	private String shareUrl;

	@TableField("expire_time")
	private LocalDateTime expireTime;

	@TableField("status")
	private Integer status; // 1:有效, 0:无效

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getShareType() {
		return shareType;
	}

	public void setShareType(Integer shareType) {
		this.shareType = shareType;
	}

	public String getQuoteId() {
		return quoteId;
	}

	public void setQuoteId(String quoteId) {
		this.quoteId = quoteId;
	}

	public Integer getAccessControl() {
		return accessControl;
	}

	public void setAccessControl(Integer accessControl) {
		this.accessControl = accessControl;
	}

	public String getEncryptPassword() {
		return encryptPassword;
	}

	public void setEncryptPassword(String encryptPassword) {
		this.encryptPassword = encryptPassword;
	}

	public String getAuthorizedUsers() {
		return authorizedUsers;
	}

	public void setAuthorizedUsers(String authorizedUsers) {
		this.authorizedUsers = authorizedUsers;
	}

	public String getShareUrl() {
		return shareUrl;
	}

	public void setShareUrl(String shareUrl) {
		this.shareUrl = shareUrl;
	}

	public LocalDateTime getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(LocalDateTime expireTime) {
		this.expireTime = expireTime;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
}
