package org.doubao.search.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("search_term_index")
public class SearchTermIndexDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("biz_type")
    private String bizType;
    @TableField("biz_id")
    private Long bizId;
    @TableField("term")
    private String term;
    @TableField("term_normalized")
    private String termNormalized;
    @TableField("term_type")
    private String termType;
    @TableField("source_field")
    private String sourceField;
    @TableField("weight")
    private Integer weight;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBizType() { return bizType; }
    public void setBizType(String bizType) { this.bizType = bizType; }
    public Long getBizId() { return bizId; }
    public void setBizId(Long bizId) { this.bizId = bizId; }
    public String getTerm() { return term; }
    public void setTerm(String term) { this.term = term; }
    public String getTermNormalized() { return termNormalized; }
    public void setTermNormalized(String termNormalized) { this.termNormalized = termNormalized; }
    public String getTermType() { return termType; }
    public void setTermType(String termType) { this.termType = termType; }
    public String getSourceField() { return sourceField; }
    public void setSourceField(String sourceField) { this.sourceField = sourceField; }
    public Integer getWeight() { return weight; }
    public void setWeight(Integer weight) { this.weight = weight; }
}
