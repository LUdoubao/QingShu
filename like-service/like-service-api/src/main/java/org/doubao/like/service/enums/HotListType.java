package org.doubao.like.service.enums;

public enum HotListType {
	DAILY("daily", 24),
	WEEKLY("weekly", 168),
	MONTHLY("monthly", 720),
	RISING("rising", 24),
	ALL("all", 0);

	private final String code;
	private final int defaultWindowHours;

	HotListType(String code, int defaultWindowHours) {
		this.code = code;
		this.defaultWindowHours = defaultWindowHours;
	}

	public String getCode() {
		return code;
	}

	public int getDefaultWindowHours() {
		return defaultWindowHours;
	}

	public static HotListType fromCode(String code) {
		if (code == null || code.trim().isEmpty()) {
			return ALL;
		}
		for (HotListType value : values()) {
			if (value.code.equalsIgnoreCase(code.trim())) {
				return value;
			}
		}
		throw new IllegalArgumentException("unsupported hot list type: " + code);
	}
}
