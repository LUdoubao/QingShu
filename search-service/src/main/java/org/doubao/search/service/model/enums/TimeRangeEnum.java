package org.doubao.search.service.model.enums;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

/**
 * 时间范围枚举类
 * 定义常用的时间筛选范围，用于搜索和统计功能中的时间条件过滤
 */
public enum TimeRangeEnum {

	/**
	 * 今天：从今天0点到现在
	 */
	TODAY("today", "今天", 0, java.time.temporal.ChronoUnit.DAYS),

	/**
	 * 近7天：从7天前的此刻到现在
	 */
	LAST_7_DAYS("last7days", "近7天", 7, java.time.temporal.ChronoUnit.DAYS),

	/**
	 * 近30天：从30天前的此刻到现在
	 */
	LAST_30_DAYS("last30days", "近30天", 30, java.time.temporal.ChronoUnit.DAYS),

	/**
	 * 本周：从本周一0点到现在
	 */
	THIS_WEEK("thisweek", "本周", 1, java.time.temporal.ChronoUnit.WEEKS),

	/**
	 * 本月：从本月1号0点到现在
	 */
	THIS_MONTH("thismonth", "本月", 1, java.time.temporal.ChronoUnit.MONTHS),

	/**
	 * 本季度：从本季度第一天0点到现在
	 */
	THIS_QUARTER("thisquarter", "本季度", 1, java.time.temporal.ChronoUnit.MONTHS),

	/**
	 * 本年：从本年1月1号0点到现在
	 */
	THIS_YEAR("thisyear", "本年", 1, java.time.temporal.ChronoUnit.YEARS);

	/**
	 * 时间范围编码，用于前端传递参数和后端识别
	 */
	private final String code;

	/**
	 * 时间范围描述，用于前端展示
	 */
	private final String desc;

	/**
	 * 时间数量，结合时间单位使用
	 */
	private final int amount;

	/**
	 * 时间单位（天、周、月、年等）
	 */
	private final TemporalUnit unit;

	TimeRangeEnum(String code, String desc, int amount, ChronoUnit chronoUnit) {
		this.code = code;
		this.desc = desc;
		this.amount = amount;
		this.unit = chronoUnit;
	}

	public String getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}

	public int getAmount() {
		return amount;
	}

	public TemporalUnit getUnit() {
		return unit;
	}

	/**
	 * 根据编码获取对应的时间范围枚举
	 * 用于将前端传递的字符串参数转换为枚举实例
	 *
	 * @param code 时间范围编码
	 * @return 对应的时间范围枚举，如果未找到则返回null
	 */
	public static TimeRangeEnum getByCode(String code) {
		if (code == null || code.trim().isEmpty()) {
			return null;
		}

		for (TimeRangeEnum range : values()) {
			if (range.code.equalsIgnoreCase(code.trim())) {
				return range;
			}
		}
		return null;
	}

	/**
	 * 获取该时间范围的开始时间
	 * 根据不同的时间范围类型，计算对应的起始时间点
	 *
	 * @return 时间范围的开始时间
	 */
	public LocalDateTime getStartTime() {
		LocalDateTime now = LocalDateTime.now();

		switch (this) {
			case TODAY:
				// 今天：取当天0点
				return now.toLocalDate().atStartOfDay();
			case THIS_WEEK:
				// 本周：取本周一0点
				int dayOfWeek = now.getDayOfWeek().getValue();
				return now.minusDays(dayOfWeek - 1).toLocalDate().atStartOfDay();
			case THIS_MONTH:
				// 本月：取本月1号0点
				return now.withDayOfMonth(1).toLocalDate().atStartOfDay();
			case THIS_QUARTER:
				// 本季度：取本季度第一天0点
				int month = now.getMonthValue();
				int firstMonthOfQuarter = (month - 1) / 3 * 3 + 1;
				return now.withMonth(firstMonthOfQuarter).withDayOfMonth(1).toLocalDate().atStartOfDay();
			case THIS_YEAR:
				// 本年：取本年1月1号0点
				return now.withMonth(1).withDayOfMonth(1).toLocalDate().atStartOfDay();
			default:
				// 近N天：从N天前的此刻开始
				return now.minus(amount, unit);
		}
	}

	/**
	 * 获取该时间范围的结束时间
	 * 统一返回当前时间
	 *
	 * @return 时间范围的结束时间（当前时间）
	 */
	public LocalDateTime getEndTime() {
		return LocalDateTime.now();
	}
}
