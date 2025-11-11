package org.doubao.mall.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ConvertUtil {
	private static final Logger log = LoggerFactory.getLogger(ConvertUtil.class);

	public static LocalDate safeParseLocalDate(Object obj) {
		if (obj == null) {
			return null;
		}

		try {
			if (obj instanceof LocalDate) {
				return (LocalDate) obj;
			} else if (obj instanceof String) {
				return LocalDate.parse((String) obj);
			} else {
				return LocalDate.parse(obj.toString());
			}
		} catch (Exception e) {
			log.error("解析LocalDate失败: {}", obj);
			return null;
		}
	}
	public static List<LocalDate> safeConvertToListOfLocalDate(Object obj) {
		if (obj == null) {
			return new ArrayList<>();
		}

		List<LocalDate> result = new ArrayList<>();
		if (obj instanceof List) {
			for (Object item : (List<?>) obj) {
				if (item != null) {
					try {
						if (item instanceof LocalDate) {
							result.add((LocalDate) item);
						} else if (item instanceof String) {
							result.add(LocalDate.parse((String) item));
						} else {
							result.add(LocalDate.parse(item.toString()));
						}
					} catch (Exception e) {
						log.error("无法转换为LocalDate: {}", item);
					}
				}
			}
		}
		return result;
	}

	public static List<Long> safeConvertToListOfLong(Object obj) {
		if (obj == null) {
			return new ArrayList<>();
		}

		List<Long> result = new ArrayList<>();
		if (obj instanceof List) {
			for (Object item : (List<?>) obj) {
				if (item != null) {
					try {
						if (item instanceof Long) {
							result.add((Long) item);
						} else if (item instanceof Integer) {
							result.add(((Integer) item).longValue());
						} else if (item instanceof String) {
							result.add(Long.valueOf((String) item));
						} else {
							result.add(Long.valueOf(item.toString()));
						}
					} catch (NumberFormatException e) {
						log.error("无法转换为Long: {}", item);
					}
				}
			}
		}
		return result;
	}

	public static Long safeParseLong(Object obj) {
		if (obj == null) {
			return 0L;
		}

		try {
			if (obj instanceof Long) {
				return (Long) obj;
			} else if (obj instanceof Integer) {
				return ((Integer) obj).longValue();
			} else if (obj instanceof String) {
				return Long.valueOf((String) obj);
			} else {
				return Long.valueOf(obj.toString());
			}
		} catch (NumberFormatException e) {
			log.error("解析Long失败: {}", obj);
			return 0L;
		}
	}
}
