package org.doubao.ai.service.service;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.doubao.ai.service.dto.*;
import org.doubao.mall.common.entity.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.*;

@Service
public class AIService {
	private static final Logger LOGGER = LoggerFactory.getLogger(AIService.class);
	private final ExecutorService executor = Executors.newFixedThreadPool(3);
	private final ObjectMapper objectMapper;
	private OkHttpClient httpClient;
	@Resource
	private RedisTemplate<String, Object> redisTemplate;

	@Value("${api.baidu.url}")
	private String baiduApiUrl;
	@Value("${api.baidu.key}")
	private String baiduApiKey;
	@Value("${api.baidu.model}")
	private String baiduModel;
	@Value("${api.deepseek.url}")
	private String deepseekApiUrl;
	@Value("${api.deepseek.key}")
	private String deepseekApiKey;
	@Value("${api.deepseek.model}")
	private String deepseekModel;
	private final int connectTimeout = 300000;
	private final int readTimeout = 600000;

	private final static String AI_KEY = "AI_KEY:";
	public AIService(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@PostConstruct
	public void init() {
		// 配置连接池（最大空闲连接数和保持时间）
		ConnectionPool connectionPool = new ConnectionPool(20, 5, TimeUnit.MINUTES);

		// 配置HTTP客户端
		this.httpClient = new OkHttpClient.Builder()
				.connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
				.readTimeout(readTimeout, TimeUnit.MILLISECONDS)
				.callTimeout(readTimeout + connectTimeout + 5000, TimeUnit.MILLISECONDS)
				.connectionPool(connectionPool)
				.retryOnConnectionFailure(true) // 启用连接失败重试
				.build();
	}

	public ChatResponse sendChatRequest(String content, String author, String source, String model, String id, String twice, String isPoetry) throws IOException {
		if(twice == null || !twice.equals("true")) {
			// 从redis获取
			if (Boolean.TRUE.equals(redisTemplate.hasKey(AI_KEY + id))) {
				return (ChatResponse) redisTemplate.opsForValue().get(AI_KEY + id);
			}
		}
		String url = "";
		String key = "";
		if (isPoetry.equals("true")) {
			url = deepseekApiUrl;
			key = deepseekApiKey;
			model = model == null || model.isEmpty() ? deepseekModel : model;
		} else {
			url = baiduApiUrl;
			key = baiduApiKey;
			model = model == null || model.isEmpty() ? baiduModel : model;
		}

		// 第一次获取或二次刷新
		ChatRequest request = buildRequest(content, author, source, isPoetry);
		request.setModel(model);

		String finalUrl = url;
		String finalKey = key;
		return executeWithTimeout(() -> {
			RequestBody requestBody = RequestBody.create(
					objectMapper.writeValueAsString(request),
					MediaType.parse("application/json; charset=utf-8")
			);

			Request httpRequest = new Request.Builder()
					.url(finalUrl)
					.post(requestBody)
					.addHeader("Authorization", "Bearer " + finalKey)
					.addHeader("Content-Type", "application/json")
					.addHeader("appid", "") // 根据实际需求添加
					.build();

			try (Response response = httpClient.newCall(httpRequest).execute()) {
				if (response.isSuccessful() && response.body() != null) {
					String responseJson = response.body().string();
					ChatResponse chatResponse = objectMapper.readValue(responseJson, ChatResponse.class);
					redisTemplate.opsForValue().set(AI_KEY + id, chatResponse);
					return chatResponse;
				} else {
					throw new IOException("API request failed. Status: " + response.code() + ", Body: " +
							(response.body() != null ? response.body().string() : ""));
				}
			}
		});
	}

	private ChatRequest buildRequest(String content, String author, String source, String isPoetry) {
		String formattedPrompt = createStandardPrompt(content, author, source, isPoetry);
		ChatRequest request = new ChatRequest();
		request.setStream(false);
		request.setMessages(Arrays.asList(
				new ChatRequest.Message("system", "你是一位专业的文学分析助手，擅长解读各类引文作品。"),
				new ChatRequest.Message("user", formattedPrompt)
		));
		return request;
	}

	private ChatRequest bulidChatRequest(List<ChatMessage> messages) {
		ChatRequest request = new ChatRequest();
		request.setStream(false);
		request.setMessages(ChatRequest.Message.convert(messages));
		return request;
	}


	private String createStandardPrompt(String content, String author, String source, String isPoetry) {
		if (isPoetry.equals("true")) {
			return "## 引文拓展要求\n\n" +
					"### 引文信息\n" +
					"- **引文内容**: " + content + "\n" +
					"- **引文作者**: " + author + "\n" +
					"- **引文来源**: " + source + "\n\n" +
					"### 分析要求\n" +
					"请按照以下Markdown格式规范给出拓展内容：\n\n" +
					"#### 1. 全文\n" +
					"#### 2. 译文\n" +
					"#### 3. 注释\n" +
					"#### 4. 序言\n" +
					"**注意**：\n" +
					"- 全文的内容一定要是该诗词的全部诗词句，不能有遗漏，不能有错误\n"+
					"- 全文的内容仅包含全文，不要有多余的描述，字号用正文字号\n"+
					"- 全文的内容过长时根据句号合理换行，避免单行过长\n"+
					"- 译文内容为全文译文，即翻译成白话文，语言为中文，不要出现其他语言\n"+
					"- 除全文内容外输出内容长度不超过300字\n"+
					"- 内容准确专业客观\n" +
					"- 使用规范的Markdown语法";
		}
		return "## 引文分析要求\n\n" +
				"### 引文信息\n" +
				"- **引文内容**: " + content + "\n" +
				"- **引文作者**: " + author + "\n" +
				"- **引文来源**: " + source + "\n\n" +
				"### 分析要求\n" +
				"请按照以下Markdown格式规范进行专业分析,并直接给出分析内容：\n\n" +
				"#### 1. 引文赏析\n" +
				"#### 2. 创作背景\n" +
				"**注意**：\n" +
				"- 分析内容不超过300字\n" +
				"- 保持专业客观的分析态度\n" +
				"- 使用规范的Markdown语法";
	}

	private <T> T executeWithTimeout(Callable<T> task) throws IOException {
		Future<T> future = executor.submit(task);
		try {
			return future.get(readTimeout + connectTimeout + 5000, TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			future.cancel(true);
			throw new IOException("API request timed out", e);
		} catch (ExecutionException | InterruptedException e) {
			throw new IOException("API request failed", e);
		}
	}

	@PreDestroy
	public void shutdown() {
		// 关闭线程池
		executor.shutdownNow();

		// 关闭OkHttpClient连接池
		if (httpClient != null) {
			httpClient.connectionPool().evictAll();
			httpClient.dispatcher().executorService().shutdown();
		}
	}

	public AIResponse generateReply(AIRequest request) throws IOException {
		AIResponse aiResponse = new AIResponse();

		String userId = request.getUserId();
		// 限流检查
		String limitError = checkRateLimit(Long.valueOf(userId));
		if (limitError != null) {
			aiResponse.setErrorMsg(limitError);
			aiResponse.setSuccess(false);
			return aiResponse; // 返回限流错误
		}
		ApiInfo apiInfo = buildApiInfo(request);
		ChatRequest chatRequest = bulidChatRequest(request.getMessages());
		chatRequest.setModel(apiInfo.apiModel);

		LOGGER.info("=============chatRequest: " + JSON.toJSONString(chatRequest));
		RequestBody requestBody = RequestBody.create(
				objectMapper.writeValueAsString(chatRequest),
				MediaType.parse("application/json; charset=utf-8")
		);

		Request httpRequest = new Request.Builder()
				.url(apiInfo.apiUrl)
				.post(requestBody)
				.addHeader("Authorization", "Bearer " + apiInfo.apiKey)
				.addHeader("Content-Type", "application/json")
				.build();

		try (Response response = httpClient.newCall(httpRequest).execute()) {
			if (response.isSuccessful() && response.body() != null) {
				String responseJson = response.body().string();
				LOGGER.info("=============responseJson: " + responseJson);
				ChatResponse chatResponse = objectMapper.readValue(responseJson, ChatResponse.class);
				if (chatResponse.getChoices() != null && !chatResponse.getChoices().isEmpty()) {
					LOGGER.info("=============getContent: {} ", chatResponse.getChoices().get(0).getMessage().getContent());
					aiResponse.setContent(chatResponse.getChoices().get(0).getMessage().getContent());
					aiResponse.setSuccess(true);
					aiResponse.setResponseTime(System.currentTimeMillis());
					aiResponse.setErrorMsg("");
					return aiResponse;
				}
				aiResponse.setSuccess(false);
				aiResponse.setErrorMsg("API request failed. Status: " + response.code() + ", Body: " +
						(response.body() != null ? response.body().string() : ""));
				return aiResponse;
			} else {
				LOGGER.info("=============response: " + JSON.toJSONString(response));
				aiResponse.setSuccess(false);
				aiResponse.setErrorMsg("API request failed. Status: " + response.code() + ", Body: " +
						(response.body() != null ? response.body().string() : ""));
			}
		} catch (Exception e) {
			LOGGER.info("=============e: " + e.getMessage());
			aiResponse.setSuccess(false);
			aiResponse.setErrorMsg("API request failed. " + e.getMessage());
		}
		return aiResponse;
	}
	private static class ApiInfo {
		private String apiUrl;
		private String apiKey;
		private String apiModel;
	}

	private ApiInfo buildApiInfo(AIRequest  request) {
		ApiInfo apiInfo = new ApiInfo();
		String aiType = request.getAiType();
		LOGGER.info("=============aiType: " + aiType);
		if (aiType == null) {
			aiType = "baidu";
		}
		String url = "";
		String key = "";
		String model = request.getModel();
		switch (aiType) {
			case "deepSeek":
				url = deepseekApiUrl;
				key = deepseekApiKey;
				model = model == null || model.isEmpty() ? deepseekModel : model;
				break;
			case "baidu":
			default:
				url = baiduApiUrl;
				key = baiduApiKey;
				model = model == null || model.isEmpty() ? baiduModel : model;
		}

		LOGGER.info("=============url: " + url);
		LOGGER.info("=============model: " + model);
		apiInfo.apiKey = key;
		apiInfo.apiUrl = url;
		apiInfo.apiModel = model;
		return apiInfo;
	}
	// 生成当天的日期字符串（格式：yyyyMMdd）
	private String getTodayDateStr() {
		Calendar calendar = Calendar.getInstance();
		return String.format("%04d%02d%02d",
				calendar.get(Calendar.YEAR),
				calendar.get(Calendar.MONTH) + 1,
				calendar.get(Calendar.DAY_OF_MONTH));
	}
	// 检查限流（返回错误信息，null表示通过）
	public String checkRateLimit(Long userId) {
		String today = getTodayDateStr();

		// 全局限流Key
		String globalKey = "ai:limit:global:" + today;
		// 用户限流Key（基于userId）
		String userKey = "ai:limit:user:" + userId + ":" + today;

		ValueOperations<String, Object> ops = redisTemplate.opsForValue();

		if (ops.get(globalKey) != null) {
			// 检查全局调用次数
			Integer globalCount = (Integer) ops.get(globalKey);
			if (globalCount != null && globalCount >= 100) {
				return "系统调用已达今日上限（100次），请明天再试";
			}
		}
		if (ops.get(userKey) != null) {
			// 检查用户调用次数
			Integer userCount = (Integer) ops.get(userKey);
			if (userCount != null && userCount >= 10) {
				return "您的调用已达今日上限（10次），请明天再试";
			}
		}

		// 增加计数（使用Redis事务保证原子性）
		redisTemplate.execute((RedisCallback<Object>) connection -> {
			// 全局计数+1（如果不存在则初始化为1，过期时间24小时）
			ops.increment(globalKey, 1);
			redisTemplate.expire(globalKey, 1, TimeUnit.DAYS);

			// 用户计数+1（如果不存在则初始化为1，过期时间24小时）
			ops.increment(userKey, 1);
			redisTemplate.expire(userKey, 1, TimeUnit.DAYS);
			return null;
		});

		return null; // 通过限流
	}
}