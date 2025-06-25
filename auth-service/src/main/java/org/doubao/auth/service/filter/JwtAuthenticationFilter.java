package org.doubao.auth.service.filter;

import org.doubao.auth.service.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

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
		// 1. 从请求头中获取Authorization字段值
		String authHeader = request.getHeader("Authorization");
		// 2. 检查Authorization头是否符合JWT标准格式（以"Bearer "开头）
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			// 3. 提取纯Token字符串（去掉"Bearer "前缀）
			String token = authHeader.substring(7);
			// 4. 使用JWT工具类从Token中解析出用户名
			//    ⚠️注意：这里只解析不验证Token有效性
			String username = jwtUtil.getUsernameFromToken(token);
			/*
			 * 5. 双重安全验证：
			 *   - 确保从Token中成功解析出用户名
			 *   - 检查SecurityContext中没有已认证对象（防止重复认证）
			 */
			if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
				/*
				 * 6. 创建认证令牌对象：
				 *   - 参数1：用户名作为主体(principal)
				 *   - 参数2：凭证(credentials)设为null（因为JWT已是完整凭证）
				 *   - 参数3：权限集合设为空（实际项目中应从Token中解析角色）
				 */
				UsernamePasswordAuthenticationToken authenticationToken =
						new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
				// 7. 将认证信息存入SecurityContext（标记该请求已认证）
				SecurityContextHolder.getContext().setAuthentication(authenticationToken);
			}
		}
		// 8. 无论是否处理认证，都必须继续过滤器链执行
		//    让后续过滤器或控制器可以处理请求
		filterChain.doFilter(request, response);
	}
}
