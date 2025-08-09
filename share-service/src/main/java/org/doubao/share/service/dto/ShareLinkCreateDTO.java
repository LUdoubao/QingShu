package org.doubao.share.service.dto;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public class ShareLinkCreateDTO {
	private String quoteId;
	private String shareType; // ORIGINAL, NON_ORIGINAL
	private String accessControl; // PUBLIC, PASSWORD, SPECIFIED
	private String password;
	private List<String> authorizedUserIds;
	private LocalDateTime expireTime;

	public String getQuoteId() {
		return quoteId;
	}

	public void setQuoteId(String quoteId) {
		this.quoteId = quoteId;
	}

	public String getShareType() {
		return shareType;
	}

	public void setShareType(String shareType) {
		this.shareType = shareType;
	}

	public String getAccessControl() {
		return accessControl;
	}

	public void setAccessControl(String accessControl) {
		this.accessControl = accessControl;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public List<String> getAuthorizedUserIds() {
		return authorizedUserIds;
	}

	public void setAuthorizedUserIds(List<String> authorizedUserIds) {
		this.authorizedUserIds = authorizedUserIds;
	}

	public LocalDateTime getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(LocalDateTime expireTime) {
		this.expireTime = expireTime;
	}
}
