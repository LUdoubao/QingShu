package org.doubao.search.service.suggest;

import org.doubao.search.service.domain.query.QueryContext;

import java.util.List;
import java.util.Map;

public interface SuggestSource {

    List<String> load(QueryContext context, Map<String, String> rawSuggestionData);
}
