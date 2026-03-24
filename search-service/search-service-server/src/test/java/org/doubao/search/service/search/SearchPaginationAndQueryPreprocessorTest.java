package org.doubao.search.service.search;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.doubao.search.service.assembler.SearchPageAssembler;
import org.doubao.search.service.domain.query.QueryContext;
import org.doubao.search.service.domain.result.RecallDoc;
import org.doubao.search.service.dto.SearchResultDTO;
import org.doubao.search.service.support.QueryPreprocessor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchPaginationAndQueryPreprocessorTest {

    @Test
    public void shouldReturnOnlyCurrentPageRecords() {
        SearchPageAssembler assembler = new SearchPageAssembler();
        QueryContext context = new QueryContext("test", "test", "quote", 2, 2, 1L, Arrays.asList("test"));

        List<RecallDoc> rankedDocs = new ArrayList<RecallDoc>();
        rankedDocs.add(buildRecallDoc(1L, "doc-1"));
        rankedDocs.add(buildRecallDoc(2L, "doc-2"));
        rankedDocs.add(buildRecallDoc(3L, "doc-3"));
        rankedDocs.add(buildRecallDoc(4L, "doc-4"));

        Page<SearchResultDTO> page = assembler.assemble(context, rankedDocs.size(), rankedDocs);

        Assertions.assertEquals(4L, page.getTotal());
        Assertions.assertEquals(2, page.getRecords().size());
        Assertions.assertEquals(Long.valueOf(3L), page.getRecords().get(0).getId());
        Assertions.assertEquals(Long.valueOf(4L), page.getRecords().get(1).getId());
    }

    @Test
    public void shouldNotSplitChineseKeywordIntoSingleCharacterTerms() {
        QueryPreprocessor queryPreprocessor = new QueryPreprocessor();

        QueryContext context = queryPreprocessor.process(
                new org.doubao.search.service.domain.query.SearchQuery("苏轼", 1, 10, "quote", 1L));

        Assertions.assertEquals(Arrays.asList("苏轼"), context.getTerms());
    }

    private RecallDoc buildRecallDoc(Long id, String content) {
        SearchResultDTO dto = new SearchResultDTO();
        dto.setId(id);
        dto.setContent(content);
        return new RecallDoc(id, dto, "TEST", 1D);
    }
}
