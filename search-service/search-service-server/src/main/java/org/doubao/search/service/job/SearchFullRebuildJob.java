package org.doubao.search.service.job;

import org.doubao.search.service.index.SearchIndexService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class SearchFullRebuildJob {

    @Resource
    private SearchIndexService searchIndexService;

    public void rebuildQuoteIndex() {
        searchIndexService.rebuildAll("quote");
    }
}
