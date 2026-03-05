package org.doubao.like.service.service.impl.local;

import org.doubao.like.service.feign.QuoteClient;
import org.doubao.mall.common.condition.MonolithMode;
import org.doubao.mall.common.entity.Result;
import org.doubao.quote.service.service.QuoteService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
@MonolithMode
public class LikeQuoteClientLocalImpl implements QuoteClient {

	@Lazy
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