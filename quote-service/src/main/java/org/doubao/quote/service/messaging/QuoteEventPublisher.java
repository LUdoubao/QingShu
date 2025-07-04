package org.doubao.quote.service.messaging;

import org.doubao.quote.service.dto.QuoteUpdateDto;
import org.doubao.quote.service.entity.Quote;
import org.doubao.quote.service.entity.QuoteTag;
import org.doubao.quote.service.vo.QuoteVo;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class QuoteEventPublisher {

	@Autowired
	private RabbitTemplate rabbitTemplate;
	public static final String QUOTE_EXCHANGE = "quote.exchange";

	public void publishQuoteVerify(QuoteUpdateDto quoteUpdateDto) {
		// 拼接quoteUpdateDto json字符串
		String quoteUpdateDtoJson = "{\"quoteId\":\"" + quoteUpdateDto.getQuoteId() + "\",\"AQuoteVo\":\""
				+ quoteUpdateDto.getAfterQuoteVo().toString() + "\",\"BQuoteVo\":\"" + quoteUpdateDto.getBeforeQuoteVo().toString() + "\"}";
		rabbitTemplate.convertAndSend(QUOTE_EXCHANGE, "quote.verify", quoteUpdateDtoJson);
	}

	public void publishQuoteAdd(Quote quote, List<QuoteTag> quoteTags) {
		// 拼接quoteUpdateDto json字符串
		String quoteJson = "{\"id\":\"" + quote.getId() + "\",\"content\":\"" + quote.getContent() + "\",\"author\":\""
				+ quote.getAuthor() + "\",\"source\":\"" + quote.getSource() + "\",\"categoryId\":\""
				+ quote.getCategoryId() + "\",\"tagIds\":\"" + quoteTags + "\"}";
		rabbitTemplate.convertAndSend(QUOTE_EXCHANGE, "quote.add", quoteJson);
	}
}