package org.doubao.dialog.service.feign;


import lombok.extern.slf4j.Slf4j;
import org.doubao.dialog.service.dto.AIRequest;
import org.doubao.dialog.service.dto.AIResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * AI服务Feign客户端降级处理
 */
@Component
public class AIServiceFallback implements AIServiceClient {

	private static final Logger log = LoggerFactory.getLogger(AIServiceFallback.class);
	@Override
	public AIResponse generateReply(AIRequest request) {
		log.error("调用AI服务generateReply失败，执行降级处理");
		AIResponse response = new AIResponse();
		response.setContent("抱歉，当前服务繁忙，请稍后再试");
		response.setSuccess(false);
		return response;
	}

	@Override
	public String generateTitle(String content) {
		log.error("调用AI服务generateTitle失败，执行降级处理");
		return "新对话";
	}
}
