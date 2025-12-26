package org.doubao.auth.service.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import org.doubao.auth.service.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
	@Autowired
	private JwtUtil jwtUtil;
	@Resource
	private RedisTemplate<String, Object> redisTemplate;

	@Override
	protected void doFilterInternal(HttpServletRequest request,
									HttpServletResponse response,
									FilterChain filterChain)
			throws ServletException, IOException {
		String token = request.getParameter("token");
		if (token != null && !token.isEmpty()) {
			if (isTokenExpired(token)) {
				logger.info("token已过期:{}", token);
				handleTokenExpired(response, request);
				return;
			}

			// 检查token是否在黑名单中
			if (isTokenBlacklisted(token)) {
				logger.info("token黑名单:{}", token);
				handleTokenInvalid(response, request);
				return;
			}


			// 4. 使用JWT工具类从Token中解析出用户名
			Claims claims = jwtUtil.getClaimsFromToken(token);

			if (claims != null && SecurityContextHolder.getContext().getAuthentication() == null) {
				UsernamePasswordAuthenticationToken authenticationToken =
						new UsernamePasswordAuthenticationToken(claims.getSubject(), null, Collections.emptyList());
				// 7. 将认证信息存入SecurityContext（标记该请求已认证）
				SecurityContextHolder.getContext().setAuthentication(authenticationToken);
			}
		}
		// 8. 无论是否处理认证，都必须继续过滤器链执行
		filterChain.doFilter(request, response);
	}

	// 检查令牌是否过期
	private boolean isTokenExpired(String token) {
		try {
			// 获取令牌过期时间
			Date expiration = jwtUtil.getExpirationDateFromToken(token);
			// 检查是否过期
			return expiration.before(new Date());
		} catch (Exception e) {
			// 解析失败视为过期
			return true;
		}
	}
	// 处理令牌过期响应
	private void handleTokenExpired(HttpServletResponse response,
									HttpServletRequest request) throws IOException {
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		// 构建标准化的错误响应
		Map<String, Object> errorResponse = new HashMap<>();
		errorResponse.put("status", HttpServletResponse.SC_UNAUTHORIZED);
		errorResponse.put("error", "Unauthorized");
		errorResponse.put("message", "Token已过期");
		errorResponse.put("path", request.getRequestURI());

		response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
	}
	// 检查令牌是否在黑名单中
	private boolean isTokenBlacklisted(String token) {
		return Boolean.TRUE.equals(redisTemplate.hasKey("BLACKLIST:" + token));
	}
	// 处理无效令牌响应
	private void handleTokenInvalid(HttpServletResponse response, HttpServletRequest request) throws IOException {
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		// 构建标准化的错误响应
		Map<String, Object> errorResponse = new HashMap<>();
		errorResponse.put("status", HttpServletResponse.SC_UNAUTHORIZED);
		errorResponse.put("error", "Unauthorized");
		errorResponse.put("message", "Token无效或已失效");
		errorResponse.put("path", request.getRequestURI());

		response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
	}


}