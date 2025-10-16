package org.doubao.user.server.report.service.impl;

import org.doubao.user.server.report.constant.ReportConstant;
import org.doubao.user.server.report.service.AIPreCheckService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * AI预检测服务默认实现（基于关键词匹配）
 */
@Service
public class DefaultAIPreCheckServiceImpl implements AIPreCheckService {

	// 高风险关键词库
	private static final List<String> HIGH_RISK_KEYWORDS = Arrays.asList(
			"违法", "暴力", "色情", "诈骗", "赌博"
	);

	// 低风险关键词库
	private static final List<String> LOW_RISK_KEYWORDS = Arrays.asList(
			"不友善", "低俗", "广告", "重复内容"
	);

	@Override
	public Integer preCheck(Long reportId, String content) {
		if (content == null || content.trim().isEmpty()) {
			return ReportConstant.RISK_LEVEL_NORMAL;
		}

		// 检测高风险关键词
		for (String keyword : HIGH_RISK_KEYWORDS) {
			if (content.contains(keyword)) {
				return ReportConstant.RISK_LEVEL_HIGH;
			}
		}

		// 检测低风险关键词
		for (String keyword : LOW_RISK_KEYWORDS) {
			if (content.contains(keyword)) {
				return ReportConstant.RISK_LEVEL_LOW;
			}
		}

		return ReportConstant.RISK_LEVEL_NORMAL;
	}
}