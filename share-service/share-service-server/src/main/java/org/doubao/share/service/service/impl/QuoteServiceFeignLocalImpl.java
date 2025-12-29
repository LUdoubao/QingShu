package org.doubao.share.service.service.impl;

import org.doubao.quote.service.service.QuoteService;
import org.doubao.share.service.feign.QuoteServiceFeign;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "service.run-mode", havingValue = "monolith", matchIfMissing = true)
public class QuoteServiceFeignLocalImpl implements QuoteServiceFeign {

	@Resource
	private QuoteService quoteService;

	@Override
	public String getQuoteType(String quoteId) {
		return quoteService.getQuoteType(quoteId);
	}

	@Override
	public boolean checkQuoteExists(Map<String, String> request) {
		return quoteService.checkQuoteExists(request);
	}
}