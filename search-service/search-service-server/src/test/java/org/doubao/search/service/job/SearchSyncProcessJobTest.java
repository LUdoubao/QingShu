package org.doubao.search.service.job;

import org.doubao.search.service.sync.SearchIndexSyncManager;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = SearchSyncProcessJob.class)
public class SearchSyncProcessJobTest {

    @Autowired
    private SearchSyncProcessJob searchSyncProcessJob;

    @MockBean
    private SearchIndexSyncManager searchIndexSyncManager;

    @Test
    public void shouldCallProcessPendingTasks() {
        searchSyncProcessJob.processPendingTasks();
        Mockito.verify(searchIndexSyncManager).processPendingTasks();
    }
}
