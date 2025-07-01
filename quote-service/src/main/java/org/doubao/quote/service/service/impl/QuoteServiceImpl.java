package org.doubao.quote.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.doubao.quote.service.dto.QuoteDTO;
import org.doubao.quote.service.entity.Quote;
import org.doubao.quote.service.mapper.QuoteMapper;
import org.doubao.quote.service.service.QuoteService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class QuoteServiceImpl extends ServiceImpl<QuoteMapper, Quote> implements QuoteService {


	@Override
	public List<Quote> list() {
		return this.list(null);
	}

	@Override
	public Quote addQuote(QuoteDTO dto) {
		Quote q = new Quote();
		q.setContent(dto.getContent());
		q.setAuthor(dto.getAuthor());
		q.setCreatedTime(LocalDateTime.now());
		this.save(q);
		return q;
	}
}
