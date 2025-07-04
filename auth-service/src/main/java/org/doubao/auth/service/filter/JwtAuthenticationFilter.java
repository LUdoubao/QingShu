package org.doubao.auth.service.filter;

import io.jsonwebtoken.Claims;
import org.doubao.auth.service.utils.JwtUtil;
import org.doubao.mall.common.entity.UserInfo;
import org.doubao.mall.common.util.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	@Autowired
	private JwtUtil jwtUtil;

	@Override
	protected void doFilterInternal(HttpServletRequest request,
									HttpServletResponse response,
									FilterChain filterChain)
			throws ServletException, IOException {
		String userId = request.getHeader("X-User-Id");
		String username = request.getHeader("X-User-Name");
		if (userId != null && username != null) {
			UserInfo user = new UserInfo();
			user.setUserId(userId);
			user.setUsername(username);
			UserContext.setUser(user);
		}

		// 1. 从请求头中获取Authorization字段值
		String authHeader = request.getHeader("Authorization");
		// 2. 检查Authorization头是否符合JWT标准格式（以"Bearer "开头）
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			// 3. 提取纯Token字符串（去掉"Bearer "前缀）
			String token = authHeader.substring(7);
			// 4. 使用JWT工具类从Token中解析出用户名
			Claims claims = jwtUtil.getUsernameFromToken(token);

			if (claims != null && SecurityContextHolder.getContext().getAuthentication() == null) {
				UsernamePasswordAuthenticationToken authenticationToken =
						new UsernamePasswordAuthenticationToken(claims.getSubject(), null, Collections.emptyList());
				// 7. 将认证信息存入SecurityContext（标记该请求已认证）
				SecurityContextHolder.getContext().setAuthentication(authenticationToken);
			}
		}
		// 8. 无论是否处理认证，都必须继续过滤器链执行
		//    让后续过滤器或控制器可以处理请求
		filterChain.doFilter(request, response);
	}
}
