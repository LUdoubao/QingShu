package org.doubao.ai.service.dto;

import java.util.List;

public class ChatResponse {
	private String id;
	private String object;
	private Long created;
	private String model;
	private List<Choice> choices;

	public static class Choice {
		private Message message;
		private Integer index;
		private String finish_reason;

		public Message getMessage() {
			return message;
		}

		public void setMessage(Message message) {
			this.message = message;
		}

		public Integer getIndex() {
			return index;
		}

		public void setIndex(Integer index) {
			this.index = index;
		}

		public String getFinish_reason() {
			return finish_reason;
		}

		public void setFinish_reason(String finish_reason) {
			this.finish_reason = finish_reason;
		}
	}

	public static class Message {
		private String role;
		private String content;

		public String getRole() {
			return role;
		}

		public void setRole(String role) {
			this.role = role;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getObject() {
		return object;
	}

	public void setObject(String object) {
		this.object = object;
	}

	public Long getCreated() {
		return created;
	}

	public void setCreated(Long created) {
		this.created = created;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public List<Choice> getChoices() {
		return choices;
	}

	public void setChoices(List<Choice> choices) {
		this.choices = choices;
	}
}