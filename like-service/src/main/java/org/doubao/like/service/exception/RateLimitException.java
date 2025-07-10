package org.doubao.like.service.exception;


import org.doubao.mall.common.enums.ErrorCode;
import org.doubao.mall.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

/**
 * 限流异常
 * <p>
 * 当用户操作频率超过系统限制时抛出
 *
 */
public class RateLimitException extends BusinessException {

	/**
	 * 用户ID（触发限流的用户）
	 */
	private final Long userId;

	/**
	 * 限流类型
	 */
	private final RateLimitType limitType;

	/**
	 * 构造方法
	 *
	 * @param userId    触发限流的用户ID
	 * @param limitType 限流类型枚举
	 */
	public RateLimitException(Long userId, RateLimitType limitType) {
		super(HttpStatus.TOO_MANY_REQUESTS,
				generateMessage(userId, limitType),
				ErrorCode.RATE_LIMIT_EXCEEDED);
		this.userId = userId;
		this.limitType = limitType;
	}

	/**
	 * 生成异常消息
	 *
	 * @param userId    用户ID
	 * @param limitType 限流类型
	 * @return 格式化后的提示消息
	 */
	private static String generateMessage(Long userId, RateLimitType limitType) {
		return String.format("用户[%d]触发%s限流规则", userId, limitType.getDescription());
	}


	/**
	 * 限流类型枚举
	 */
	public enum RateLimitType {
		GLOBAL("全局"),
		LIKE_OPERATION("点赞操作"),
		CONTENT_CREATION("内容创建");

		private final String description;

		RateLimitType(String description) {
			this.description = description;
		}

		public String getDescription() {
			return description;
		}
	}

	/**
	 * 创建点赞操作限流异常
	 *
	 * @param userId 用户ID
	 * @return 限流异常实例
	 */
	public static RateLimitException forLikeOperation(Long userId) {
		return new RateLimitException(userId, RateLimitType.LIKE_OPERATION);
	}

	/**
	 * 获取恢复时间提示
	 *
	 * @return 可读的时间提示字符串
	 */
	public String getRetryAfterHint() {
		switch (limitType) {
			case LIKE_OPERATION:
				return "请1分钟后再试";
			case CONTENT_CREATION:
				return "请30分钟后再试";
			default:
				return "请稍后再试";
		}
	}
}