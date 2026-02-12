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
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfig.class);
	private static final String[] EXCLUDE_URLS = {
			"/auth/**",
			"/user/login",
			"/user/register",
			"/user/verify",
			"/user/forgot-password",
			"/public/**",
			"/dialog/ws/**"
	};
	@Autowired
	private Environment environment;

	@Bean
	@ConditionalOnProperty(name = "service.run-mode", havingValue = "microservice")
	public JwtAuthenticationFilter jwtAuthenticationFilter() {
		LOGGER.info("✅ Microservice mode enabled.");
		return new JwtAuthenticationFilter();
	}

	@Bean
	@ConditionalOnProperty(name = "service.run-mode", havingValue = "monolith", matchIfMissing = true)
	public JwtAuthenticationFilterLocal jwtAuthenticationFilterLocal() {
		LOGGER.info("✅ Monolith mode enabled.");
		return new JwtAuthenticationFilterLocal();
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		LOGGER.info("⚙️ 配置HttpSecurity");

		http.csrf().disable()
				.authorizeRequests()
				.antMatchers(EXCLUDE_URLS).permitAll()
				.anyRequest().authenticated()
				.and()
				.addFilterBefore(getJwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

		LOGGER.info("✅ HttpSecurity配置完成");
	}

	private org.springframework.web.filter.OncePerRequestFilter getJwtAuthenticationFilter() {
		try {
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