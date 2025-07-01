package org.doubao.quote.service.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.doubao.mall.common.entity.Result;
import org.doubao.quote.service.dto.QuoteDTO;
import org.doubao.quote.service.entity.Quote;
import org.doubao.quote.service.service.QuoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/quote")
public class QuoteController {

	@Autowired
	private QuoteService quoteService;

	@GetMapping("/page")
	@SuppressWarnings("unchecked")
	public Result<Page<Quote>> page(
			@RequestParam(defaultValue = "1") Integer page,
			@RequestParam(defaultValue = "10") Integer size
	) {
		Page<Quote> pageData = quoteService.page(new Page<>(page, size),
				new LambdaQueryWrapper<Quote>()
						.eq(Quote::getDeleted, 0)
						.orderByDesc(Quote::getCreatedTime));

		return Result.success(pageData);
	}

	@PostMapping("create")
	public Quote create(@RequestBody QuoteDTO dto) {
		return quoteService.addQuote(dto);
	}
}

