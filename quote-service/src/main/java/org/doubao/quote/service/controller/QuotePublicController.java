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
import java.util.Map;

@RestController
@RequestMapping("/public/quote")
public class QuotePublicController {

	@Autowired
	private QuoteService quoteService;
	@GetMapping("/detail/{id}")
	public Result<QuoteVo> detail(@PathVariable Long id) {
		return quoteService.getDetailById(id);
	}
}

