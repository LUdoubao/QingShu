package org.doubao.share.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;


@TableName("share_access_record")
public class ShareAccessRecord {

	@TableId(type = IdType.AUTO)
	private Long id;

	@TableField("share_link_id")
	private Long shareLinkId;

	@TableField("user_id")
	private String userId;

	@TableField("access_time")
	private LocalDateTime accessTime;

	@TableField("ip_address")
	private String ipAddress;

	@TableField("access_type")
	private Integer accessType; // 1:公开, 2:密码, 3:指定用户

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getShareLinkId() {
		return shareLinkId;
	}

	public void setShareLinkId(Long shareLinkId) {
		this.shareLinkId = shareLinkId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public LocalDateTime getAccessTime() {
		return accessTime;
	}

	public void setAccessTime(LocalDateTime accessTime) {
		this.accessTime = accessTime;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public Integer getAccessType() {
		return accessType;
	}

	public void setAccessType(Integer accessType) {
		this.accessType = accessType;
	}
}