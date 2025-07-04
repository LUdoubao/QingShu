package org.doubao.quote.service.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.doubao.mall.common.entity.Result;
import org.doubao.quote.service.dto.PageDto;
import org.doubao.quote.service.dto.QuoteDTO;
import org.doubao.quote.service.dto.QuoteUpdateDto;
import org.doubao.quote.service.entity.Quote;
import org.doubao.quote.service.service.QuoteService;
import org.doubao.quote.service.vo.QuoteVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/quote")
public class QuoteController {

	@Autowired
	private QuoteService quoteService;

	@PostMapping("/page")
	public Result<Page<QuoteVo>> page(@RequestBody PageDto pageDto) {
		return quoteService.page(pageDto);
	}

	@PostMapping("/pageManager")
	public Result<Page<QuoteVo>> pageManager(@RequestBody PageDto pageDto) {
		return quoteService.pageManager(pageDto);
	}

	@PostMapping("create")
	public Result<Quote> create(@RequestBody QuoteDTO dto) {
		return quoteService.addQuote(dto);
	}

	@PostMapping("/delete")
	public Result<String> delete(@RequestBody List<Long> quoteIds) {
		return quoteService.deleteQuote(quoteIds);
	}

	@PostMapping("/update")
	public Result<String> update(@RequestBody QuoteUpdateDto dto) {
		return quoteService.updateQuote(dto);
	}

	@GetMapping("/detail/{id}")
	public Result<QuoteVo> detail(@PathVariable Long id) {
		return quoteService.getDetailById(id);
	}
}

