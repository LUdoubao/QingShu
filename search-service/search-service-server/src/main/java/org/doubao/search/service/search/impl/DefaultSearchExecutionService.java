package org.doubao.search.service.search.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.doubao.search.service.assembler.SearchPageAssembler;
import org.doubao.search.service.domain.query.QueryContext;
import org.doubao.search.service.domain.result.RecallDoc;
import org.doubao.search.service.domain.result.RecallResult;
import org.doubao.search.service.dto.SearchResultDTO;
import org.doubao.search.service.recall.RecallService;
import org.doubao.search.service.rank.SearchRankService;
import org.doubao.search.service.search.SearchExecutionService;
import org.doubao.search.service.stats.SearchStatsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class DefaultSearchExecutionService implements SearchExecutionService {

    @Resource
    private RecallService recallService;

    @Resource
    private SearchRankService searchRankService;

    @Resource
    private SearchPageAssembler searchPageAssembler;

    @Resource
    private SearchStatsService searchStatsService;

    @Override
    public Page<SearchResultDTO> search(QueryContext context) {
        RecallResult recallResult = recallService.recall(context);
        List<RecallDoc> rankedDocs = searchRankService.rank(context, recallResult.getDocs());
        Page<SearchResultDTO> result = searchPageAssembler.assemble(context, recallResult.getTotal(), rankedDocs);
        searchStatsService.recordSearch(context, result.getTotal());
        return result;
    }
}
