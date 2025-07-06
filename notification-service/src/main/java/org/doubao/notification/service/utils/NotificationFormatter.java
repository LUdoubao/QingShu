package org.doubao.notification.service.utils;

import com.alibaba.fastjson.JSON;
import org.doubao.notification.service.entity.Notification;
import org.doubao.notification.service.event.AuditEvent;
import org.doubao.notification.service.event.SystemEvent;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class NotificationFormatter {

	public Notification formatAuditNotification(AuditEvent event) {
		Notification notification = new Notification();
		notification.setUserId(event.getUserId());
		notification.setType("AUDIT");
		notification.setTitle("引文审核通知");

		Map<String, Object> content = new HashMap<>();
		content.put("id", event.getQuoteId());
		content.put("status", event.getStatus());
		content.put("reason", event.getReason());
		content.put("action_time", LocalDateTime.now());
		content.put("content", event.getQuoteContent());
		content.put("target", event.getTarget());


		notification.setContent(JSON.toJSONString(content));
		notification.setSourceId(event.getQuoteId());
		notification.setSourceType("quote");

		return notification;
	}

	public Notification formatSystemNotification(SystemEvent event) {
		Notification notification = new Notification();
		notification.setUserId(event.getUserId());
		notification.setType("SYSTEM");
		notification.setTitle("系统通知");

		Map<String, Object> content = new HashMap<>();
		content.put("action", event.getAction());
		content.put("target", event.getTarget());
		content.put("id", event.getTargetId());
		content.put("result", event.getResult());

		notification.setContent(JSON.toJSONString(content));
		notification.setSourceId(event.getTargetId());
		notification.setSourceType(event.getTarget());

		return notification;
	}

}
