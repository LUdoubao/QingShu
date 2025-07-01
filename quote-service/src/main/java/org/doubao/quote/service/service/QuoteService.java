package org.doubao.quote.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.doubao.quote.service.dto.QuoteDTO;
import org.doubao.quote.service.entity.Quote;

import java.util.List;

public interface QuoteService extends IService<Quote> {
	List<Quote> list();

	Quote addQuote(QuoteDTO dto);
}
