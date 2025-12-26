package org.doubao.dialog.service.config;

import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.UnknownHostException;

// 配置类，使 Feign 所有异常触发熔断
@Configuration
public class DialogFeignErrorDecoderConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger(DialogFeignErrorDecoderConfig.class);
	@Bean
	public ErrorDecoder errorDecoder() {
		return new ErrorDecoder.Default() {
			@Override
			public Exception decode(String methodKey, Response response) {
				LOGGER.error("feign error: {}", response);
				Exception exception = super.decode(methodKey, response);
				// 如果是不可达/UnknownHost，包裹为RuntimeException
				if (exception != null && (exception instanceof RetryableException ||
						(exception.getCause() != null && exception.getCause() instanceof UnknownHostException))) {
					LOGGER.error("feign error exception: {}", exception.getMessage());
					return new RuntimeException("服务不可用", exception);
				}
				return exception;
			}
		};
	}
}