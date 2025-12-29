package org.doubao.search.service.service.impl.local;

import org.doubao.mall.common.entity.Result;
import org.doubao.quote.service.service.QuoteService;
import org.doubao.search.service.feign.QuoteClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "service.run-mode", havingValue = "monolith", matchIfMissing = true)
public class SearchQuoteClientLocalImpl implements QuoteClient {

	@Resource
	private QuoteService quoteService;

	@Override
	public Result<Map<String, String>> getSearchSuggestions(String keyword) {
		try {
			return quoteService.getSearchSuggestions(keyword);
		} catch (Exception e) {
			return Result.error("获取搜索建议失败: " + e.getMessage());
		}
	}

	@Override
	public Result<Map<String, Object>> searchQuotes(String keyword, int page, int size, Long currentUserId, String type) {
		try {
			return quoteService.search(keyword, page, size, type, currentUserId);
		} catch (Exception e) {
			return Result.error("搜索引文失败: " + e.getMessage());
		}
	}
}