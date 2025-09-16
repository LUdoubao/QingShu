package org.doubao.search.service.es.service.remote;

import org.doubao.search.service.es.model.dto.CopywritingDTO;
import org.doubao.search.service.es.service.remote.fallback.ContentRemoteFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@FeignClient(name = "content-service", fallback = ContentRemoteFallback.class)
public interface ContentRemoteService {
    @GetMapping("/api/v1/copywriting/{id}")
    CopywritingDTO getCopywritingById(@PathVariable("id") Long id);
    
    @GetMapping("/api/v1/copywriting/page")
    List<CopywritingDTO> getCopywritingsByPage(
            @RequestParam("page") int page, 
            @RequestParam("size") int size);
}
