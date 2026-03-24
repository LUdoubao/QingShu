package org.doubao.search.service.provider;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.doubao.search.service.domain.query.QueryContext;
import org.doubao.search.service.dto.SearchResultDTO;
import org.doubao.search.service.dto.SearchSuggestionDTO;

public interface SearchProvider {

    SearchSuggestionDTO suggest(QueryContext context);

    Page<SearchResultDTO> search(QueryContext context);
}
