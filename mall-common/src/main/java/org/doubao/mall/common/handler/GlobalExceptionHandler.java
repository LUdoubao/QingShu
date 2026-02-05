package org.doubao.mall.common.handler;

import org.doubao.mall.common.entity.ErrorResponse;
import org.doubao.mall.common.exception.BusinessException;
import org.doubao.mall.common.exception.RateLimitExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {
	private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorResponse> handleBusinessException(
			BusinessException ex, WebRequest request) {
		LOG.error("BusinessException: {}", ex.getMessage());
		// 构建错误响应
		ErrorResponse errorResponse = ErrorResponse.builder()
				.status(ex.getHttpStatus())
				.errorCode(ex.getErrorCode().getCode())
				.message(ex.getMessage())
				.path(request.getDescription(false).replace("uri=", ""))
				.build();

		return ResponseEntity
				.status(ex.getHttpStatus())
				.body(errorResponse);
	}
	@ExceptionHandler(RateLimitExceededException.class)
	public ResponseEntity<ErrorResponse> handleRateLimitExceededException(
			RateLimitExceededException ex, WebRequest request) {
		LOG.warn("RateLimitExceededException: {}", ex.getMessage());
		ErrorResponse errorResponse = ErrorResponse.builder()
				.status(HttpStatus.TOO_MANY_REQUESTS)
				.errorCode("RATE_LIMIT_EXCEEDED")
				.message(ex.getMessage())
				.path(request.getDescription(false).replace("uri=", ""))
				.build();
		return ResponseEntity
				.status(HttpStatus.TOO_MANY_REQUESTS)
				.body(errorResponse);
	}
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleAllExceptions(
			Exception ex, WebRequest request) {

		LOG.error("未捕获的异常: {}", ex.getMessage(), ex);

		ErrorResponse errorResponse = ErrorResponse.builder()
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.errorCode("INTERNAL_ERROR")
				.message("系统内部错误")
				.path(request.getDescription(false).replace("uri=", ""))
				.build();

		return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(errorResponse);
	}
}
