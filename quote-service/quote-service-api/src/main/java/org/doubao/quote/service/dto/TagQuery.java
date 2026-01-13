package org.doubao.quote.service.dto;

import java.util.List;

public class TagQuery {
	private String tagName;
	private List<Long> ids;

	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public List<Long> getIds() {
		return ids;
	}

	public void setIds(List<Long> ids) {
		this.ids = ids;
	}
}
