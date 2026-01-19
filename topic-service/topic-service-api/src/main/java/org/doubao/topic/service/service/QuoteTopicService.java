package org.doubao.topic.service.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.doubao.mall.common.dto.TopicBindDTO;
import org.doubao.mall.common.dto.TopicQuoteVO;
import org.doubao.topic.service.dto.TopicQuoteQueryDTO;
import org.doubao.topic.service.entity.QuoteTopic;

import java.util.List;

public interface QuoteTopicService extends IService<QuoteTopic> {
	void deleteQuoteBind(List<Long> quoteIds);

	void bindQuoteToTopic(TopicBindDTO dto);

	Page<TopicQuoteVO> queryTopicQuotes(TopicQuoteQueryDTO queryDTO);
}
