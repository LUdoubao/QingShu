package org.doubao.search.service.db.dto;

import lombok.Data;

import java.util.List;

public class SearchSuggestionDTO {
    private List<String> quotes;
    private List<String> tags;
    private List<String> categories;

    public List<String> getQuotes() {
        return quotes;
    }

    public void setQuotes(List<String> quotes) {
        this.quotes = quotes;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }
}
