package org.doubao.search.service.gateway;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.doubao.search.service.domain.query.QueryContext;
import org.doubao.search.service.dto.SearchResultDTO;

import java.util.Map;

public interface QuoteSearchGateway {

    Map<String, String> getSuggestions(QueryContext context);

    Page<SearchResultDTO> search(QueryContext context);
}
