package org.doubao.ai.service.controller;

import org.doubao.ai.service.dto.AIRequest;
import org.doubao.ai.service.dto.AIResponse;
import org.doubao.ai.service.dto.ChatResponse;
import org.doubao.ai.service.service.AIService;
import org.doubao.mall.common.entity.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/ai")
public class ChatController {

	@Resource
	private AIService aiService;

	@PostMapping("/appreciation")
	public Result<String> appreciation(@RequestBody Map<String, String> request) throws IOException {
		String userId = request.get("userId");

		// 限流检查
		String limitError = aiService.checkRateLimit(Long.valueOf(userId));
		if (limitError != null) {
			return Result.error(limitError); // 返回限流错误
		}
		String id = request.get("id");
		String content = request.get("content");
		String source = request.get("source");
		String author = request.get("author");
		String model = request.get("model");
		String twice = request.get("twice");
		String isPoetry = request.get("isPoetry");
		ChatResponse response = aiService.sendChatRequest(content, author, source,model, id, twice, isPoetry);


		// 提取AI回复内容
		String aiReply = response.getChoices().get(0).getMessage().getContent();

		return Result.success(aiReply);
	}

	@PostMapping("/generate/reply")
	public Result<AIResponse>  generateReply(@RequestBody AIRequest request) throws IOException {
		return Result.success(aiService.generateReply(request));
	}
}
