package org.doubao.search.service.es.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "搜索历史VO")
public class SearchHistoryVO {
    @Schema(description = "记录ID")
    private String id;
    
    @Schema(description = "搜索关键词")
    private String keyword;
    
    @Schema(description = "搜索时间")
    private LocalDateTime searchTime;
    
    @Schema(description = "结果数量")
    private Integer resultCount;
    
    @Schema(description = "筛选条件")
    private Map<String, Object> filters;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public LocalDateTime getSearchTime() {
        return searchTime;
    }

    public void setSearchTime(LocalDateTime searchTime) {
        this.searchTime = searchTime;
    }

    public Integer getResultCount() {
        return resultCount;
    }

    public void setResultCount(Integer resultCount) {
        this.resultCount = resultCount;
    }

    public Map<String, Object> getFilters() {
        return filters;
    }

    public void setFilters(Map<String, Object> filters) {
        this.filters = filters;
    }
}
