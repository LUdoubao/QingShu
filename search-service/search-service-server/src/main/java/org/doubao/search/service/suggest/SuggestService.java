package org.doubao.search.service.suggest;

import org.doubao.search.service.domain.query.QueryContext;
import org.doubao.search.service.dto.SearchSuggestionDTO;

public interface SuggestService {

    SearchSuggestionDTO suggest(QueryContext context);
}
