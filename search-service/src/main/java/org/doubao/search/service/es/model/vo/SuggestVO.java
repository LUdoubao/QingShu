package org.doubao.search.service.es.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Schema(description = "搜索联想结果")
public class SuggestVO {

    @Schema(description = "热门搜索词")
    private List<HotKeyword> hotKeywords;

    @Schema(description = "相关标签")
    private List<Tag> tags;

    @Schema(description = "相关作者")
    private List<Author> authors;

    public List<HotKeyword> getHotKeywords() {
        return hotKeywords;
    }

    public void setHotKeywords(List<HotKeyword> hotKeywords) {
        this.hotKeywords = hotKeywords;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public List<Author> getAuthors() {
        return authors;
    }

    public void setAuthors(List<Author> authors) {
        this.authors = authors;
    }

    @Schema(description = "热门搜索词")
    public static class HotKeyword {
        @Schema(description = "关键词")
        private String keyword;
        @Schema(description = "热度值")
        private Integer hotValue;

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
    }

    @Schema(description = "相关标签")
    public static class Tag {
        @Schema(description = "标签名称")
        private String name;
        @Schema(description = "使用数量")
        private Long count;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Long getCount() {
            return count;
        }

        public void setCount(Long count) {
            this.count = count;
        }
    }

    @Schema(description = "相关作者")
    public static class Author {
        @Schema(description = "作者ID")
        private Long id;
        @Schema(description = "作者名称")
        private String name;
        @Schema(description = "作者头像")
        private String avatar;
        @Schema(description = "是否认证")
        private Boolean isVerified;
        @Schema(description = "文案数量")
        private Long copyCount;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Boolean getVerified() {
            return isVerified;
        }

        public void setVerified(Boolean verified) {
            isVerified = verified;
        }

        public Long getCopyCount() {
            return copyCount;
        }

        public void setCopyCount(Long copyCount) {
            this.copyCount = copyCount;
        }
    }
}
