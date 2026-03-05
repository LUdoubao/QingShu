package org.doubao.topic.service.dto;

/**
 * 查询话题下文案参数
 */
public class TopicQuoteQueryDTO {
	private Long topicId;
	private Integer page;
	private Integer size;

	public Long getTopicId() {
		return topicId;
	}

	public void setTopicId(Long topicId) {
		this.topicId = topicId;
	}

	public Integer getPage() {
		return page;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}
}
