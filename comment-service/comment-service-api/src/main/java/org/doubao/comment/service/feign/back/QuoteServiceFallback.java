package org.doubao.comment.service.feign.back;

import org.doubao.comment.service.feign.QuoteClient;
import org.doubao.comment.service.vo.QuoteVo;
import org.doubao.mall.common.entity.Result;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

// Feign降级处理
@Component
@ConditionalOnProperty(name = "service.run-mode", havingValue = "microservice")
public class QuoteServiceFallback implements QuoteClient {
	@Override
	public Result<QuoteVo> detail(Long id) {
		return Result.error("引文服务暂时不可用，请稍后重试");
	}
}