package org.doubao.mall.common.event;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 引文审核结果事件
 */
public class AuditQuoteEvent extends SystemEvent {
	private String quoteContent;   // 引文内容（摘要）
	private String reason;         // 审核不通过原因
	private String submitterName;   // 提交者名称
	public AuditQuoteEvent(Long userId,
						   Long targetId, String result, String quoteContent, String reason, String submitterName) {
		super(userId, "AUDIT_QUOTE", "quote", targetId, result, "引文审核结果");
		this.quoteContent = quoteContent;
		this.reason = reason;
		this.submitterName = submitterName;
	}

	public String getQuoteContent() {
		return quoteContent;
	}

	public void setQuoteContent(String quoteContent) {
		this.quoteContent = quoteContent;
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
}