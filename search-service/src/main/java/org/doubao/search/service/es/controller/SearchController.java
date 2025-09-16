package org.doubao.search.service.es.controller;

import com.github.houbb.heaven.util.lang.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.doubao.mall.common.entity.Result;
import org.doubao.search.service.es.model.dto.SearchRequestDto;
import org.doubao.search.service.es.model.vo.SearchHistoryVO;
import org.doubao.search.service.es.model.vo.SearchResultVO;
import org.doubao.search.service.es.model.vo.SuggestVO;
import org.doubao.search.service.es.service.SearchHistoryService;
import org.doubao.search.service.es.service.SearchService;
import org.doubao.search.service.es.service.SuggestService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/search")

@Tag(name = "搜索接口", description = "提供全文检索、筛选、联想等功能")
public class SearchController {

    @Resource
    private SearchService searchService;
    @Resource
    private SuggestService suggestService;
    @Resource
    private SearchHistoryService searchHistoryService;

    @PostMapping
    @Operation(summary = "执行搜索", description = "根据关键词和筛选条件执行搜索")
    public Result<SearchResultVO> search(
            @Valid @RequestBody SearchRequestDto request,
            @Parameter(description = "用户ID，可选") @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        SearchResultVO result = searchService.search(request, userId);
        // 保存搜索历史
        if (userId != null) {
            searchHistoryService.saveSearchHistory(
                userId, 
                request.getKeyword(), 
                BeanUtil.beanToMap(request.getFilters()),
                result.getTotal().intValue()
            );
        }
        return Result.success(result);
    }

    @GetMapping("/suggest")
    @Operation(summary = "获取搜索联想", description = "根据输入的关键词前缀获取搜索联想")
    public Result<SuggestVO> getSuggestions(
            @Parameter(description = "搜索关键词前缀", required = true) @RequestParam String keyword,
            @Parameter(description = "用户ID，可选") @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(suggestService.getSuggestions(keyword, userId));
    }

    @GetMapping("/history")
    @Operation(summary = "获取用户搜索历史", description = "获取当前用户的搜索历史记录")
    public Result<List<SearchHistoryVO>> getUserSearchHistory(
            @Parameter(description = "用户ID", required = true) @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "获取数量限制") @RequestParam(required = false) Integer limit) {
        return Result.success(searchHistoryService.getUserSearchHistory(userId, limit));
    }

    @DeleteMapping("/history/{historyId}")
    @Operation(summary = "删除单条搜索历史", description = "删除用户的单条搜索历史记录")
    public Result<Boolean> deleteSearchHistory(
            @Parameter(description = "用户ID", required = true) @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "历史记录ID", required = true) @PathVariable String historyId) {
        return Result.success(searchHistoryService.deleteSearchHistory(userId, historyId));
    }

    @DeleteMapping("/history")
    @Operation(summary = "清空搜索历史", description = "清空用户的所有搜索历史记录")
    public Result<Boolean> clearSearchHistory(
            @Parameter(description = "用户ID", required = true) @RequestHeader("X-User-Id") Long userId) {
        return Result.success(searchHistoryService.clearSearchHistory(userId));
    }

    @GetMapping("/hot")
    @Operation(summary = "获取热门搜索", description = "获取指定时间段内的热门搜索词")
    public Result<List<?>> getHotSearches(
          @RequestParam String period,
          @RequestParam Integer limit) {
        return Result.success(searchService.getHotSearches(period, limit));
    }
}
