package org.doubao.search.service.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.time.LocalDateTime;

@Schema(description = "搜索请求DTO")
public class SearchRequestDto {

    @Schema(description = "搜索关键词", required = true)
    private String keyword;

    @Schema(description = "筛选选条件")
    private SearchFilter filters;

    @Schema(description = "排序字段(relevance, time, likeCount, collectCount, commentCount, viewCount)")
    private String sortBy = "relevance";

    @Schema(description = "排序方向(asc, desc)")
    private String sortOrder = "desc";

    @Schema(description = "页码")
    private Integer page = 1;

    @Schema(description = "每页大小")
    private Integer pageSize = 20;

    public SearchRequestDto() {
    }


    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public SearchFilter getFilters() {
        return filters;
    }

    public void setFilters(SearchFilter filters) {
        this.filters = filters;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public void source(SearchSourceBuilder sourceBuilder) {

    }

    @Schema(description = "搜索筛选条件")
    public static class SearchFilter {

        @Schema(description = "标签列表")
        private String[] tags;

        @Schema(description = "作者ID列表")
        private Long[] authorIds;

        @Schema(description = "是否只看关注的作者")
        private Boolean isFollowing;

        @Schema(description = "是否只看认证作者")
        private Boolean isVerified;

        @Schema(description = "开始时间")
        private LocalDateTime startTime;

        @Schema(description = "结束时间")
        private LocalDateTime endTime;

        @Schema(description = "内容类型")
        private String[] contentTypes;

        @Schema(description = "点赞数范围[min, max]")
        private Integer[] likeCountRange;

        @Schema(description = "收藏数范围[min, max]")
        private Integer[] collectCountRange;

        @Schema(description = "评论数范围[min, max]")
        private Integer[] commentCountRange;

        @Schema(description = "浏览量范围[min, max]")
        private Integer[] viewCountRange;

        @Schema(description = "审核状态(仅管理员)")
        private String auditStatus;

        public String[] getTags() {
            return tags;
        }

        public void setTags(String[] tags) {
            this.tags = tags;
        }

        public Long[] getAuthorIds() {
            return authorIds;
        }

        public void setAuthorIds(Long[] authorIds) {
            this.authorIds = authorIds;
        }

        public Boolean getFollowing() {
            return isFollowing;
        }

        public void setFollowing(Boolean following) {
            isFollowing = following;
        }

        public Boolean getVerified() {
            return isVerified;
        }

        public void setVerified(Boolean verified) {
            isVerified = verified;
        }

        public LocalDateTime getStartTime() {
            return startTime;
        }

        public void setStartTime(LocalDateTime startTime) {
            this.startTime = startTime;
        }

        public LocalDateTime getEndTime() {
            return endTime;
        }

        public void setEndTime(LocalDateTime endTime) {
            this.endTime = endTime;
        }

        public String[] getContentTypes() {
            return contentTypes;
        }

        public void setContentTypes(String[] contentTypes) {
            this.contentTypes = contentTypes;
        }

        public Integer[] getLikeCountRange() {
            return likeCountRange;
        }

        public void setLikeCountRange(Integer[] likeCountRange) {
            this.likeCountRange = likeCountRange;
        }

        public Integer[] getCollectCountRange() {
            return collectCountRange;
        }

        public void setCollectCountRange(Integer[] collectCountRange) {
            this.collectCountRange = collectCountRange;
        }

        public Integer[] getCommentCountRange() {
            return commentCountRange;
        }

        public void setCommentCountRange(Integer[] commentCountRange) {
            this.commentCountRange = commentCountRange;
        }

        public Integer[] getViewCountRange() {
            return viewCountRange;
        }

        public void setViewCountRange(Integer[] viewCountRange) {
            this.viewCountRange = viewCountRange;
        }

        public String getAuditStatus() {
            return auditStatus;
        }

        public void setAuditStatus(String auditStatus) {
            this.auditStatus = auditStatus;
        }
    }
}
