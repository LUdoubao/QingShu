package org.doubao.search.service.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "热门搜索VO")
public class HotSearchVO {
    @Schema(description = "关键词")
    private String keyword;
    
    @Schema(description = "搜索次数")
    private Integer searchCount;
    
    @Schema(description = "热度值")
    private Integer hotValue;
    
    @Schema(description = "排名")
    private Integer rank;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Integer getHotValue() {
        return hotValue;
    }

    public void setHotValue(Integer hotValue) {
        this.hotValue = hotValue;
    }

    public Integer getSearchCount() {
        return searchCount;
    }

    public void setSearchCount(Integer searchCount) {
        this.searchCount = searchCount;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }
}
