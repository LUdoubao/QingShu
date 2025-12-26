package org.doubao.auth.service.config;

import org.doubao.auth.service.filter.JwtAuthenticationFilter;
import org.doubao.auth.service.filter.JwtAuthenticationFilterLocal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Bean
	@ConditionalOnProperty(name = "service.run-mode", havingValue = "microservice")
	public JwtAuthenticationFilter jwtAuthenticationFilter() {
		return new JwtAuthenticationFilter();
	}
	@Bean
	@ConditionalOnProperty(name = "service.run-mode", havingValue = "monolith", matchIfMissing = true)
	public JwtAuthenticationFilterLocal jwtAuthenticationFilterLocal() {
		return new JwtAuthenticationFilterLocal();
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// ⚙️ 禁用 CSRF 防护（跨站请求伪造保护）
		// 对于 API 网关通常不需要 CSRF，因为通常是前后端分离架构
		http.csrf().disable()
				// 🔐 配置请求授权规则
				.authorizeRequests()
				// ✅ 放行所有以 /auth/ 开头的认证相关接口
				// 通常包括登录、注册、获取令牌等无需认证的端点
				.antMatchers("/auth/**", "/user/login", "/user/register", "/user/verify").permitAll()
				// 🛡️ 其他所有请求都需要认证才能访问
				// 将强制所有其他请求都需要有效的认证凭据
				.anyRequest().authenticated()
				// 结束授权规则配置，连接下一个配置
				.and()
				// 🚦 添加自定义 JWT 验证过滤器
				// 根据运行模式添加对应的过滤器
				.addFilterBefore(getJwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
	}
	
	// 根据运行模式获取对应的 JWT 认证过滤器
	private org.springframework.web.filter.OncePerRequestFilter getJwtAuthenticationFilter() {
		try {
			// 尝试获取微服务模式的过滤器
			JwtAuthenticationFilter microserviceFilter = jwtAuthenticationFilter();
			if (microserviceFilter != null) {
				return microserviceFilter;
			}
		} catch (Exception e) {
			// 如果微服务模式的过滤器不存在，忽略异常，尝试单体模式
		}
		
		// 返回单体模式的过滤器（默认）
		return jwtAuthenticationFilterLocal();
	}
}