package org.doubao.quote.service.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.doubao.mall.common.entity.Result;
import org.doubao.quote.service.dto.PageDto;
import org.doubao.quote.service.dto.QuoteDTO;
import org.doubao.quote.service.dto.QuoteUpdateDto;
import org.doubao.quote.service.entity.Quote;
import org.doubao.quote.service.vo.QuoteVo;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface QuoteService extends IService<Quote> {
	Result<Page<QuoteVo>> page(PageDto pageDto);

	Result<Quote> addQuote(QuoteDTO dto);

	Result<String> deleteQuote(List<Long> quoteIds);

	Result<String> updateQuote(QuoteUpdateDto dto);

	Result<QuoteVo> getDetailById(Long id);
}
