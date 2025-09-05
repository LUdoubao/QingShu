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
		return Result.success(aiService.appreciation(request));
	}

	@PostMapping("/generate/reply")
	public Result<AIResponse>  generateReply(@RequestBody AIRequest request) throws IOException {
		return Result.success(aiService.generateReply(request));
	}
}
