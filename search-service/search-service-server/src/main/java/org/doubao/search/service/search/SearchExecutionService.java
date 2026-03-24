package org.doubao.search.service.search;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.doubao.search.service.domain.query.QueryContext;
import org.doubao.search.service.dto.SearchResultDTO;

public interface SearchExecutionService {

    Page<SearchResultDTO> search(QueryContext context);
}
