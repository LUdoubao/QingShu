package org.doubao.search.service.domain.result;

import org.doubao.search.service.dto.SearchResultDTO;

public class RecallDoc {

    private final Long bizId;
    private SearchResultDTO document;
    private final String route;
    private double score;

    public RecallDoc(Long bizId, SearchResultDTO document, String route, double score) {
        this.bizId = bizId;
        this.document = document;
        this.route = route;
        this.score = score;
    }

    public Long getBizId() {
        return bizId;
    }

    public SearchResultDTO getDocument() {
        return document;
    }

    public void setDocument(SearchResultDTO document) {
        this.document = document;
    }

    public String getRoute() {
        return route;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
