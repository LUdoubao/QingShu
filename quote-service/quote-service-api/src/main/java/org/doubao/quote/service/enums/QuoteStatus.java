package org.doubao.quote.service.enums;

import java.util.ArrayList;
import java.util.List;

/**
 * 状态，0:审核中1:已发布 2:屏蔽 3:草稿 4:未通过 5:下架
 */
public enum QuoteStatus {
	AUDITING(0, "审核中"),
	PUBLISHED(1, "已发布"),
	BLOCKED(2, "屏蔽"),
	DRAFT(3, "草稿"),
	NOT_PASS(4, "未通过"),
	OFF_SHELF(5, "下架");
	private final int code;
	private final String name;
	// 状态集合
	QuoteStatus(int code, String name) {
		this.code = code;
		this.name = name;
	}
	public int getCode() {
		return code;
	}
	public String getName() {
		return name;
	}
	public static QuoteStatus getQuoteStatus(int code) {
		for (QuoteStatus status : QuoteStatus.values()) {
			if (status.getCode() == code) {
				return status;
			}
		}
		return null;
	}

	public static List<Integer> noList() {
		List<Integer> noList = new ArrayList<>();
		noList.add(AUDITING.code);
		noList.add(BLOCKED.code);
		noList.add(OFF_SHELF.code);
		return noList;
	}
}
