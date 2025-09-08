package org.doubao.mall.common.constant;

public class Constants {
	public static final String REDIS_USER =  "USER:";
	public static final String DEFAULT_USER_NAME =  "未知用户";

	public static final String USER_REGISTER_ROUTING_KEY = "event.key.USER_REGISTER";
	public static final String USER_COMMENT_ROUTING_KEY = "event.key.COMMENT_EVENT";
	public static final String USER_LIKE_ROUTING_KEY = "event.key.LIKE_EVENT";
	public static final String USER_QUOTE_ROUTING_KEY = "event.key.QUOTE_EVENT";
	public static final String NEW_MESSAGE_ROUTING_KEY = "event.key.NEW_MESSAGE";
	public static final String QUOTE_PUBLISH_ROUTING_KEY = "event.key.DYNAMIC_PUBLISH";
	public static final String FANOUT_EVENT_EXCHANGE = "fanout.event.exchange";


}