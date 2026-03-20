package org.doubao.recommend.service.domain;

public class CursorInfo {
    private Double score;
    private Long contentId;

    public CursorInfo() {
    }

    public CursorInfo(Double score, Long contentId) {
        this.score = score;
        this.contentId = contentId;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Long getContentId() {
        return contentId;
    }

    public void setContentId(Long contentId) {
        this.contentId = contentId;
    }
}
