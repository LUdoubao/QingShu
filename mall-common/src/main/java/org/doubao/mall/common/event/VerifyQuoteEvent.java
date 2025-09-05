package org.doubao.mall.common.event;

/**
 * 引文审核事件 （管理员专属）
 */
public class VerifyQuoteEvent extends SystemEvent {
	private String quoteContent;   // 引文内容（摘要）

	public VerifyQuoteEvent(Long userId,
						   Long targetId, String quoteContent) {
		super(userId, "VERIFY_QUOTE", "quote", targetId, "", "新引文待审核");
		this.quoteContent = quoteContent;
	}

	public String getQuoteContent() {
		return quoteContent;
	}

	public void setQuoteContent(String quoteContent) {
		this.quoteContent = quoteContent;
	}
}
