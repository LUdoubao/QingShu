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
	private final HttpStatus status;
	private final String errorCode;
	private final String message;
	private final Instant timestamp;
	private final Map<String, Object> metadata;

	public ErrorResponse(HttpStatus status, String errorCode, String message, Instant timestamp, Map<String, Object> metadata) {
		this.status = status;
		this.errorCode = errorCode;
		this.message = message;
		this.timestamp = timestamp;
		this.metadata = metadata;
	}

	// 私有构造方法
	private ErrorResponse(Builder builder) {
		this.status = builder.status;
		this.errorCode = builder.errorCode;
		this.message = builder.message;
		this.timestamp = builder.timestamp;
		this.metadata = builder.metadata;
	}

	// ---------- Builder 实现 ----------
	public static class Builder {
		private HttpStatus status;
		private String errorCode;
		private String message;
		private Instant timestamp = Instant.now();
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

		public Builder timestamp(Instant timestamp) {
			this.timestamp = timestamp;
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

	// ---------- 静态工厂方法 ----------
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * 从异常创建错误响应
	 */
	public static ErrorResponse fromException(BusinessException ex) {
		return builder()
				.status(ex.getHttpStatus())
				.errorCode(ex.getErrorCode().name())
				.message(ex.getMessage())
				.metadata(ex.getMetadata())
				.build();
	}

	/**
	 * 快速创建参数错误响应
	 */
	public static ErrorResponse invalidParam(String field, String reason) {
		return builder()
				.status(HttpStatus.BAD_REQUEST)
				.errorCode("INVALID_PARAM")
				.message("参数校验失败: " + reason)
				.metadata(new java.util.HashMap<String, Object>() {{
					put("field", field);
					put("reason", reason);
				}})
				.build();
	}

}