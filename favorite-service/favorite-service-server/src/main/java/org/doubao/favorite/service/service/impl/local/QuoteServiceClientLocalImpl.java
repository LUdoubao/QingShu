package org.doubao.favorite.service.service.impl.local;

import org.doubao.favorite.service.feign.QuoteServiceClient;
import org.doubao.mall.common.entity.Result;
import org.doubao.quote.service.service.QuoteService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "service.run-mode", havingValue = "monolith", matchIfMissing = true)
public class QuoteServiceClientLocalImpl implements QuoteServiceClient {

	@Resource
	private QuoteService quoteService;

	@Override
	public Result<List<Map<String, Object>>> getQuotesByIds(List<Long> ids) {
		try {
			return quoteService.batch(ids);
		} catch (Exception e) {
			return Result.error("批量获取文案信息失败: " + e.getMessage());
		}
	}
}