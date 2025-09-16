package org.doubao.search.service.es.service.impl;

import cn.hutool.core.bean.BeanUtil;
import org.doubao.search.service.es.model.dto.CopywritingDTO;
import org.doubao.search.service.es.model.entity.es.CopywritingEsEntity;
import org.doubao.search.service.es.model.message.IndexUpdateMessage;
import org.doubao.search.service.es.repository.elasticsearch.CopywritingEsRepository;
import org.doubao.search.service.es.service.IndexSyncService;
import org.doubao.search.service.es.service.remote.ContentRemoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class IndexSyncServiceImpl implements IndexSyncService {

    private static final Logger log = LoggerFactory.getLogger(IndexSyncServiceImpl.class);
    @Resource
    private CopywritingEsRepository copywritingEsRepository;
    @Resource
    private ContentRemoteService contentRemoteService;

    @Override
    @RabbitListener(queues = "copywriting-index-update")
    public void handleIndexUpdate(IndexUpdateMessage message) {
        log.info("Received index update message: {}", message);
        
        if (message == null || message.getCopywritingId() == null) {
            log.warn("Invalid index update message: {}", message);
            return;
        }
        
        try {
            switch (message.getOperation()) {
                case CREATE:
                case UPDATE:
                    syncCopywritingToEs(message.getCopywritingId());
                    break;
                case DELETE:
                    deleteCopywritingFromEs(message.getCopywritingId());
                    break;
                default:
                    log.warn("Unknown operation: {}", message.getOperation());
            }
        } catch (Exception e) {
            log.error("Failed to process index update message: {}", message, e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncCopywritingToEs(Long copywritingId) {
        if (copywritingId == null) {
            return;
        }
        
        // 从内容服务获取文案详情
        CopywritingDTO copywriting = contentRemoteService.getCopywritingById(copywritingId);
        if (copywriting == null) {
            log.warn("Copywriting not found, id: {}", copywritingId);
            // 尝试删除ES中可能存在的记录
            deleteCopywritingFromEs(copywritingId);
            return;
        }
        
        // 转换为ES实体
        CopywritingEsEntity esEntity = new CopywritingEsEntity();
        BeanUtil.copyProperties(copywriting, esEntity);
        
        // 保存到ES
        copywritingEsRepository.save(esEntity);
        log.info("Successfully synced copywriting to ES, id: {}", copywritingId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCopywritingFromEs(Long copywritingId) {
        if (copywritingId == null) {
            return;
        }
        
        // 从ES删除
        copywritingEsRepository.deleteById(copywritingId);
        log.info("Successfully deleted copywriting from ES, id: {}", copywritingId);
    }

    @Override
    public void batchSyncCopywritings(int batchSize, int startPage) {
        // 批量同步方法，用于全量索引重建
        log.info("Starting batch sync copywritings, batchSize: {}, startPage: {}", batchSize, startPage);
        
        int page = startPage;
        while (true) {
            try {
                // 分页获取文案列表
                List<CopywritingDTO> copywritings = contentRemoteService.getCopywritingsByPage(page, batchSize);
                if (copywritings.isEmpty()) {
                    log.info("Batch sync completed, total pages processed: {}", page - startPage + 1);
                    break;
                }
                
                // 转换并保存到ES
                List<CopywritingEsEntity> esEntities = copywritings.stream()
                        .map(copywriting -> {
                            CopywritingEsEntity esEntity = new CopywritingEsEntity();
                            BeanUtil.copyProperties(copywriting, esEntity);
                            return esEntity;
                        })
                        .collect(Collectors.toList());
                
                copywritingEsRepository.saveAll(esEntities);
                log.info("Synced page {} with {} copywritings", page, esEntities.size());
                
                page++;
            } catch (Exception e) {
                log.error("Error in batch sync, page: {}", page, e);
                // 出错重试一次
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
}
