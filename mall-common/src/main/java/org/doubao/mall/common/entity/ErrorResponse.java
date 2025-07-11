package org.doubao.mall.common.entity;

import lombok.Builder;
import org.doubao.mall.common.enums.ErrorCode;
import org.doubao.mall.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

import java.time.Instant;

import lombok.Data;
import org.springframework.http.HttpStatus;
import java.time.Instant;
import java.util.Map;

/**
 * 标准化错误响应体
 * <p>
 * 包含：
 * - HTTP状态码
 * - 业务错误码
 * - 错误消息
 * - 时间戳（自动生成）
 * - 扩展元数据（可选）
 */
public class ErrorResponse {
	private HttpStatus status;
	private String errorCode;
	private String message;
	private String path;
	private Instant timestamp;
	private Map<String, Object> metadata;

	private ErrorResponse(Builder builder) {
		this.status = builder.status;
		this.errorCode = builder.errorCode;
		this.message = builder.message;
		this.path = builder.path;
		this.timestamp = Instant.now();
		this.metadata = builder.metadata;
	}
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private HttpStatus status;
		private String errorCode;
		private String message;
		private String path;
		private Map<String, Object> metadata;

		public Builder status(HttpStatus status) {
			this.status = status;
			return this;
		}

		public Builder errorCode(String errorCode) {
			this.errorCode = errorCode;
			return this;
		}

		public Builder message(String message) {
			this.message = message;
			return this;
		}

		public Builder path(String path) {
			this.path = path;
			return this;
		}

		public Builder metadata(Map<String, Object> metadata) {
			this.metadata = metadata;
			return this;
		}

		public ErrorResponse build() {
			return new ErrorResponse(this);
		}
	}

	public HttpStatus getStatus() {
		return status;
	}

	public void setStatus(HttpStatus status) {
		this.status = status;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Instant getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Instant timestamp) {
		this.timestamp = timestamp;
	}

	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}
}