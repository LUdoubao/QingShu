package org.doubao.search.service.index;

import org.doubao.search.service.entity.SearchDocIndexDO;
import org.doubao.search.service.entity.SearchSuggestTermDO;
import org.doubao.search.service.support.QueryPreprocessor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Component
public class SuggestTermBuilder {

    @Resource
    private QueryPreprocessor queryPreprocessor;

    public List<SearchSuggestTermDO> build(SearchDocIndexDO doc) {
        List<SearchSuggestTermDO> terms = new ArrayList<SearchSuggestTermDO>();
        add(terms, doc.getContent(), "QUOTE", doc.getBizId(), "quote", doc.getHotScore(), doc.getQualityScore());
        add(terms, doc.getCategoryName(), "CATEGORY", doc.getCategoryId(), "quote", doc.getHotScore(), doc.getQualityScore());
        if (doc.getTagNamesText() != null) {
            for (String tag : doc.getTagNamesText().split(",")) {
                add(terms, tag, "TAG", doc.getBizId(), "quote", doc.getHotScore(), doc.getQualityScore());
            }
        }
        return terms;
    }

    private void add(List<SearchSuggestTermDO> target, String termText, String termType, Long sourceId,
                     String sourceBizType, Double hotScore, Double qualityScore) {
        String normalized = queryPreprocessor.normalize(termText);
        if (normalized.isEmpty()) {
            return;
        }
        SearchSuggestTermDO item = new SearchSuggestTermDO();
        item.setTermText(termText);
        item.setTermNormalized(normalized);
        item.setPrefixText(normalized.substring(0, Math.min(normalized.length(), 10)));
        item.setTermType(termType);
        item.setSourceId(sourceId);
        item.setSourceBizType(sourceBizType);
        item.setHotScore(hotScore == null ? 0D : hotScore);
        item.setQualityScore(qualityScore == null ? 0D : qualityScore);
        item.setSearchCount(0L);
        item.setClickCount(0L);
        item.setResultCount(0L);
        item.setStatus(1);
        target.add(item);
    }
}
