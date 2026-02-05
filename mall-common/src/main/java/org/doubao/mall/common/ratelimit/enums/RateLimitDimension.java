package org.doubao.mall.common.ratelimit.enums;

/**
 * 限流维度
 */
public enum RateLimitDimension {
	/**
	 * 全局限流
	 */
	GLOBAL,
	/**
	 * IP限流
	 */
	IP,
	/**
	 * 用户限流
	 */
	USER
}
