package org.doubao.mall.common.constant;

public class Constants {
	// 请求头
	public static final String TOKEN_HEADER = "Authorization";
	// 响应头
	public static final String TRACE_ID_HEADER = "X-B3-TraceId";
	// 用户ID
	public static final String USER_ID_HEADER = "X-User-Id";
	// 用户名
	public static final String USER_NAME_HEADER = "X-User-Name";

	public static final String REDIS_USER =  "user:";
	public static final String DEFAULT_USER_NAME =  "未知用户";

	public static final String USER_VERIFICATION_EXCHANGE = "user.verification";
	public static final String BUSINESS_EXCHANGE = "business.exchange";
	public static final String QUOTE_EXCHANGE = "quote.exchange";
	public static final String NOTIFICATION_EXCHANGE = "notification.exchange";
	public static final String NOTIFICATION_QUEUE = "notification.queue";
	public static final String NOTIFICATION_LIKE_QUEUE = "notification.queue.like";
	public static final String USER_NOTIFICATION_ROUTING_KEY_PREFIX = "notification.user.";
}