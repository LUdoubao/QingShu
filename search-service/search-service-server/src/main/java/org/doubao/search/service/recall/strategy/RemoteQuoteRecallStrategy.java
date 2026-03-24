package org.doubao.search.service.recall.strategy;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.doubao.search.service.domain.query.QueryContext;
import org.doubao.search.service.domain.result.RecallDoc;
import org.doubao.search.service.domain.result.RecallResult;
import org.doubao.search.service.dto.SearchResultDTO;
import org.doubao.search.service.gateway.QuoteSearchGateway;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Component
public class RemoteQuoteRecallStrategy implements RecallStrategy {

    @Resource
    private QuoteSearchGateway quoteSearchGateway;

    @Override
    public RecallResult recall(QueryContext context) {
        Page<SearchResultDTO> page = quoteSearchGateway.search(context);
        List<RecallDoc> docs = new ArrayList<>();
        for (SearchResultDTO record : page.getRecords()) {
            docs.add(new RecallDoc(record.getId(), record, "REMOTE", 0D));
        }
        return new RecallResult(page.getTotal(), docs);
    }
}
