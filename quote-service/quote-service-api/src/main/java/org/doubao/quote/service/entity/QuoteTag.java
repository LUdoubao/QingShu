package org.doubao.quote.service.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("quote_tag")
public class QuoteTag {
	@TableField(value = "quote_id")
	private Long quoteId;
	@TableField(value = "tag_id")
	private Long tagId;

	public Long getQuoteId() {
		return quoteId;
	}

	public void setQuoteId(Long quoteId) {
		this.quoteId = quoteId;
	}

	public Long getTagId() {
		return tagId;
	}

	public void setTagId(Long tagId) {
		this.tagId = tagId;
	}
}