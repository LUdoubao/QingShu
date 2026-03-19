package org.doubao.recommend.service.domain;


/**
 * 推荐候选物品对象
 * 用于表示推荐系统中的候选内容，包含内容 ID、召回来源和基础分数
 */
public class CandidateItem {
    /**
     * 内容 ID
     * 唯一标识一个候选内容的标识符
     */
    private Long contentId;
    
    /**
     * 召回来源
     * 标识该候选内容来自哪个召回通道或策略
     * 例如：协同过滤召回、热门召回、基于内容召回等
     */
    private String recallSource;
    
    /**
     * 基础分数，默认值为 0.0
     * 用于评估候选内容的初始质量或相关性得分
     * 在后续的排序阶段可能会被调整
     */
    private Double baseScore = 0.0;

    public Long getContentId() {
        return contentId;
    }

    public void setContentId(Long contentId) {
        this.contentId = contentId;
    }

    public String getRecallSource() {
        return recallSource;
    }

    public void setRecallSource(String recallSource) {
        this.recallSource = recallSource;
    }

    public Double getBaseScore() {
        return baseScore;
    }

    public void setBaseScore(Double baseScore) {
        this.baseScore = baseScore;
    }
}
