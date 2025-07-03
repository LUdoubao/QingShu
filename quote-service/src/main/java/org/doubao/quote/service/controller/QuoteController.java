package org.doubao.quote.service.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.doubao.mall.common.entity.Result;
import org.doubao.quote.service.dto.PageDto;
import org.doubao.quote.service.dto.QuoteDTO;
import org.doubao.quote.service.entity.Quote;
import org.doubao.quote.service.service.QuoteService;
import org.doubao.quote.service.vo.QuoteVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/quote")
public class QuoteController {

	@Autowired
	private QuoteService quoteService;

	@PostMapping("/page")
	public Result<Page<QuoteVo>> page(@RequestBody PageDto pageDto) {
		return quoteService.page(pageDto);
	}

	@PostMapping("create")
	public Quote create(@RequestBody QuoteDTO dto) {
		return quoteService.addQuote(dto);
	}
}

