package org.doubao.search.service.db.controller;

import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.util.UserContext;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.doubao.search.service.db.dto.SearchSuggestionDTO;
import org.doubao.search.service.db.dto.SearchResultDTO;
import org.doubao.search.service.db.entity.SearchHistory;
import org.doubao.search.service.db.service.SearchService;
import org.doubao.search.service.db.service.SearchHistoryService;
import java.util.List;

@RestController
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private SearchService searchService;
    
    @Autowired
    private SearchHistoryService searchHistoryService;
    
    /**
     * 获取搜索建议
     */
    @GetMapping("/suggestions")
    public Result<SearchSuggestionDTO> getSuggestions(@RequestParam String keyword) {
        try {
            SearchSuggestionDTO suggestions = searchService.getSearchSuggestions(keyword);
            return Result.success(suggestions);
        } catch (Exception e) {
            return Result.error("获取搜索建议失败: " + e.getMessage());
        }
    }
    
    /**
     * 执行搜索
     */
    @GetMapping("/results")
    public Result<Page<SearchResultDTO>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam String type
           ) {
        try {
            Long userId = UserContext.getUserId();
            // 保存搜索历史
            searchHistoryService.saveSearchHistory(userId, keyword);

            Page<SearchResultDTO> results = searchService.search(keyword, page, size, type);
            return Result.success(results);
        } catch (Exception e) {
            return Result.error("搜索失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户搜索历史
     */
    @GetMapping("/history")
    public Result<List<SearchHistory>> getSearchHistory() {
        Long userId = UserContext.getUserId();

        List<SearchHistory> historyList = searchHistoryService.getUserSearchHistory(userId);
        return Result.success(historyList);

    }
    
    /**
     * 清空用户搜索历史
     */
    @PostMapping("/clear")
    public Result<Void> clearSearchHistory() {

        Long userId = UserContext.getUserId();
        boolean success = searchHistoryService.clearUserSearchHistory(userId);
        if (success) {
            return Result.success(null);
        } else {
            return Result.error("清空搜索历史失败");
        }

    }
}
