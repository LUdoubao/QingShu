package org.doubao.search.service.search;

import org.doubao.search.service.entity.SearchDocIndexDO;
import org.doubao.search.service.entity.SearchSuggestTermDO;
import org.doubao.search.service.entity.SearchTermIndexDO;
import org.doubao.search.service.entity.QuoteSearchSyncData;
import org.doubao.search.service.index.SearchDocumentBuilder;
import org.doubao.search.service.index.SearchTermBuilder;
import org.doubao.search.service.index.SuggestTermBuilder;
import org.doubao.search.service.support.QueryPreprocessor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.stream.Collectors;

public class SearchTermBuilderTest {

    @Test
    public void shouldLimitTermsToFifteenAndRespectFieldMinLength() {
        SearchTermBuilder builder = new SearchTermBuilder();
        ReflectionTestUtils.setField(builder, "queryPreprocessor", new QueryPreprocessor());

        SearchDocIndexDO doc = new SearchDocIndexDO();
        doc.setBizType("quote");
        doc.setBizId(1L);
        doc.setTitle("人生若只如初见");
        doc.setContent("人生若只如初见何事秋风悲画扇");
        doc.setAuthorName("纳兰性德");

        List<SearchTermIndexDO> terms = builder.build(doc);
        List<String> contentTerms = terms.stream()
                .filter(item -> "content".equals(item.getSourceField()))
                .map(SearchTermIndexDO::getTermNormalized)
                .collect(Collectors.toList());
        List<String> titleTerms = terms.stream()
                .filter(item -> "title".equals(item.getSourceField()))
                .map(SearchTermIndexDO::getTermNormalized)
                .collect(Collectors.toList());
        List<String> authorTerms = terms.stream()
                .filter(item -> "author".equals(item.getSourceField()))
                .map(SearchTermIndexDO::getTermNormalized)
                .collect(Collectors.toList());

        Assertions.assertTrue(terms.size() <= 15);
        Assertions.assertTrue(authorTerms.stream().allMatch(item -> item.length() >= 2));
        Assertions.assertTrue(titleTerms.stream().allMatch(item -> item.length() >= 2));
        Assertions.assertTrue(contentTerms.stream().allMatch(item -> item.length() >= 3));
        Assertions.assertFalse(contentTerms.contains("人"));
        Assertions.assertFalse(contentTerms.contains("生"));
        Assertions.assertTrue(contentTerms.contains("人生若"));
        Assertions.assertFalse(terms.stream().anyMatch(item -> "tag".equals(item.getSourceField())));
    }

    @Test
    public void shouldReduceSuggestionAndSearchTextPayload() {
        SuggestTermBuilder suggestTermBuilder = new SuggestTermBuilder();
        ReflectionTestUtils.setField(suggestTermBuilder, "queryPreprocessor", new QueryPreprocessor());

        SearchDocumentBuilder searchDocumentBuilder = new SearchDocumentBuilder();
        ReflectionTestUtils.setField(searchDocumentBuilder, "queryPreprocessor", new QueryPreprocessor());

        QuoteSearchSyncData data = new QuoteSearchSyncData();
        data.setBizId(1L);
        data.setTitle("人生若只如初见");
        data.setContent("人生若只如初见何事秋风悲画扇等闲变却故人心却道故人心易变");
        data.setAuthorName("纳兰性德");
        data.setSource("木兰词");
        data.setCategoryName("诗词");
        data.setTagNamesText("古诗,伤感,宋词,名句");
        data.setStatus(1);
        data.setIsOriginal(1);

        SearchDocIndexDO doc = searchDocumentBuilder.build(data);
        List<SearchSuggestTermDO> suggestTerms = suggestTermBuilder.build(doc);
        String originalSearchText = data.getTitle() + " " + data.getContent() + " " + data.getAuthorName()
                + " " + data.getSource() + " " + data.getCategoryName() + " " + data.getTagNamesText();

        Assertions.assertTrue(doc.getSearchText().length() < originalSearchText.length());
        Assertions.assertTrue(suggestTerms.size() <= 4);
        Assertions.assertTrue(suggestTerms.stream().allMatch(item -> item.getTermNormalized().length() <= 12));
        Assertions.assertFalse(suggestTerms.stream().anyMatch(item -> "QUOTE".equals(item.getTermType())
                && item.getTermText().contains("何事秋风悲画扇等闲变却故人心")));
    }
}
