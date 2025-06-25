package org.doubao.mall.common.handler;

import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.exception.BizException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BizException.class)
	public Result<?> handleBizException(BizException ex) {
		return Result.error(ex.getCode(), ex.getMessage());
	}

	@ExceptionHandler(Exception.class)
	public Result<?> handleException(Exception ex) {
		return Result.error("服务器异常: " + ex.getMessage());
	}
}