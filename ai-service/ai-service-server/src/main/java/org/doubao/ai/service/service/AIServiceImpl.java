package org.doubao.ai.service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.doubao.ai.service.dto.ChatRequest;
import org.doubao.ai.service.dto.ChatResponse;
import org.doubao.mall.common.enums.ErrorCode;
import org.doubao.mall.common.exception.BusinessException;
import org.doubao.mall.common.util.DoubaoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.*;

@Service
public class AIServiceImpl implements AIService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AIServiceImpl.class);
    private static final String AI_KEY = "AI_KEY:";
    private static final String AI_LIMIT_GLOBAL = "AI:LIMIT:GLOBAL:";
    private static final String AI_LIMIT_USER = "AI:LIMIT:USER:";

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

    public AIServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        ConnectionPool connectionPool = new ConnectionPool(20, 5, TimeUnit.MINUTES);
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
                .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
                .callTimeout(readTimeout + connectTimeout + 5000L, TimeUnit.MILLISECONDS)
                .connectionPool(connectionPool)
                .retryOnConnectionFailure(true)
                .build();
    }

    public ChatResponse sendChatRequest(String userId, String content, String author, String title, String model, String id, String twice, String isPoetry) throws IOException {
        if (!"true".equals(twice) && Boolean.TRUE.equals(redisTemplate.hasKey(AI_KEY + id))) {
            return (ChatResponse) redisTemplate.opsForValue().get(AI_KEY + id);
        }

        checkRateLimit(Long.valueOf(userId));

        String url = deepseekApiUrl;
        String key = deepseekApiKey;
        String resolvedModel = DoubaoUtils.isEmpty(model) ? deepseekModel : model;

        ChatRequest request = buildRequest(content, author, title, isPoetry);
        request.setModel(resolvedModel);

        return executeWithTimeout(() -> {
            RequestBody requestBody = RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8"),
                    objectMapper.writeValueAsString(request)
            );

            Request httpRequest = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .addHeader("Authorization", "Bearer " + key)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("appid", "")
                    .build();

            try (Response response = httpClient.newCall(httpRequest).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseJson = response.body().string();
                    ChatResponse chatResponse = objectMapper.readValue(responseJson, ChatResponse.class);
                    redisTemplate.opsForValue().set(AI_KEY + id, chatResponse);
                    return chatResponse;
                }
                throw new IOException("API request failed. Status: " + response.code() + ", Body: " +
                        (response.body() != null ? response.body().string() : ""));
            }
        });
    }

    public ChatRequest buildRequest(String content, String author, String title, String isPoetry) {
        String formattedPrompt = createStandardPrompt(content, author, title, isPoetry);
        ChatRequest request = new ChatRequest();
        request.setStream(false);
        request.setMessages(Arrays.asList(
                new ChatRequest.Message("system", "You are a literature analysis assistant focused on quote interpretation."),
                new ChatRequest.Message("user", formattedPrompt)
        ));
        return request;
    }

    private String createStandardPrompt(String content, String author, String title, String isPoetry) {
        if ("true".equals(isPoetry)) {
            return "Quote content: " + content + "\n" +
                    "Author: " + author + "\n" +
                    "Source: " + title + "\n" +
                    "Please output markdown sections for full text, translation, notes, and introduction. Keep it concise and accurate.";
        }
        return "Quote content: " + content + "\n" +
                "Author: " + author + "\n" +
                "Source: " + title + "\n" +
                "Please output markdown sections for appreciation and background. Keep it concise and objective.";
    }

    private <T> T executeWithTimeout(Callable<T> task) throws IOException {
        Future<T> future = executor.submit(task);
        try {
            return future.get(readTimeout + connectTimeout + 5000L, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new IOException("API request timed out", e);
        } catch (ExecutionException | InterruptedException e) {
            throw new IOException("API request failed", e);
        }
    }

    public String appreciation(Map<String, String> request) throws IOException {
        String userId = request.get("userId");
        String id = request.get("id");
        String content = request.get("content");
        String title = request.get("title");
        String author = request.get("author");
        String model = request.get("model");
        String twice = request.get("twice");
        String isPoetry = request.get("isPoetry");
        ChatResponse response = sendChatRequest(userId, content, author, title, model, id, twice, isPoetry);
        return response.getChoices().get(0).getMessage().getContent();
    }

    private String getTodayDateStr() {
        Calendar calendar = Calendar.getInstance();
        return String.format("%04d%02d%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH));
    }

    public void checkRateLimit(Long userId) {
        String today = getTodayDateStr();
        String globalKey = AI_LIMIT_GLOBAL + today;
        String userKey = AI_LIMIT_USER + userId + ":" + today;

        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        if (ops.get(globalKey) != null) {
            Integer globalCount = (Integer) ops.get(globalKey);
            if (globalCount != null && globalCount >= 100) {
                throw new BusinessException(ErrorCode.AI_LIMIT_EXCEEDED_SYSTEM);
            }
        }
        if (ops.get(userKey) != null) {
            Integer userCount = (Integer) ops.get(userKey);
            if (userCount != null && userCount >= 10) {
                throw new BusinessException(ErrorCode.AI_LIMIT_EXCEEDED_TODAY);
            }
        }

        redisTemplate.execute((RedisCallback<Object>) connection -> {
            ops.increment(globalKey, 1);
            redisTemplate.expire(globalKey, 1, TimeUnit.DAYS);
            ops.increment(userKey, 1);
            redisTemplate.expire(userKey, 1, TimeUnit.DAYS);
            return null;
        });
    }
}
