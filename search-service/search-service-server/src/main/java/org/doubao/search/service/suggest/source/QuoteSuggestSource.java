package org.doubao.search.service.suggest.source;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class QuoteSuggestSource extends DbSuggestSource {

    @Override
    protected String getTermType() {
        return "QUOTE";
    }

    @Override
    protected List<String> fallback(Map<String, String> rawSuggestionData) {
        String values = rawSuggestionData.get("quotes");
        if (values == null || values.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(values.split(","));
    }
}
