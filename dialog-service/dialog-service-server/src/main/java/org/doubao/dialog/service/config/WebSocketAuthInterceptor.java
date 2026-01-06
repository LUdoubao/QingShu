package org.doubao.dialog.service.config;

import org.apache.commons.lang.StringUtils;
import org.doubao.dialog.service.feign.AuthServiceClient;
import org.doubao.dialog.service.util.RedisCacheUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

/**
 * WebSocket认证拦截器
 * 核心职责：
 * 1. WebSocket连接建立前拦截请求，校验用户Token合法性
 * 2. 解析Token中的用户ID，绑定到WebSocket会话属性（供后续Handler使用）
 * 3. 拦截非法请求（无Token/Token过期/Token无效），返回401未授权
 * 适配场景：对话业务实时通信，确保只有已登录用户能建立WebSocket连接
 */
@Component
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

	// ========================= 常量定义 =========================
	/** Token参数名：WebSocket连接时客户端传递Token的参数键（如ws://xxx?token=xxx） */
	private static final String TOKEN_PARAM_NAME = "token";
	/** 用户ID会话属性键：解析后的用户ID绑定到会话的属性名（与DialogWebSocketHandler一致） */
	private static final String SESSION_ATTR_USER_ID = "userId";
	/** Token过期Redis键前缀：存储已拉黑/过期的Token，用于快速拦截（格式=BLACKLIST:xxx） */
	private static final String TOKEN_BLACKLIST_PREFIX = "BLACKLIST:";
	/** Token过期时间校验阈值（毫秒）：允许Token存在10秒时间偏差，避免网络延迟导致的误判 */
	private static final long TOKEN_EXPIRE_OFFSET = 10000L;

	private static final Logger log = LoggerFactory.getLogger(WebSocketAuthInterceptor.class);

	// ========================= 依赖注入 =========================
	/** Redis缓存工具类：查询Token黑名单（如用户登出后已失效的Token） */
	@Resource
	private RedisCacheUtil redisCacheUtil;
	@Resource
	private AuthServiceClient authServiceClient;


	// ========================= 核心拦截逻辑 =========================
	/**
	 * WebSocket连接建立前拦截（核心认证逻辑）
	 * 执行时机：客户端发送WebSocket握手请求后，连接建立前
	 * 返回值：true=认证通过，继续建立连接；false=认证失败，中断连接
	 */
	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
								   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
		// 1. 转换ServerHttpRequest为ServletServerHttpRequest，获取请求参数（兼容Spring WebSocket标准）
		ServletServerHttpRequest servletRequest = null;
		if (request instanceof ServletServerHttpRequest) {
			servletRequest = (ServletServerHttpRequest) request;
		} else {
			log.error("WebSocket auth failed | unsupported request type (clientIp: unknown)");
			setUnauthorizedResponse(response, "不支持的请求类型，无法解析Token");
			return false;
		}

		// 1.1 从请求参数中获取Token（WebSocket连接不支持请求头，需通过URL参数传递）
		String token = servletRequest.getServletRequest().getParameter(TOKEN_PARAM_NAME);
		// 1.2 获取客户端真实IP（用于日志定位和问题排查）
		String clientIp = getClientIp(servletRequest.getServletRequest());

		// 2. 基础参数校验（无Token直接拦截）
		if (StringUtils.isBlank(token)) {
			log.warn("WebSocket auth failed | no token found (clientIp: {})", clientIp);
			setUnauthorizedResponse(response, "WebSocket认证失败：缺少Token参数");
			return false;
		}

		// 3. 校验Token是否在黑名单（如用户登出后已失效的Token，通过Redis快速查询）
		String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
		String blacklistToken = redisCacheUtil.getString(blacklistKey, String.class);
		if (StringUtils.isNotBlank(blacklistToken)) {
			log.warn("WebSocket auth failed | token in blacklist (clientIp: {}, token: {})",
					clientIp, maskToken(token)); // Token脱敏，避免日志泄露敏感信息
			setUnauthorizedResponse(response, "WebSocket认证失败：Token已失效，请重新登录");
			return false;
		}

		// 4. 调用认证服务解析Token（核心校验：签名合法性、过期时间、用户有效性）
		String parseResult;
		try {
			parseResult = authServiceClient.webSocket(token).getData();
		} catch (Exception e) {
			// 其他未知异常（如认证服务调用失败、网络异常等）
			log.error("WebSocket auth failed | unknown error (clientIp: {}, token: {}, error: {})",
					clientIp, maskToken(token), e.getMessage(), e);
			setServerErrorResponse(response, "WebSocket认证服务异常，请稍后重试");
			return false;
		}

		// 5. 校验解析结果（确保用户ID非空、用户状态正常，避免无效用户建立连接）
		if (Objects.isNull(parseResult)) {
			log.warn("WebSocket auth failed | invalid user (clientIp: {}, userId: {})",
					clientIp, parseResult);
			setUnauthorizedResponse(response, "WebSocket认证失败：用户不存在或已被禁用");
			return false;
		}

		// 6. 认证通过：将用户ID绑定到会话属性（供后续DialogWebSocketHandler使用，传递用户身份）
		attributes.put(SESSION_ATTR_USER_ID, parseResult);
		log.info("WebSocket auth success | clientIp: {}, userId: {}, token: {}",
				clientIp, parseResult, maskToken(token));

		return true;
	}
	/**
	 * 工具方法：设置401未授权响应（统一响应格式，避免重复代码）
	 * @param response ServerHttpResponse响应对象
	 * @param message 错误提示信息
	 */
	private void setUnauthorizedResponse(ServerHttpResponse response, String message) throws IOException {
		response.setStatusCode(HttpStatus.UNAUTHORIZED);
		writeResponseContent(response, message);
	}

	/**
	 * 工具方法：设置500服务错误响应（统一响应格式，避免重复代码）
	 * @param response ServerHttpResponse响应对象
	 * @param message 错误提示信息
	 */
	private void setServerErrorResponse(ServerHttpResponse response, String message) throws IOException {
		response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
		writeResponseContent(response, message);
	}

	/**
	 * 工具方法：写入响应内容（处理响应编码和流关闭，确保响应正常返回）
	 * @param response ServerHttpResponse响应对象
	 * @param content 响应内容
	 */
	private void writeResponseContent(ServerHttpResponse response, String content) throws IOException {
		// 设置响应头：指定内容类型和编码，避免中文乱码
		response.getHeaders().setContentType(MediaType.TEXT_PLAIN);
		response.getHeaders().setContentLength(content.getBytes(StandardCharsets.UTF_8).length);

		// 写入响应内容并关闭流
		try (OutputStream outputStream = response.getBody()) {
			outputStream.write(content.getBytes(StandardCharsets.UTF_8));
			outputStream.flush();
		}
	}


	/**
	 * WebSocket连接建立后回调（空实现，无需处理）
	 * 执行时机：WebSocket连接建立成功后
	 */
	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
							   WebSocketHandler wsHandler, Exception exception) {
	}


	// ========================= 工具方法 =========================
	/**
	 * 获取客户端真实IP（处理代理场景，如Nginx、网关转发）
	 * @param request HTTP请求对象
	 * @return 客户端IP地址（如192.168.1.100）
	 */
	private String getClientIp(HttpServletRequest request) {
		// 1. 先从X-Forwarded-For获取（代理场景下的真实IP）
		String xffIp = request.getHeader("X-Forwarded-For");
		if (StringUtils.isNotBlank(xffIp) && !"unknown".equalsIgnoreCase(xffIp)) {
			// X-Forwarded-For格式：clientIp, proxyIp1, proxyIp2（取第一个非unknown的IP）
			String[] ipArr = xffIp.split(",");
			for (String ip : ipArr) {
				if (StringUtils.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip.trim())) {
					return ip.trim();
				}
			}
		}

		// 2. 从X-Real-IP获取（Nginx常用配置）
		String realIp = request.getHeader("X-Real-IP");
		if (StringUtils.isNotBlank(realIp) && !"unknown".equalsIgnoreCase(realIp)) {
			return realIp.trim();
		}

		// 3. 最后从request获取（直接请求场景）
		return request.getRemoteAddr();
	}

	/**
	 * Token脱敏（隐藏中间部分字符，避免日志泄露敏感信息）
	 * 脱敏规则：前6位 + ... + 后4位（如eyJhbG...1234）
	 * @param token 原始Token
	 * @return 脱敏后的Token
	 */
	private String maskToken(String token) {
		if (StringUtils.isBlank(token)) {
			return "";
		}
		if (token.length() <= 10) {
			// 短Token（如测试环境）直接返回前3位+...
			return token.substring(0, Math.min(3, token.length())) + "...";
		}
		return token.substring(0, 6) + "..." + token.substring(token.length() - 4);
	}

	// ========================= 内部依赖类定义（与AuthService交互的模型） =========================
	/**
	 * Token解析结果模型（AuthService返回的解析结果封装）
	 * 包含用户ID、用户状态、Token过期时间等核心信息
	 */
	public static class TokenParseResult {
		/** 解析出的用户ID（Token合法时非空） */
		private Long userId;
		/** 用户状态：true=正常（已激活、未被禁用），false=异常（未激活、已禁用） */
		private boolean isUserActive;
		/** Token过期时间戳（毫秒）：用于进一步校验过期状态 */
		private long tokenExpireTime;

		// Getter和Setter（Lombok的注解可简化，此处显式定义便于理解）
		public Long getUserId() {
			return userId;
		}

		public void setUserId(Long userId) {
			this.userId = userId;
		}

		public boolean isUserActive() {
			return isUserActive;
		}

		public void setUserActive(boolean userActive) {
			isUserActive = userActive;
		}

		public long getTokenExpireTime() {
			return tokenExpireTime;
		}

		public void setTokenExpireTime(long tokenExpireTime) {
			this.tokenExpireTime = tokenExpireTime;
		}
	}
}
