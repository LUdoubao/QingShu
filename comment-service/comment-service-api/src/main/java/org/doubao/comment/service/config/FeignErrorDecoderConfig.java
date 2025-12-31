package org.doubao.comment.service.config;

import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.UnknownHostException;

/**
 * Feign错误解码器配置类
 * <p>
 * 配置自定义的Feign错误解码器，用于处理远程服务调用时的异常情况，
 * 包括网络连接超时、服务不可达等异常，并将其转换为更友好的错误信息
 */
@Configuration
public class FeignErrorDecoderConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger(FeignErrorDecoderConfig.class);

	/**
	 * 创建自定义错误解码器
	 * <p>
	 * 该方法创建一个自定义的ErrorDecoder，用于处理Feign客户端调用时的异常情况
	 * 包括网络连接超时、服务不可达等异常，并将其转换为更友好的错误信息
	 * 
	 * @return 自定义的ErrorDecoder实例
	 */
	@Bean
	public ErrorDecoder errorDecoder() {
		return new ErrorDecoder.Default() {
			/**
			 * 解码Feign调用异常
			 * <p>
			 * 根据不同的异常类型，记录日志并返回更友好的异常信息
			 * 主要处理重试异常（网络连接超时）和未知主机异常（服务不可达）
			 * 
			 * @param methodKey 调用的方法键
			 * @param response HTTP响应对象
			 * @return 解码后的异常对象
			 */
			@Override
			public Exception decode(String methodKey, Response response) {
				LOGGER.error("Feign error: method={}, status={}, url={}",
						methodKey, response.status(), response.request().url());

				Exception exception = super.decode(methodKey, response);

				if (exception != null) {
					if (exception instanceof RetryableException) {
						LOGGER.error("Network connection error: {}", exception.getMessage());
						// 网络连接超时异常，返回友好的错误信息
						return new RuntimeException("服务网络连接超时，请稍后重试", exception);
					} else if (exception.getCause() instanceof UnknownHostException) {
						LOGGER.error("Service host unreachable: {}", exception.getMessage());
						// 服务不可达异常，返回友好的错误信息
						return new RuntimeException("服务不可达，请检查网络连接", exception);
					}
				}
				return exception;
			}
		};
	}
}