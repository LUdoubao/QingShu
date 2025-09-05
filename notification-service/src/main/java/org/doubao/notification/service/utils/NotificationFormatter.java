package org.doubao.notification.service.utils;

import com.alibaba.fastjson.JSON;
import org.doubao.mall.common.event.*;
import org.doubao.notification.service.entity.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class NotificationFormatter {

	private static final Logger LOGGER = LoggerFactory.getLogger(NotificationFormatter.class);
	public Notification formatSystemNotification(AuditQuoteEvent event) {

		Notification notification = new Notification();
		notification.setUserId(event.getUserId());
		notification.setType(event.getType());
		notification.setTitle(event.getTitle());
		notification.setAction(event.getAction());
		notification.setSourceId(String.valueOf(event.getTargetId()));
		notification.setSourceType(event.getTarget());

		Map<String, Object> content = new HashMap<>();
		content.put("quoteContent", event.getQuoteContent());
		content.put("reason", event.getReason());
		content.put("submitterName", event.getSubmitterName());
		notification.setContent(JSON.toJSONString(content));

		return notification;
	}
	public Notification formatSystemNotification(VerifyQuoteEvent event) {

		Notification notification = new Notification();
		notification.setUserId(event.getUserId());
		notification.setType(event.getType());
		notification.setTitle(event.getTitle());
		notification.setAction(event.getAction());
		notification.setSourceId(String.valueOf(event.getTargetId()));
		notification.setSourceType(event.getTarget());

		Map<String, Object> content = new HashMap<>();
		content.put("quoteContent", event.getQuoteContent());
		notification.setContent(JSON.toJSONString(content));
		return notification;
	}

	public Notification formatLikeNotification(LikeEvent event) {
		Notification notification = new Notification();
		notification.setUserId(event.getUserId());
		notification.setType("LIKE");
		notification.setTitle("点赞通知");
		Map<String, Object> content = new HashMap<>();
		content.put("target", "like");
		content.put("action", event.isLike());
		content.put("entityType", event.getEntityType());
		content.put("content", event.getContent());
		content.put("id", event.getEntityId());
		content.put("userName", event.getOperatorUserName());

		notification.setContent(JSON.toJSONString(content));
		notification.setSourceId(event.getEntityId());
		notification.setSourceType(String.valueOf(event.getEntityType()));
		return notification;
	}

	public Notification formatCommentNotification(CommentEvent event) {
		Notification notification = new Notification();
		notification.setUserId(event.getUserId());
		notification.setType("COMMENT");
		notification.setTitle(event.isComment() ? "评论通知" : "回复通知");
		Map<String, Object> content = new HashMap<>();
		content.put("target", "comment");
		content.put("action", event.isComment());
		content.put("content", event.getContent());
		content.put("commentContent", event.getCommentContent());
		content.put("id", event.getQuoteId());
		content.put("userName", event.getOperatorUserName());

		notification.setContent(JSON.toJSONString(content));
		notification.setSourceId(event.getQuoteId());
		notification.setSourceType("1");
		return notification;
	}
}
