package org.doubao.comment.service.feign.back;

import org.doubao.comment.service.feign.QuoteClient;
import org.doubao.comment.service.vo.QuoteVo;
import org.doubao.mall.common.entity.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * 引文服务降级处理工厂
 * <p>
 * 在微服务模式下（service.run-mode=microservice），当引文服务不可用时，
 * 提供降级处理逻辑，确保评论服务的可用性
 */
@Component
@ConditionalOnProperty(name = "service.run-mode", havingValue = "microservice")
public class QuoteClientFallback implements FallbackFactory<QuoteClient> {
	private static final Logger LOGGER = LoggerFactory.getLogger(QuoteClientFallback.class);

	/**
	 * 创建引文服务降级实现
	 * <p>
	 * 当引文服务调用失败时，返回降级实现，提供默认的错误处理逻辑
	 * 
	 * @param cause 服务调用失败的原因
	 * @return 引文服务的降级实现
	 */
	@Override
	public QuoteClient create(Throwable cause) {
		LOGGER.error("Quote service unavailable, cause: {}", cause.getMessage(), cause);
		return new QuoteClient() {
			/**
			 * 获取引文详情操作降级处理
			 * <p>
			 * 当引文服务不可用时，返回错误信息，避免影响评论服务的正常运行
			 * 
			 * @param id 引文ID
			 * @return 包含错误信息的响应结果
			 */
			@Override
			public Result<QuoteVo> detail(Long id) {
				LOGGER.error("Quote service detail method failed for id: {}, cause: {}", id, cause.getMessage());
				return Result.error("引文服务暂时不可用，请稍后重试");
			}
		};
	}
}