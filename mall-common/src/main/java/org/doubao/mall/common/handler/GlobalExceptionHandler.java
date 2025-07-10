package org.doubao.mall.common.handler;

import org.doubao.mall.common.entity.ErrorResponse;
import org.doubao.mall.common.exception.BusinessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorResponse> handleBusinessException(
			BusinessException ex) {
		return ResponseEntity
				.status(ex.getHttpStatus())
				.body(ex.toErrorResponse());
	}
}