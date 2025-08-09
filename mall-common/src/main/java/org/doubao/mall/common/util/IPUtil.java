package org.doubao.mall.common.util;

import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletRequest;

public class IPUtil {

	// 获取客户端IP地址
	public static String getClientIp(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}

		// 处理多个IP的情况，取第一个
		if (ip != null && ip.contains(",")) {
			ip = ip.split(",")[0].trim();
		}

		return ip;
	}

	// IP脱敏处理，保留前两段，后两段替换为XX
	public static String maskIp(String ip) {
		if (ip == null || ip.isEmpty()) {
			return "";
		}

		String[] parts = ip.split("\\.");
		if (parts.length != 4) {
			return ip;
		}

		return parts[0] + "." + parts[1] + ".XX.XX";
	}
}
