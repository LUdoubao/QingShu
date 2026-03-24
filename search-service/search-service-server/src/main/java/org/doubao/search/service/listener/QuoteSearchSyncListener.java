package org.doubao.search.service.listener;

import com.rabbitmq.client.Channel;
import org.doubao.mall.common.entity.BusinessEvent;
import org.doubao.mall.common.enums.EventType;
import org.doubao.search.service.sync.SearchIndexSyncManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;

@Component
public class QuoteSearchSyncListener {

    private static final Logger LOG = LoggerFactory.getLogger(QuoteSearchSyncListener.class);

    @Resource
    private SearchIndexSyncManager searchIndexSyncManager;

    @RabbitListener(queues = "search.sync.queue", ackMode = "MANUAL")
    public void onQuoteEvent(@Payload BusinessEvent event, Message amqpMessage, Channel channel) throws IOException {
        try {
            if (event == null || event.getEventType() != EventType.QUOTE_EVENT) {
                channel.basicAck(amqpMessage.getMessageProperties().getDeliveryTag(), false);
                return;
            }
            Map<String, Object> extInfo = event.getExtInfo();
            if (extInfo == null || !"SEARCH_SYNC".equals(String.valueOf(extInfo.get("action")))) {
                channel.basicAck(amqpMessage.getMessageProperties().getDeliveryTag(), false);
                return;
            }

            String bizType = String.valueOf(extInfo.get("bizType"));
            String opType = String.valueOf(extInfo.get("opType"));
            Long targetId = event.getTargetId();
            if (targetId == null || bizType == null || bizType.isEmpty()) {
                channel.basicAck(amqpMessage.getMessageProperties().getDeliveryTag(), false);
                return;
            }

            if ("DELETE".equalsIgnoreCase(opType)) {
                searchIndexSyncManager.submitDelete(bizType, targetId);
            } else {
                searchIndexSyncManager.submitUpsert(bizType, targetId);
            }
            LOG.info("[search-sync] accepted quote event, opType: {}, targetId: {}", opType, targetId);
            channel.basicAck(amqpMessage.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception ex) {
            LOG.error("[search-sync] consume quote event failed", ex);
            channel.basicReject(amqpMessage.getMessageProperties().getDeliveryTag(), false);
        }
    }
}
