package org.doubao.search.service.service;


import org.doubao.search.service.model.vo.SuggestVO;

public interface SuggestService {
    SuggestVO getSuggestions(String keyword, Long userId);
}
