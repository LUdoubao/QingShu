package org.doubao.search.service.suggest;

import org.doubao.search.service.dto.SearchSuggestionDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SuggestAssembler {

    public SearchSuggestionDTO assemble(List<String> quotes, List<String> tags, List<String> categories) {
        SearchSuggestionDTO dto = new SearchSuggestionDTO();
        dto.setQuotes(quotes);
        dto.setTags(tags);
        dto.setCategories(categories);
        return dto;
    }
}
