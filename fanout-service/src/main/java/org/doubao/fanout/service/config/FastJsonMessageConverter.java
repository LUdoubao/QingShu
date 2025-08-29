package org.doubao.fanout.service.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.AbstractMessageConverter;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.lang.NonNullApi;

public class FastJsonMessageConverter extends AbstractMessageConverter {

	public static final String DEFAULT_CHARSET = "UTF-8";
	private volatile String defaultCharset = DEFAULT_CHARSET;

	// 配置FastJSON特性
	private SerializerFeature[] serializerFeatures = new SerializerFeature[]{
			SerializerFeature.WriteClassName,  // 写入类名信息
			SerializerFeature.WriteDateUseDateFormat  // 日期格式化
	};

	private Feature[] deserializerFeatures = new Feature[]{
			Feature.SupportAutoType,  // 支持自动类型识别
			Feature.AllowComment,     // 允许注释
			Feature.AllowSingleQuotes // 允许单引号
	};

	public FastJsonMessageConverter() {
		super();
	}

	@Override
	protected Message createMessage(Object object, MessageProperties messageProperties) {
		byte[] bytes = null;
		try {
			String jsonString = JSON.toJSONString(object, serializerFeatures);
			bytes = jsonString.getBytes(this.defaultCharset);
		} catch (Exception e) {
			throw new MessageConversionException("Failed to convert to JSON", e);
		}
		messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
		messageProperties.setContentEncoding(this.defaultCharset);
		if (bytes != null) {
			messageProperties.setContentLength(bytes.length);
		}
		return new org.springframework.amqp.core.Message(bytes, messageProperties);
	}

	@Override
	public Object fromMessage(org.springframework.amqp.core.Message message)
			throws MessageConversionException {

		Object content = null;
		MessageProperties properties = message.getMessageProperties();
		if (properties != null) {
			String contentType = properties.getContentType();
			if (contentType != null && contentType.contains("json")) {
				String encoding = properties.getContentEncoding();
				if (encoding == null) {
					encoding = this.defaultCharset;
				}
				try {
					// 关键：处理Java序列化对象的情况
					if ("application/x-java-serialized-object".equals(contentType)) {
						throw new MessageConversionException(
								"Java serialized objects not supported. Expected JSON content.");
					}

					String json = new String(message.getBody(), encoding);
					// 使用配置的反序列化特性
					return JSON.parseObject(json, Object.class, deserializerFeatures);
				} catch (Exception e) {
					throw new MessageConversionException("Failed to parse JSON", e);
				}
			}
		}
		return content;
	}

	// 配置方法
	public void setSerializerFeatures(SerializerFeature... features) {
		this.serializerFeatures = features;
	}

	public void setDeserializerFeatures(Feature... features) {
		this.deserializerFeatures = features;
	}

	public void setDefaultCharset(String charset) {
		this.defaultCharset = charset;
	}
}
