package org.doubao.mall.common.handler;

import org.doubao.mall.common.entity.UserInfo;
import org.doubao.mall.common.util.UserContext;
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
		String username = request.getHeader("X-User-Name");

		if (userId != null && username != null) {
			UserInfo user = new UserInfo();
			user.setId(userId);
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