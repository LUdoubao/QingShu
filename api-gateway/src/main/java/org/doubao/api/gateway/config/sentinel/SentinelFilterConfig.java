package org.doubao.api.gateway.config.sentinel;

import com.alibaba.csp.sentinel.adapter.gateway.sc.SentinelGatewayFilter;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class SentinelFilterConfig {
	@Bean
	@Order(-1)
	public GlobalFilter customSentinelFilter() {
		return new SentinelGatewayFilter();
	}
}