package org.doubao.quote.service.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.doubao.mall.common.entity.Result;
import org.doubao.quote.service.dto.PageDto;
import org.doubao.quote.service.dto.QueryDataPageDto;
import org.doubao.quote.service.dto.QuoteDTO;
import org.doubao.quote.service.dto.QuoteUpdateDto;
import org.doubao.quote.service.entity.Quote;
import org.doubao.quote.service.service.QuoteService;
import org.doubao.quote.service.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/quote")
public class QuoteController {

	@Autowired
	private QuoteService quoteService;

	@PostMapping("/page")
	public Result<Page<QuoteVo>> page(@RequestBody PageDto pageDto) {
		return quoteService.page(pageDto);
	}

	@PostMapping("/original")
	public Result<Page<QuoteVo>> originalPage(@RequestBody PageDto pageDto) {
		return quoteService.originalPage(pageDto);
	}

	@PostMapping("/pageManager")
	public Result<Page<QuoteVo>> pageManager(@RequestBody PageDto pageDto) {
		return quoteService.pageManager(pageDto);
	}

	@PostMapping("/create")
	public Result<Void> create(@RequestBody QuoteDTO dto) {
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
	@GetMapping("/updateDetail/{id}")
	public Result<QuoteVo> updateDetail(@PathVariable Long id) {
		return Result.success(quoteService.getUpdateDetail(id));
	}

	@PostMapping("/verify")
	public Result<String> verify(@RequestBody QuoteDTO dto) {
		return quoteService.verify(dto);
	}
	@GetMapping("/verifyDetail/{id}")
	public Result<QuoteVo> verifyDetail(@PathVariable Long id) {
		return quoteService.getVerifyDetailById(id);
	}
	@PostMapping("/verify/list")
	public Result<Page<QuoteVo>> verifyPage(@RequestBody PageDto pageDto) {
		return quoteService.verifyPage(pageDto);
	}


	@PostMapping("/batch")
	public Result<List<Map<String, Object>>> batch(@RequestBody List<Long> ids) {
		return quoteService.batch(ids);
	}

	@GetMapping("/{quoteId}/type")
	public String getQuoteType(@PathVariable("quoteId") String quoteId) {
		return quoteService.getQuoteType(quoteId);
	}

	@PostMapping("/inner/exists")
	public boolean checkQuoteExists(@RequestBody Map<String, String> request) {
		return quoteService.checkQuoteExists(request);
	}

	@GetMapping("/search/suggestion")
	public Result<Map<String, String>> getSearchSuggestions(@RequestParam("keyword") String keyword) {
		return quoteService.getSearchSuggestions(keyword);
	}

	@GetMapping("/search/type")
	Result<Map<String, Object>> searchQuotes(@RequestParam("keyword") String keyword,
								@RequestParam("page") int page, @RequestParam("size") int size,
								@RequestParam("currentUserId") Long currentUserId,
								@RequestParam("type") String type) {
		return quoteService.search(keyword, page, size, type, currentUserId);
	}



	@PostMapping("/updateStatus")
	Result<Void> updateStatus(@RequestBody Map<String, String> request) {
		quoteService.updateStatus(request);
		return Result.success();
	}
	@PostMapping("/queryQuoteData")
	public Result<Page<QuoteDataVo>> queryQuoteData(@RequestBody QueryDataPageDto queryDataPageDto) {
		return Result.success(quoteService.queryQuoteData(queryDataPageDto));
	}

	@GetMapping("/queryStatusCount")
	public Result<QuoteStatusCountVo> queryStatusCount() {
		return Result.success(quoteService.queryStatusCount());
	}
	@GetMapping("/queryContentOverview")
	public Result<ContentOverviewVo> queryContentOverview() {
		return Result.success(quoteService.queryContentOverview());
	}

	@GetMapping("/queryContentTrend")
	public Result<List<ContentTrendVo>> queryContentTrend(
			@RequestParam int days,  // 最近天数：7/14/30
			@RequestParam(required = false) List<String> metrics) {
		return Result.success(quoteService.queryContentTrend(days, metrics));
	}

	@PostMapping("/updateStatus")
	public Result<Void> updateQuoteStatus(@RequestParam Long quoteId, @RequestParam Integer status) {
		quoteService.updateQuoteStatus(quoteId, status);
		return Result.success();
	}

	@PostMapping("/saveAsDraft")
	public Result<Long> saveAsDraft(@RequestBody QuoteDTO dto) {
		return Result.success(quoteService.saveAsDraft(dto));
	}
}

