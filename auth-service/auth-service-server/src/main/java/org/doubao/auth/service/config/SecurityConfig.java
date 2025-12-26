package org.doubao.auth.service.config;

import org.doubao.auth.service.filter.JwtAuthenticationFilter;
import org.doubao.auth.service.filter.JwtAuthenticationFilterLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.annotation.PostConstruct;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfig.class);

	@Autowired
	private Environment environment;

	@PostConstruct
	public void init() {
		LOGGER.info("=== SecurityConfig初始化 ===");
		LOGGER.info("当前运行模式: {}", environment.getProperty("service.run-mode", "未设置"));
		LOGGER.info("所有相关属性:");
		LOGGER.info("  service.run-mode: {}", environment.getProperty("service.run-mode"));
		LOGGER.info("  spring.profiles.active: {}", environment.getProperty("spring.profiles.active"));
		LOGGER.info("  logging.level.org.doubao.auth: {}", environment.getProperty("logging.level.org.doubao.auth"));
		LOGGER.info("=== SecurityConfig初始化结束 ===");
	}

	@Bean
	@ConditionalOnProperty(name = "service.run-mode", havingValue = "microservice")
	public JwtAuthenticationFilter jwtAuthenticationFilter() {
		LOGGER.info("✅ Microservice mode enabled.");
		LOGGER.info("✅ 创建JwtAuthenticationFilter实例");
		return new JwtAuthenticationFilter();
	}

	@Bean
	@ConditionalOnProperty(name = "service.run-mode", havingValue = "monolith", matchIfMissing = true)
	public JwtAuthenticationFilterLocal jwtAuthenticationFilterLocal() {
		LOGGER.info("✅ Monolith mode enabled.");
		LOGGER.info("✅ 创建JwtAuthenticationFilterLocal实例");
		return new JwtAuthenticationFilterLocal();
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		LOGGER.info("⚙️ 配置HttpSecurity");

		http.csrf().disable()
				.authorizeRequests()
				.antMatchers("/auth/**", "/user/login", "/user/register", "/user/verify").permitAll()
				.anyRequest().authenticated()
				.and()
				.addFilterBefore(getJwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

		LOGGER.info("✅ HttpSecurity配置完成");
	}

	private org.springframework.web.filter.OncePerRequestFilter getJwtAuthenticationFilter() {
		LOGGER.info("🔄 获取JWT认证过滤器");

		try {
			LOGGER.info("尝试获取微服务模式过滤器...");
			JwtAuthenticationFilter microserviceFilter = jwtAuthenticationFilter();
			if (microserviceFilter != null) {
				LOGGER.info("✅ 使用微服务过滤器");
				return microserviceFilter;
			}
		} catch (Exception e) {
			LOGGER.warn("创建微服务过滤器失败: {}", e.getMessage());
		}

		LOGGER.info("使用单体模式过滤器");
		try {
			return jwtAuthenticationFilterLocal();
		} catch (Exception e) {
			LOGGER.error("创建单体模式过滤器失败", e);
			throw e;
		}
	}
}