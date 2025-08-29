package org.doubao.mall.common.event;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 审核事件：表示引文审核结果的事件
 */
public class AuditQuoteEvent extends NotificationEvent {
	private Long quoteId;        // 引文ID
	private String quoteContent;   // 引文内容（摘要）
	private String status;          // 审核状态 (APPROVED, REJECTED, etc.)
	private String reason;          // 审核不通过原因
	private String submitterName;   // 提交者名称
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
	private LocalDateTime actionTime; // 审核时间
	private String target = "quote";
	private String action = "QUOTE_VERIFY";
	// 构造函数
	public AuditQuoteEvent() {
		super("AUDIT_QUOTE");
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public AuditQuoteEvent(Long userId, Long quoteId, String quoteContent, String status,
						   String reason, String submitterName) {
		super("AUDIT_QUOTE", userId);
		this.quoteId = quoteId;
		this.quoteContent = quoteContent;
		this.status = status;
		this.reason = reason;
		this.submitterName = submitterName;
		this.actionTime = LocalDateTime.now();
	}

	// Getters and Setters
	public Long getQuoteId() {
		return quoteId;
	}

	public void setQuoteId(Long quoteId) {
		this.quoteId = quoteId;
	}

	public String getQuoteContent() {
		return quoteContent;
	}

	public void setQuoteContent(String quoteContent) {
		this.quoteContent = quoteContent;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getSubmitterName() {
		return submitterName;
	}

	public void setSubmitterName(String submitterName) {
		this.submitterName = submitterName;
	}

	public LocalDateTime getActionTime() {
		return actionTime;
	}

	public void setActionTime(LocalDateTime actionTime) {
		this.actionTime = actionTime;
	}

	@Override
	public String toString() {
		return "AuditQuoteEvent{" +
				"quoteId=" + quoteId +
				", quoteContent='" + quoteContent + '\'' +
				", status='" + status + '\'' +
				", reason='" + reason + '\'' +
				", submitterName='" + submitterName + '\'' +
				", actionTime=" + actionTime +
				"} " + super.toString();
	}
}