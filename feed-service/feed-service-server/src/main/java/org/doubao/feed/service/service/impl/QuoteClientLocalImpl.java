package org.doubao.feed.service.service.impl;

import org.doubao.feed.service.feign.QuoteClient;
import org.doubao.mall.common.condition.MonolithMode;
import org.doubao.mall.common.entity.Result;
import org.doubao.quote.service.service.QuoteService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
@MonolithMode
public class QuoteClientLocalImpl implements QuoteClient {

	@Resource
	private QuoteService quoteService;

	@Override
	public Result<List<Map<String, Object>>> batch(List<Long> ids) {
		try {
			return quoteService.batch(ids);
		} catch (Exception e) {
			return Result.error("批量获取内容信息失败: " + e.getMessage());
		}
	}
}