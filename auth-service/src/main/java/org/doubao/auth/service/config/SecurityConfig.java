package org.doubao.auth.service.config;

import lombok.RequiredArgsConstructor;
import org.doubao.auth.service.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private JwtAuthenticationFilter jwtAuthenticationFilter;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
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
				.antMatchers("/auth/**").permitAll()
				// 🛡️ 其他所有请求都需要认证才能访问
				// 将强制所有其他请求都需要有效的认证凭据
				.anyRequest().authenticated()
				// 结束授权规则配置，连接下一个配置
				.and()
				// 🚦 添加自定义 JWT 验证过滤器
				// 将自定义的 jwtAuthenticationFilter 添加到过滤器链中
				// 位置在 UsernamePasswordAuthenticationFilter 之前执行
				// 这样可以在传统的用户名/密码认证前进行JWT验证
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
	}
}
