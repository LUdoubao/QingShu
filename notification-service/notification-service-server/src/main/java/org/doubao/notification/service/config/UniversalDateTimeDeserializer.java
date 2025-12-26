package org.doubao.notification.service.config;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.List;


public class UniversalDateTimeDeserializer implements ObjectDeserializer {

	@Override
	public LocalDateTime deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
		Object value = parser.parse();

		try {
			if (value instanceof List) {
				List<?> dateArray = (List<?>) value;
				return parseFromArray(dateArray);
			} else if (value instanceof String) {
				String dateStr = (String) value;

				// 尝试处理带时区和不带时区的格式
				if (dateStr.contains("T")) {
					return LocalDateTime.parse(dateStr);
				} else {
					// 替换空格为T以符合ISO格式
					return LocalDateTime.parse(dateStr.replace(' ', 'T'));
				}
			} else {
				throw new RuntimeException("Unsupported date format: " + value.getClass().getName());
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to parse date: " + value, e);
		}
	}

	private LocalDateTime parseFromArray(List<?> dateArray) {
		if (dateArray.size() < 6) {
			throw new RuntimeException("Invalid date array, length: " + dateArray.size());
		}

		return LocalDateTime.of(
				parseNumber(dateArray.get(0)),
				parseNumber(dateArray.get(1)),
				parseNumber(dateArray.get(2)),
				parseNumber(dateArray.get(3)),
				parseNumber(dateArray.get(4)),
				parseNumber(dateArray.get(5)),
				dateArray.size() > 6 ? parseNanos(dateArray.get(6)) : 0
		);
	}

	private int parseNumber(Object num) {
		if (num instanceof Number) {
			return ((Number) num).intValue();
		} else if (num instanceof String) {
			return Integer.parseInt((String) num);
		}
		throw new RuntimeException("Invalid number value: " + num);
	}

	private int parseNanos(Object nano) {
		if (nano instanceof Number) {
			return ((Number) nano).intValue();
		} else if (nano instanceof String) {
			String s = (String) nano;
			if (s.length() > 9) s = s.substring(0, 9); // 防止溢出
			return Integer.parseInt(s);
		}
		throw new RuntimeException("Invalid nano value: " + nano);
	}

	@Override
	public int getFastMatchToken() {
		return 0;
	}
}