package org.doubao.ai.service.dto;

import java.util.List;

public class ChatRequest {
	private boolean stream = false;
	private String model;
	private List<Message> messages;

	public ChatRequest(boolean stream, String model, List<Message> messages) {
		this.stream = stream;
		this.model = model;
		this.messages = messages;
	}

	public ChatRequest() {
	}

	public static class Message {
		private String role;
		private String content;

		public Message(String role, String content) {
			this.role = role;
			this.content = content;
		}

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

	public boolean isStream() {
		return stream;
	}

	public void setStream(boolean stream) {
		this.stream = stream;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public List<Message> getMessages() {
		return messages;
	}

	public void setMessages(List<Message> messages) {
		this.messages = messages;
	}
}