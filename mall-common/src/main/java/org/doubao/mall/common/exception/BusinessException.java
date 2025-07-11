package org.doubao.mall.common.exception;


import org.doubao.mall.common.enums.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * 业务异常基类
 * <p>
 * 所有业务异常都应继承此异常，实现：
 * 1. 统一的异常处理
 * 2. 规范的错误码管理
 * 3. 结构化的错误信息
 */
public class BusinessException extends RuntimeException {

	/**
	 * HTTP状态码
	 * <p>
	 * 符合RESTful规范的状态码（如400, 404等）
	 */
	private HttpStatus httpStatus;

	/**
	 * 业务错误码
	 * <p>
	 * 格式：模块前缀 + 三位数字（如USER_001）
	 */
	private ErrorCode errorCode;

	/**
	 * 错误元数据
	 * <p>
	 * 用于携带额外的调试信息
	 */
	private Map<String, Object> metadata;


	/**
	 * 全参数构造方法
	 *
	 * @param httpStatus HTTP状态码（非空）
	 * @param message 错误描述（可读的提示信息）
	 * @param errorCode 业务错误码（非空）
	 * @param metadata 附加元数据（可为null）
	 */
	public BusinessException(
			HttpStatus httpStatus,
			String message,
			ErrorCode errorCode,
			Map<String, Object> metadata) {
		super(message);
		Assert.notNull(httpStatus, "HttpStatus不能为空");
		Assert.notNull(errorCode, "ErrorCode不能为空");
		this.httpStatus = httpStatus;
		this.errorCode = errorCode;
		this.metadata = metadata;
	}

	/**
	 * 简化构造方法（无metadata）
	 */
	public BusinessException(
			HttpStatus httpStatus,
			String message,
			ErrorCode errorCode) {
		this(httpStatus, message, errorCode, null);
	}

	/**
	 * 最简构造方法（默认400状态码）
	 */
	public BusinessException(String message, ErrorCode errorCode) {
		this(HttpStatus.BAD_REQUEST, message, errorCode, null);
	}

	public HttpStatus getHttpStatus() {
		return httpStatus;
	}

	public ErrorCode getErrorCode() {
		return errorCode;
	}

	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setErrorCode(ErrorCode errorCode) {
		this.errorCode = errorCode;
	}

	public void setHttpStatus(HttpStatus httpStatus) {
		this.httpStatus = httpStatus;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}
}