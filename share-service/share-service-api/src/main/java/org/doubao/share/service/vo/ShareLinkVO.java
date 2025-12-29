package org.doubao.share.service.vo;

import java.time.LocalDateTime;

public class ShareLinkVO {
	private String shareLink;
	private LocalDateTime expireTime;

	public String getShareLink() {
		return shareLink;
	}

	public void setShareLink(String shareLink) {
		this.shareLink = shareLink;
	}

	public LocalDateTime getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(LocalDateTime expireTime) {
		this.expireTime = expireTime;
	}
}