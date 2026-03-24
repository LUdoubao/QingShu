package org.doubao.search.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("search_query_stats")
public class SearchQueryStatsDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("query_text")
    private String queryText;

    @TableField("query_text_normalized")
    private String queryTextNormalized;

    @TableField("search_count")
    private Long searchCount;

    @TableField("result_count_total")
    private Long resultCountTotal;

    @TableField("click_count")
    private Long clickCount;

    @TableField("last_result_count")
    private Integer lastResultCount;

    public String getQueryText() {
        return queryText;
    }

    public void setQueryText(String queryText) {
        this.queryText = queryText;
    }

    public String getQueryTextNormalized() {
        return queryTextNormalized;
    }

    public void setQueryTextNormalized(String queryTextNormalized) {
        this.queryTextNormalized = queryTextNormalized;
    }

    public Long getSearchCount() {
        return searchCount;
    }

    public void setSearchCount(Long searchCount) {
        this.searchCount = searchCount;
    }

    public Long getResultCountTotal() {
        return resultCountTotal;
    }

    public void setResultCountTotal(Long resultCountTotal) {
        this.resultCountTotal = resultCountTotal;
    }

    public Long getClickCount() {
        return clickCount;
    }

    public void setClickCount(Long clickCount) {
        this.clickCount = clickCount;
    }

    public Integer getLastResultCount() {
        return lastResultCount;
    }

    public void setLastResultCount(Integer lastResultCount) {
        this.lastResultCount = lastResultCount;
    }
}
