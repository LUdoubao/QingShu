package org.doubao.auth.service.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import org.doubao.auth.service.service.AuthService;
import org.doubao.auth.service.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
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
import java.util.*;

public class JwtAuthenticationFilterLocal extends OncePerRequestFilter {
	private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilterLocal.class);
	@Autowired
	private JwtUtil jwtUtil;
	@Resource
	private AuthService authService;
	@Resource
	private RedisTemplate<String, Object> redisTemplate;
	private static final List<String> EXCLUDE_URLS = Arrays.asList(
			"/auth/",
			"/user/login",
			"/user/register",
			"/user/verify",
			"/user/forgot-password",
			"/public/",
			"/dialog/ws"
	);
	@Override
	protected void doFilterInternal(HttpServletRequest request,
									HttpServletResponse response,
									FilterChain filterChain)
			throws ServletException, IOException {
		// 获取当前请求的完整路径
		String path = request.getRequestURI();
		// 检查请求路径是否在排除列表(不需要JWT验证的路径)
		// 使用流操作检测路径前缀匹配，若匹配则跳过验证直接放行
		if (EXCLUDE_URLS.stream().anyMatch(path::startsWith)) {
			logger.info("请求路径：{}，不需要JWT验证", path);
			filterChain.doFilter(request, response);
			return;
		}
		// 从Authorization请求头中获取令牌
		String token = request.getHeader("Authorization");
		// 验证令牌格式：必须存在且以"Bearer "开头
		if (token == null || !token.startsWith("Bearer ")) {
			// 设置401状态
			response.setStatus(HttpStatus.UNAUTHORIZED.value());
			logger.error("JWT 解析失败, 请求路径：{}",  path);
			// 立即结束响应
			return;
		}
		String jwt = token.substring(7);

		if (isTokenExpired(jwt)) {
			logger.info("token已过期:{}", jwt);
			handleTokenExpired(response, request);
			return;
		}

		// 检查token是否在黑名单中
		if (isTokenBlacklisted(jwt)) {
			logger.info("token黑名单:{}", jwt);
			handleTokenInvalid(response, request);
			return;
		}

		Map<String, Object> body = authService.verify(jwt).getBody();
		Claims claims = (Claims) body;
		if (claims != null) {
			request.setAttribute("X-User-Name", claims.get("username"));
			request.setAttribute("X-User-Id", claims.get("userId"));
			logger.info("请求路径：{},用户名:{}", path, claims.getSubject());
		} else {
			logger.error("JWT 解析失败, 请求路径：{}",  path);
			handleTokenInvalid(response, request);
		}

		if (claims != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			UsernamePasswordAuthenticationToken authenticationToken =
					new UsernamePasswordAuthenticationToken(claims.getSubject(), null, Collections.emptyList());
			SecurityContextHolder.getContext().setAuthentication(authenticationToken);
		}

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
