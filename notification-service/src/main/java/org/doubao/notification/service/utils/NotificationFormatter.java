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
		content.put("result", event.getResult());
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
		content.put("quoteCreatedId", event.getQuoteCreatedId());
		notification.setContent(JSON.toJSONString(content));
		return notification;
	}

	public Notification formatLikeNotification(LikeEvent event) {
		Notification notification = new Notification();
		notification.setUserId(event.getUserId());
		notification.setType(event.getType());
		notification.setAction(event.getAction());
		Map<String, Object> content = new HashMap<>();
		content.put("content", event.getContent());
		content.put("operatorUserId", event.getOperatorUserId());
		content.put("operatorUserName", event.getOperatorUserName());
		content.put("operatorUserAvatar", event.getOperatorUserAvatar());

		notification.setContent(JSON.toJSONString(content));
		notification.setSourceId(event.getTargetId());
		notification.setSourceType(String.valueOf(event.getTarget()));
		return notification;
	}

	public Notification formatCommentNotification(CommentEvent event) {
		Notification notification = new Notification();
		notification.setUserId(event.getUserId());
		notification.setType(event.getType());
		notification.setAction(event.getAction());
		Map<String, Object> content = new HashMap<>();
		content.put("commentType", event.getCommentType());
		content.put("content", event.getContent());
		content.put("commentContent", event.getCommentContent());
		content.put("replyContent", event.getReplyContent());
		content.put("operatorUserId", event.getOperatorUserId());
		content.put("operatorUserName", event.getOperatorUserName());
		content.put("operatorUserAvatar", event.getOperatorUserAvatar());

		notification.setContent(JSON.toJSONString(content));
		notification.setSourceId(event.getTargetId());
		notification.setSourceType(event.getTarget());
		return notification;
	}

	public Notification formatNewMessageNotification(DialogEvent newMessageEvent) {
		Notification notification = new Notification();
		notification.setUserId(newMessageEvent.getUserId());
		notification.setType(newMessageEvent.getType());
		notification.setAction(newMessageEvent.getAction());
		Map<String, Object> content = new HashMap<>();
		content.put("dialogContent", newMessageEvent.getDialogContent());
		String extra = newMessageEvent.getExtra();
		if (extra != null && !extra.isEmpty()) {
			content.put("extra", JSON.parseObject(extra));
		}
		notification.setContent(JSON.toJSONString(content));
		notification.setSourceId(newMessageEvent.getTargetId());
		notification.setSourceType(newMessageEvent.getTarget());
		return notification;
	}
}
