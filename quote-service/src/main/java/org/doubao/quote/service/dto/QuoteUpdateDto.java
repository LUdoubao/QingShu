package org.doubao.quote.service.dto;

import org.doubao.quote.service.vo.QuoteVo;

public class QuoteUpdateDto {
	/**
	 * 待更新的引文id
	 */
	private Long quoteId;
	/**
	 * 旧引文
	 */
	private QuoteVo beforeQuoteVo;
	/**
	 * 新引文
	 */
	private QuoteVo afterQuoteVo;

	public Long getQuoteId() {
		return quoteId;
	}

	public void setQuoteId(Long quoteId) {
		this.quoteId = quoteId;
	}

	public QuoteVo getBeforeQuoteVo() {
		return beforeQuoteVo;
	}

	public void setBeforeQuoteVo(QuoteVo beforeQuoteVo) {
		this.beforeQuoteVo = beforeQuoteVo;
	}

	public QuoteVo getAfterQuoteVo() {
		return afterQuoteVo;
	}

	public void setAfterQuoteVo(QuoteVo afterQuoteVo) {
		this.afterQuoteVo = afterQuoteVo;
	}
}
