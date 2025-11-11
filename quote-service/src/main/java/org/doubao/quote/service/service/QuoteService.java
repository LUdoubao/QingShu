package org.doubao.quote.service.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.doubao.mall.common.entity.Result;
import org.doubao.quote.service.dto.*;
import org.doubao.quote.service.entity.Quote;
import org.doubao.quote.service.vo.*;

import java.util.List;
import java.util.Map;

public interface QuoteService extends IService<Quote> {
	Result<Page<QuoteVo>> page(PageDto pageDto);

	Result<Void> addQuote(QuoteDTO dto);

	Result<String> deleteQuote(List<Long> quoteIds);

	Result<String> updateQuote(QuoteUpdateDto dto);

	Result<QuoteVo> getDetailById(Long id);

	Result<QuoteVo> publicGetDetailById(Long id, List<Integer> statusList);

	Result<Page<QuoteVo>> pageManager(PageDto pageDto);

	Result<String> verify(QuoteDTO dto);

	Result<QuoteVo> getVerifyDetailById(Long id);

	Result<Page<QuoteVo>> verifyPage(PageDto pageDto);

	Result<List<Map<String, Object>>> batch(List<Long> ids);

	String getQuoteType(String quoteId);

	boolean checkQuoteExists(Map<String, String> request);

	Result<Page<QuoteVo>> originalPage(PageDto pageDto);

	Result<Map<String, String>> getSearchSuggestions(String keyword);

	Result<Map<String, Object>> search(String keyword, int page, int size, String type, Long currentUserId);

	void updateStatus(Map<String, String> request);

	Page<QuoteDataVo> queryQuoteData(QueryDataPageDto queryDataPageDto);

	QuoteStatusCountVo queryStatusCount();

	ContentOverviewVo queryContentOverview();

	List<ContentTrendVo> queryContentTrend(int days, List<String> metrics);

	QuoteVo getUpdateDetail(Long id);
}
