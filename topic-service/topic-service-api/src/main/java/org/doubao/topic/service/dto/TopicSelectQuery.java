package org.doubao.topic.service.dto;

import java.util.List;

public class TopicSelectQuery {
	private String topicName;
	private List<Long> ids;

	public String getTopicName() {
		return topicName;
	}

	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

	public List<Long> getIds() {
		return ids;
	}

	public void setIds(List<Long> ids) {
		this.ids = ids;
	}
}
