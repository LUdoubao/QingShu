package org.doubao.quote.service.service.impl.local;

import org.doubao.mall.common.dto.TopicBindDTO;
import org.doubao.mall.common.vo.TopicNameVo;
import org.doubao.mall.common.entity.Result;
import org.doubao.quote.service.feign.TopicClient;
import org.doubao.topic.service.service.QuoteTopicService;
import org.doubao.topic.service.service.TopicService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "service.run-mode", havingValue = "monolith", matchIfMissing = true)
public class TopicClientLocalImpl implements TopicClient {
	@Resource
	private QuoteTopicService quoteTopicService;
	@Resource
	private TopicService topicService;

	@Override
	public Result<Boolean> deleteQuoteBind(List<Long> quoteIds) {
		quoteTopicService.deleteQuoteBind(quoteIds);
		return Result.success(true);
	}

	@Override
	public Result<Boolean> bindQuoteToTopic(TopicBindDTO dto) {
		quoteTopicService.bindQuoteToTopic(dto);
		return Result.success(true);
	}

	@Override
	public Result<Boolean> updateBindQuote(TopicBindDTO dto) {
		quoteTopicService.updateQuoteBind(dto);
		return Result.success(true);
	}

	@Override
	public Result<TopicNameVo> getNameById(Long id) {
		return topicService.getNameById(id);
	}

	@Override
	public Result<Map<Long, TopicNameVo>> getNameByIds(List<Long> quoteIds) {
		return topicService.getNameByIds(quoteIds);
	}
}