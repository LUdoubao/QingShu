package org.doubao.search.service.search;

import org.doubao.search.service.entity.SearchDocIndexDO;
import org.doubao.search.service.entity.SearchTermIndexDO;
import org.doubao.search.service.index.SearchTermBuilder;
import org.doubao.search.service.support.QueryPreprocessor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.stream.Collectors;

public class SearchTermBuilderTest {

    @Test
    public void shouldAvoidSingleCharacterAndVeryLongContentTerms() {
        SearchTermBuilder builder = new SearchTermBuilder();
        ReflectionTestUtils.setField(builder, "queryPreprocessor", new QueryPreprocessor());

        SearchDocIndexDO doc = new SearchDocIndexDO();
        doc.setBizType("quote");
        doc.setBizId(1L);
        doc.setTitle("人生若只如初见");
        doc.setContent("人生若只如初见何事秋风悲画扇");
        doc.setAuthorName("纳兰性德");
        doc.setTagNamesText("诗词,古文");

        List<SearchTermIndexDO> terms = builder.build(doc);
        List<String> contentTerms = terms.stream()
                .filter(item -> "content".equals(item.getSourceField()))
                .map(SearchTermIndexDO::getTermNormalized)
                .collect(Collectors.toList());

        Assertions.assertFalse(contentTerms.contains("人"));
        Assertions.assertFalse(contentTerms.contains("生"));
        Assertions.assertTrue(contentTerms.contains("人生"));
        Assertions.assertFalse(contentTerms.contains("人生若只如初见何事秋风悲画扇"));
    }
}
