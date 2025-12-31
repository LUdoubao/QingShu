package org.doubao.comment.service.feign.back;

import org.doubao.comment.service.feign.QuoteClient;
import org.doubao.comment.service.vo.QuoteVo;
import org.doubao.mall.common.entity.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "service.run-mode", havingValue = "microservice")
public class QuoteClientFallback implements FallbackFactory<QuoteClient> {
	private static final Logger LOGGER = LoggerFactory.getLogger(QuoteClientFallback.class);

	@Override
	public QuoteClient create(Throwable cause) {
		LOGGER.error("Quote service unavailable, cause: {}", cause.getMessage(), cause);
		return new QuoteClient() {
			@Override
			public Result<QuoteVo> detail(Long id) {
				LOGGER.error("Quote service detail method failed for id: {}, cause: {}", id, cause.getMessage());
				return Result.error("引文服务暂时不可用，请稍后重试");
			}
		};
	}
}