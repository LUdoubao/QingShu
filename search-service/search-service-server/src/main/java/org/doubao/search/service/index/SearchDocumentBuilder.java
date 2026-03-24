package org.doubao.search.service.index;

import org.doubao.search.service.entity.QuoteSearchSyncData;
import org.doubao.search.service.entity.SearchDocIndexDO;
import org.doubao.search.service.support.QueryPreprocessor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class SearchDocumentBuilder {

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
        append(builder, data.getContent());
        append(builder, data.getAuthorName());
        append(builder, data.getSource());
        append(builder, data.getCategoryName());
        append(builder, data.getTagNamesText());
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

    private Double calculateHotScore(SearchDocIndexDO doc) {
        return doc.getViewCount() * 1.0 + doc.getLikeCount() * 3.0 + doc.getFavoriteCount() * 2.0;
    }
}
