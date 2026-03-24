package org.doubao.search.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("search_suggest_term")
public class SearchSuggestTermDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("term_text")
    private String termText;

    @TableField("term_normalized")
    private String termNormalized;

    @TableField("prefix_text")
    private String prefixText;

    @TableField("term_type")
    private String termType;

    @TableField("source_id")
    private Long sourceId;

    @TableField("source_biz_type")
    private String sourceBizType;

    @TableField("hot_score")
    private Double hotScore;

    @TableField("quality_score")
    private Double qualityScore;

    @TableField("search_count")
    private Long searchCount;

    @TableField("click_count")
    private Long clickCount;

    @TableField("result_count")
    private Long resultCount;

    @TableField("status")
    private Integer status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTermText() {
        return termText;
    }

    public void setTermText(String termText) {
        this.termText = termText;
    }

    public String getTermNormalized() {
        return termNormalized;
    }

    public void setTermNormalized(String termNormalized) {
        this.termNormalized = termNormalized;
    }

    public String getPrefixText() {
        return prefixText;
    }

    public void setPrefixText(String prefixText) {
        this.prefixText = prefixText;
    }

    public String getTermType() {
        return termType;
    }

    public void setTermType(String termType) {
        this.termType = termType;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public String getSourceBizType() {
        return sourceBizType;
    }

    public void setSourceBizType(String sourceBizType) {
        this.sourceBizType = sourceBizType;
    }

    public Double getHotScore() {
        return hotScore;
    }

    public void setHotScore(Double hotScore) {
        this.hotScore = hotScore;
    }

    public Double getQualityScore() {
        return qualityScore;
    }

    public void setQualityScore(Double qualityScore) {
        this.qualityScore = qualityScore;
    }

    public Long getSearchCount() {
        return searchCount;
    }

    public void setSearchCount(Long searchCount) {
        this.searchCount = searchCount;
    }

    public Long getClickCount() {
        return clickCount;
    }

    public void setClickCount(Long clickCount) {
        this.clickCount = clickCount;
    }

    public Long getResultCount() {
        return resultCount;
    }

    public void setResultCount(Long resultCount) {
        this.resultCount = resultCount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
