package org.doubao.search.service.service;

import org.doubao.search.service.dto.SearchSuggestionDTO;
import org.doubao.search.service.dto.SearchResultDTO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface SearchService {
    /**
     * 获取搜索建议
     */
    SearchSuggestionDTO getSearchSuggestions(String keyword);
    
    /**
     * 执行搜索
     */
    Page<SearchResultDTO> search(String keyword, int page, int size, String type, Long userId);
}