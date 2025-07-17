package org.doubao.notification.service.factory;



import org.doubao.mall.common.event.AuditEvent;
import org.doubao.mall.common.event.SystemEvent;

import java.time.LocalDateTime;

public class EventFactory {

	// 创建审核事件
	public static AuditEvent createAuditEvent(Long userId, Long quoteId, String quoteContent,
											  String status, String reason, String submitterName) {
		AuditEvent event = new AuditEvent();
		event.setType("AUDIT");
		event.setUserId(userId);
		event.setQuoteId(quoteId);
		event.setQuoteContent(quoteContent);
		event.setStatus(status);
		event.setReason(reason);
		event.setSubmitterName(submitterName);
		event.setActionTime(LocalDateTime.now());
		return event;
	}

	// 创建系统事件
	public static SystemEvent createSystemEvent(Long userId, String action, String target,
												Long targetId, String result, String details) {
		SystemEvent event = new SystemEvent();
		event.setType("SYSTEM");
		event.setUserId(userId);
		event.setAction(action);
		event.setTarget(target);
		event.setTargetId(targetId);
		event.setResult(result);
		event.setDetails(details);
		event.setActionTime(LocalDateTime.now());
		return event;
	}

	// 创建审批成功事件
	public static AuditEvent createApprovedAuditEvent(Long userId, Long quoteId, String quoteContent, String submitterName) {
		return createAuditEvent(userId, quoteId, quoteContent, "APPROVED", null, submitterName);
	}

	// 创建审批拒绝事件
	public static AuditEvent createRejectedAuditEvent(Long userId, Long quoteId, String quoteContent, String reason, String submitterName) {
		return createAuditEvent(userId, quoteId, quoteContent, "REJECTED", reason, submitterName);
	}

	// 创建操作成功事件
	public static SystemEvent createSuccessSystemEvent(Long userId, String action, String target, Long targetId) {
		return createSystemEvent(userId, action, target, targetId, "SUCCESS", null);
	}

	// 创建操作失败事件
	public static SystemEvent createFailedSystemEvent(Long userId, String action, String target, Long targetId, String reason) {
		return createSystemEvent(userId, action, target, targetId, "FAILURE", reason);
	}
}