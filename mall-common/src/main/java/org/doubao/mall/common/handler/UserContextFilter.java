package org.doubao.mall.common.handler;

import org.doubao.mall.common.util.UserContext;
import org.doubao.mall.common.vo.UserLoginVo;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class UserContextFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request,
									HttpServletResponse response,
									FilterChain filterChain)
			throws ServletException, IOException {
		String userId = request.getHeader("X-User-Id");
		if (userId == null) {
			if (request.getAttribute("X-User-Id") != null) {
				userId = String.valueOf(request.getAttribute("X-User-Id"));
			}
		}
		String username = request.getHeader("X-User-Name");
		if (username == null) {
			if (request.getAttribute("X-User-Name") != null) {
				username = String.valueOf(request.getAttribute("X-User-Name"));
			}
		}

		if (userId != null && username != null) {
			UserLoginVo user = new UserLoginVo();
			user.setId(Long.valueOf(userId));
			user.setUsername(username);
			UserContext.setUser(user);
		}

		try {
			filterChain.doFilter(request, response);
		} finally {
			UserContext.clear();
		}
	}
}