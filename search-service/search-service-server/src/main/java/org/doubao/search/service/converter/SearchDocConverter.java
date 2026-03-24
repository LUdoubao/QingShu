package org.doubao.search.service.converter;

import org.doubao.search.service.dto.SearchResultDTO;
import org.doubao.search.service.entity.SearchDocIndexDO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SearchDocConverter {

    public SearchResultDTO toResult(SearchDocIndexDO doc) {
        SearchResultDTO dto = new SearchResultDTO();
        dto.setId(doc.getBizId());
        dto.setContent(doc.getContent());
        dto.setAuthor(doc.getAuthorName());
        dto.setSource(doc.getSource());
        dto.setCategoryId(doc.getCategoryId());
        dto.setCategoryName(doc.getCategoryName());
        dto.setOriginal(doc.getIsOriginal() == null ? 0 : doc.getIsOriginal());
        dto.setTags(parseTags(doc.getTagNamesText()));
        return dto;
    }

    private List<SearchResultDTO.Tag> parseTags(String tagNamesText) {
        List<SearchResultDTO.Tag> tags = new ArrayList<SearchResultDTO.Tag>();
        if (tagNamesText == null || tagNamesText.trim().isEmpty()) {
            return tags;
        }
        for (String tagName : tagNamesText.split(",")) {
            SearchResultDTO.Tag tag = new SearchResultDTO.Tag();
            tag.setName(tagName.trim());
            tags.add(tag);
        }
        return tags;
    }
}
