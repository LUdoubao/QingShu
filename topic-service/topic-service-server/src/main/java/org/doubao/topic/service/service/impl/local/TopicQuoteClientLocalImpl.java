package org.doubao.topic.service.service.impl.local;

import org.doubao.mall.common.entity.Result;
import org.doubao.quote.service.service.QuoteService;
import org.doubao.topic.service.feign.QuoteClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@ConditionalOnProperty(name = "service.run-mode", havingValue = "monolith", matchIfMissing = true)
public class TopicQuoteClientLocalImpl implements QuoteClient {

    @Resource
    private QuoteService quoteService;

}