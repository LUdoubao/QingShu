package org.doubao.view.count.service.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;

@TableName("user_view_log")
public class UserViewLog {
	@TableId(type = IdType.AUTO)
	private Long id;

	@TableField("content_id")
	private Long contentId;

	@TableField("user_identity")
	private String userIdentity;

	@TableField("view_time")
	private LocalDateTime viewTime;

	@TableField("view_duration")
	private Integer viewDuration;

	@TableField("is_valid")
	private int isValid;

	@TableField("is_from_list")
	private int isFromList;

	@TableField("ip_address")
	private String ipAddress;

	@TableField("user_agent")
	private String userAgent;

	@TableField(value = "created_time", fill = FieldFill.INSERT)
	private LocalDateTime createdTime;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getContentId() {
		return contentId;
	}

	public void setContentId(Long contentId) {
		this.contentId = contentId;
	}

	public String getUserIdentity() {
		return userIdentity;
	}

	public void setUserIdentity(String userIdentity) {
		this.userIdentity = userIdentity;
	}

	public LocalDateTime getViewTime() {
		return viewTime;
	}

	public void setViewTime(LocalDateTime viewTime) {
		this.viewTime = viewTime;
	}

	public Integer getViewDuration() {
		return viewDuration;
	}

	public void setViewDuration(Integer viewDuration) {
		this.viewDuration = viewDuration;
	}

	public int getIsValid() {
		return isValid;
	}

	public void setIsValid(int isValid) {
		this.isValid = isValid;
	}

	public int getIsFromList() {
		return isFromList;
	}

	public void setIsFromList(int isFromList) {
		this.isFromList = isFromList;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public LocalDateTime getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(LocalDateTime createdTime) {
		this.createdTime = createdTime;
	}
}