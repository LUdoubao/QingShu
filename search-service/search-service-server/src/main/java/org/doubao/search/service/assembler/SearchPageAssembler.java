package org.doubao.search.service.assembler;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.doubao.search.service.domain.query.QueryContext;
import org.doubao.search.service.domain.result.RecallDoc;
import org.doubao.search.service.dto.SearchResultDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SearchPageAssembler {

    public Page<SearchResultDTO> assemble(QueryContext context, long total, List<RecallDoc> rankedDocs) {
        Page<SearchResultDTO> page = new Page<>(context.getPage(), context.getSize());
        page.setTotal(total);
        List<SearchResultDTO> records = new ArrayList<>();
        for (RecallDoc rankedDoc : rankedDocs) {
            records.add(rankedDoc.getDocument());
        }
        page.setRecords(records);
        return page;
    }
}
