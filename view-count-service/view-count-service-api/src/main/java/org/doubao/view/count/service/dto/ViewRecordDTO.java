package org.doubao.view.count.service.dto;


/**
 * 浏览记录数据传输对象
 * 用于接收前端传递的浏览行为相关参数
 */
public class ViewRecordDTO {
	/**
	 * 文案ID
	 */
	private Long contentId;

	/**
	 * 是否来自列表页
	 */
	private Boolean fromList;

	/**
	 * 浏览时长(秒)
	 */
	private Integer viewDuration;

	/**
	 * 是否有交互行为
	 */
	private Boolean hasInteraction;

	/**
	 * 是否为作者本人浏览
	 */
	private Boolean author;

	/**
	 * IP地址
	 */
	private String ipAddress;

	/**
	 * 用户代理信息（浏览器/设备信息）
	 */
	private String userAgent;

	/**
	 * 用户标识（登录用户为user_xxx，游客为device_xxx）
	 */
	private String userIdentity;

	public Long getContentId() {
		return contentId;
	}

	public void setContentId(Long contentId) {
		this.contentId = contentId;
	}


	public Integer getViewDuration() {
		return viewDuration;
	}

	public void setViewDuration(Integer viewDuration) {
		this.viewDuration = viewDuration;
	}

	public Boolean getHasInteraction() {
		return hasInteraction;
	}

	public void setHasInteraction(Boolean hasInteraction) {
		this.hasInteraction = hasInteraction;
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

	public String getUserIdentity() {
		return userIdentity;
	}

	public void setUserIdentity(String userIdentity) {
		this.userIdentity = userIdentity;
	}

	public Boolean getFromList() {
		return fromList;
	}

	public void setFromList(Boolean fromList) {
		this.fromList = fromList;
	}

	public Boolean getAuthor() {
		return author;
	}

	public void setAuthor(Boolean author) {
		this.author = author;
	}
}