package org.doubao.comment.service.config;

import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.UnknownHostException;

@Configuration
public class FeignErrorDecoderConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger(FeignErrorDecoderConfig.class);

	@Bean
	public ErrorDecoder errorDecoder() {
		return new ErrorDecoder.Default() {
			@Override
			public Exception decode(String methodKey, Response response) {
				LOGGER.error("Feign error: method={}, status={}, url={}",
						methodKey, response.status(), response.request().url());

				Exception exception = super.decode(methodKey, response);

				if (exception != null) {
					if (exception instanceof RetryableException) {
						LOGGER.error("Network connection error: {}", exception.getMessage());
						return new RuntimeException("服务网络连接超时，请稍后重试", exception);
					} else if (exception.getCause() instanceof UnknownHostException) {
						LOGGER.error("Service host unreachable: {}", exception.getMessage());
						return new RuntimeException("服务不可达，请检查网络连接", exception);
					}
				}
				return exception;
			}
		};
	}
}
