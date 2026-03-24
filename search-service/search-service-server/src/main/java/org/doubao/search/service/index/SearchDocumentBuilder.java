package org.doubao.search.service.index;

import org.doubao.search.service.entity.QuoteSearchSyncData;
import org.doubao.search.service.entity.SearchDocIndexDO;
import org.doubao.search.service.support.QueryPreprocessor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class SearchDocumentBuilder {

    private static final int MAX_CONTENT_SEARCH_LENGTH = 48;
    private static final int MAX_TAG_COUNT = 3;

    @Resource
    private QueryPreprocessor queryPreprocessor;

    public SearchDocIndexDO build(QuoteSearchSyncData data) {
        SearchDocIndexDO doc = new SearchDocIndexDO();
        doc.setBizType("quote");
        doc.setBizId(data.getBizId());
        doc.setTitle(data.getTitle());
        doc.setContent(data.getContent());
        doc.setAuthorName(data.getAuthorName());
        doc.setSource(data.getSource());
        doc.setCategoryId(data.getCategoryId());
        doc.setCategoryName(data.getCategoryName());
        doc.setTagNamesText(data.getTagNamesText());
        doc.setStatus(data.getStatus());
        doc.setIsOriginal(data.getIsOriginal());
        doc.setViewCount(0L);
        doc.setLikeCount(0L);
        doc.setCommentCount(0L);
        doc.setFavoriteCount(0L);
        doc.setHotScore(calculateHotScore(doc));
        doc.setQualityScore((double) (doc.getIsOriginal() == null ? 0 : doc.getIsOriginal() * 10));
        String searchText = buildSearchText(data);
        doc.setSearchText(searchText);
        doc.setSearchTextNormalized(queryPreprocessor.normalize(searchText));
        doc.setIsDeleted(data.getStatus() != null && data.getStatus() == 1 ? 0 : 1);
        return doc;
    }

    private String buildSearchText(QuoteSearchSyncData data) {
        StringBuilder builder = new StringBuilder();
        append(builder, data.getTitle());
        append(builder, data.getAuthorName());
        append(builder, data.getSource());
        append(builder, data.getCategoryName());
        append(builder, shorten(data.getContent(), MAX_CONTENT_SEARCH_LENGTH));
        append(builder, limitTags(data.getTagNamesText()));
        return builder.toString().trim();
    }

    private void append(StringBuilder builder, String text) {
        if (text == null || text.trim().isEmpty()) {
            return;
        }
        if (builder.length() > 0) {
            builder.append(' ');
        }
        builder.append(text.trim());
    }

    private String shorten(String text, int maxLength) {
        if (text == null) {
            return null;
        }
        String normalized = text.trim();
        if (normalized.length() <= maxLength) {
            return normalized;
        }
        return normalized.substring(0, maxLength);
    }

    private String limitTags(String tagNamesText) {
        if (tagNamesText == null || tagNamesText.trim().isEmpty()) {
            return null;
        }
        String[] parts = tagNamesText.split(",");
        StringBuilder builder = new StringBuilder();
        int count = 0;
        for (String part : parts) {
            String tag = part == null ? "" : part.trim();
            if (tag.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(',');
            }
            builder.append(tag);
            count++;
            if (count >= MAX_TAG_COUNT) {
                break;
            }
        }
        return builder.toString();
    }

    private Double calculateHotScore(SearchDocIndexDO doc) {
        return doc.getViewCount() * 1.0 + doc.getLikeCount() * 3.0 + doc.getFavoriteCount() * 2.0;
    }
}
