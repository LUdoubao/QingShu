package org.doubao.topic.service.service.impl.local;

import org.doubao.mall.common.entity.Result;
import org.doubao.quote.service.service.QuoteService;
import org.doubao.topic.service.feign.QuoteClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "service.run-mode", havingValue = "monolith", matchIfMissing = true)
public class TopicQuoteClientLocalImpl implements QuoteClient {

    @Resource
    @Lazy
    private QuoteService quoteService;

    @Override
    public Result<List<Map<String, Object>>> topicBatch(List<Long> ids) {
        try {
            return quoteService.topicBatch(ids);
        } catch (Exception e) {
            return Result.error("quote-service-server is not available");
        }
    }
}