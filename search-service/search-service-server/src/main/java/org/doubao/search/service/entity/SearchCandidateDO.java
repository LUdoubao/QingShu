package org.doubao.search.service.entity;

public class SearchCandidateDO {
    private Long bizId;
    private Double recallScore;

    public Long getBizId() {
        return bizId;
    }

    public void setBizId(Long bizId) {
        this.bizId = bizId;
    }

    public Double getRecallScore() {
        return recallScore;
    }

    public void setRecallScore(Double recallScore) {
        this.recallScore = recallScore;
    }
}
