package org.doubao.dialog.service.feign;


import lombok.extern.slf4j.Slf4j;
import org.doubao.dialog.service.dto.AIRequest;
import org.doubao.dialog.service.dto.AIResponse;
import org.doubao.mall.common.entity.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * AI服务Feign客户端降级处理
 */
@Component
public class AIServiceFallback implements AIServiceClient {

	private static final Logger log = LoggerFactory.getLogger(AIServiceFallback.class);
	@Override
	public Result<AIResponse> generateReply(AIRequest request) {
		log.error("调用AI服务generateReply失败，执行降级处理");
		AIResponse response = new AIResponse();
		response.setContent("抱歉，当前服务繁忙，请稍后再试");
		response.setSuccess(false);
		response.setErrorMsg("服务异常");
		return Result.success(response);
	}
}
