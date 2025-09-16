package org.doubao.search.service.es.service;


import org.doubao.search.service.es.model.vo.SuggestVO;

public interface SuggestService {
    SuggestVO getSuggestions(String keyword, Long userId);
}
