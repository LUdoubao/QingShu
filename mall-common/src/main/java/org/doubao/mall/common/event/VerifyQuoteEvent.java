package org.doubao.mall.common.event;

/**
 * 引文审核事件 （管理员专属）
 */
public class VerifyQuoteEvent extends SystemEvent {
	private String quoteContent;   // 引文内容（摘要）
	private Long quoteCreatedId;

	public VerifyQuoteEvent(Long userId,
						   Long targetId, String quoteContent, Long quoteCreatedId) {
		super(userId, "VERIFY_QUOTE", "quote", targetId, "", "新引文待审核");
		this.quoteContent = quoteContent;
		this.quoteCreatedId = quoteCreatedId;
	}

	public String getQuoteContent() {
		return quoteContent;
	}

	public void setQuoteContent(String quoteContent) {
		this.quoteContent = quoteContent;
	}

	public Long getQuoteCreatedId() {
		return quoteCreatedId;
	}

	public void setQuoteCreatedId(Long quoteCreatedId) {
		this.quoteCreatedId = quoteCreatedId;
	}
}
