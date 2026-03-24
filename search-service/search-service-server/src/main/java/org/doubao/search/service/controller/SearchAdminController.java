package org.doubao.search.service.controller;

import org.doubao.mall.common.entity.Result;
import org.doubao.search.service.job.SearchFullRebuildJob;
import org.doubao.search.service.sync.SearchIndexSyncManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/search/admin")
public class SearchAdminController {

    @Resource
    private SearchFullRebuildJob searchFullRebuildJob;

    @Resource
    private SearchIndexSyncManager searchIndexSyncManager;

    @PostMapping("/rebuild")
    public Result<String> rebuildQuoteIndex() {
        searchFullRebuildJob.rebuildQuoteIndex();
        return Result.success("quote index rebuild completed");
    }

    @PostMapping("/sync/process")
    public Result<String> processSyncTasks() {
        searchIndexSyncManager.processPendingTasks();
        return Result.success("search sync tasks processed");
    }
}
