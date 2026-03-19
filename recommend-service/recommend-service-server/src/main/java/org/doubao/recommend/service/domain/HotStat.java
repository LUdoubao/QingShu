package org.doubao.recommend.service.domain;


/**
 * 热度统计对象
 * 用于存储和传递内容的热度分数统计信息
 */
public class HotStat {
    
    /**
     * 内容 ID
     * 唯一标识一个内容的标识符
     */
    private Long contentId;
    
    /**
     * 热度分数
     * 基于内容的浏览量、互动量等计算的实时热度值
     */
    private Double score;

    /**
     * 获取内容 ID
     * @return 内容 ID
     */
    public Long getContentId() {
        return contentId;
    }

    /**
     * 设置内容 ID
     * @param contentId 内容 ID
     */
    public void setContentId(Long contentId) {
        this.contentId = contentId;
    }

    /**
     * 获取热度分数
     * @return 热度分数
     */
    public Double getScore() {
        return score;
    }

    /**
     * 设置热度分数
     * @param score 热度分数
     */
    public void setScore(Double score) {
        this.score = score;
    }
}
