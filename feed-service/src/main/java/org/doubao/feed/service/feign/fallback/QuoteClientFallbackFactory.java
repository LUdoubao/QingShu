package org.doubao.feed.service.feign.fallback;

import org.doubao.feed.service.feign.QuoteClient;
import org.doubao.feed.service.model.dto.ContentStatsDTO;
import org.doubao.mall.common.entity.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class QuoteClientFallbackFactory implements FallbackFactory<QuoteClient> {

	private static final Logger log =  LoggerFactory.getLogger(QuoteClientFallbackFactory.class);

	@Override
	public QuoteClient create(Throwable cause) {
		return new QuoteClient() {

			@Override
			public Result<List<Map<String, Object>>> batch(List<Long> ids) {
				log.error("[QuoteClient] getContentsByIds fallback, contentIds: {}, cause: {}", ids, cause.getMessage());
				return Result.success(Collections.emptyList());
			}
		};
	}
}
