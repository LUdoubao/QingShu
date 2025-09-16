package org.doubao.search.service.es.service.remote.fallback;

import org.doubao.search.service.es.model.dto.CopywritingDTO;
import org.doubao.search.service.es.service.remote.ContentRemoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Collections;
import java.util.List;

@Component
public class ContentRemoteFallback implements ContentRemoteService {
    private static final Logger log = LoggerFactory.getLogger(ContentRemoteFallback.class);
    @Override
    public CopywritingDTO getCopywritingById(@PathVariable("id") Long id) {
        log.warn("获取文案详情失败，使用降级策略，id: {}", id);
        return null;
    }

    @Override
    public List<CopywritingDTO> getCopywritingsByPage(int page, int size) {
        log.warn("分页获取文案列表失败，使用降级策略，page: {}, size: {}", page, size);
        return Collections.emptyList();
    }
}
