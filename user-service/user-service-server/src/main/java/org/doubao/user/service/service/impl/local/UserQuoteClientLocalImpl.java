package org.doubao.user.service.service.impl.local;

import org.doubao.mall.common.condition.MonolithMode;
import org.doubao.mall.common.entity.Result;
import org.doubao.quote.service.service.QuoteService;
import org.doubao.user.service.feign.report.QuoteClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

@Service
@MonolithMode
public class UserQuoteClientLocalImpl implements QuoteClient {

	@Resource
	@Lazy
	private QuoteService quoteService;

	@Override
	public Result<Void> updateStatus(Map<String, String> request) {
		try {
			quoteService.updateStatus(request);
			return Result.success();
		} catch (Exception e) {
			return Result.error("更新引文状态失败: " + e.getMessage());
		}
	}
}