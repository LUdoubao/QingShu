package org.doubao.mall.common.exception;

/**
 * 限流异常
 */
public class RateLimitExceededException extends RuntimeException {
	public RateLimitExceededException(String message) {
		super(message);
	}
}
