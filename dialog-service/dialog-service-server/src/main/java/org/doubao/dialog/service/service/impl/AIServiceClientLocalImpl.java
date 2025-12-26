package org.doubao.dialog.service.service.impl;

import org.doubao.dialog.service.dto.AIRequest;
import org.doubao.dialog.service.feign.AIServiceClient;
import org.doubao.ai.service.service.AIService;
import org.doubao.dialog.service.dto.AIResponse;
import org.doubao.mall.common.entity.Result;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@ConditionalOnProperty(name = "service.run-mode", havingValue = "monolith", matchIfMissing = true)
public class AIServiceClientLocalImpl implements AIServiceClient {

	@Resource
	private AIService aiService;

	@Override
	public Result<AIResponse> generateReply(AIRequest request) {
		try {
			org.doubao.ai.service.dto.AIRequest aiRequest = new org.doubao.ai.service.dto.AIRequest();
			BeanUtils.copyProperties(request, aiRequest);
			org.doubao.ai.service.dto.AIResponse response = aiService.generateReply(aiRequest);
			AIResponse dialogResponse = new AIResponse();
			BeanUtils.copyProperties(response, dialogResponse);
			return Result.success(dialogResponse);
		} catch (Exception e) {
			return Result.error("AI服务调用失败: " + e.getMessage());
		}
	}
}