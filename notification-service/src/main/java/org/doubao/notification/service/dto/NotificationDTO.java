package org.doubao.notification.service.dto;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;
import org.doubao.notification.service.entity.Notification;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class NotificationDTO implements Serializable {
	private Long id;
	private Long userId;
	private String type;
	private String title;
	private String action;
	private Map<String, Object> content;  // 将JSON内容解析为Map
	private int status;
	private String sourceId;
	private String sourceType;
	private String createdTime;
	private String updatedTime;


	// 从实体类转换
	public static NotificationDTO fromEntity(Notification notification) {
		NotificationDTO dto = new NotificationDTO();
		dto.setId(notification.getId());
		dto.setUserId(notification.getUserId());
		dto.setType(notification.getType());
		dto.setTitle(notification.getTitle());
		dto.setStatus(notification.getStatus());
		dto.setSourceId(notification.getSourceId());
		dto.setSourceType(notification.getSourceType());
		dto.setAction(notification.getAction());

		// 解析JSON内容
		if (StringUtils.isNotBlank(notification.getContent())) {
			dto.setContent(JSON.parseObject(notification.getContent(), Map.class));
		}

		// 格式化时间
		dto.setCreatedTime(formatDateTime(notification.getCreatedTime()));
		dto.setUpdatedTime(formatDateTime(notification.getUpdatedTime()));

		return dto;
	}

	private static String formatDateTime(LocalDateTime dateTime) {
		if (dateTime == null) return null;
		return dateTime.format(DateTimeFormatter.ISO_DATE_TIME);
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Map<String, Object> getContent() {
		return content;
	}

	public void setContent(Map<String, Object> content) {
		this.content = content;
	}


	public String getSourceId() {
		return sourceId;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	public String getSourceType() {
		return sourceType;
	}

	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	public String getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(String createdTime) {
		this.createdTime = createdTime;
	}

	public String getUpdatedTime() {
		return updatedTime;
	}

	public void setUpdatedTime(String updatedTime) {
		this.updatedTime = updatedTime;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
}
