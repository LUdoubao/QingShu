package org.doubao.topic.service.service.impl.local;

import org.doubao.mall.common.dto.TopicContentDto;
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
    public Result<List<Map<String, Object>>> topicBatch(TopicContentDto dto) {
        try {
            return quoteService.topicBatch(dto);
        } catch (Exception e) {
            return Result.error("quote-service-server is not available");
        }
    }
}