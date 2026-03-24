package org.doubao.search.service.listener;

import com.rabbitmq.client.Channel;
import org.doubao.mall.common.entity.BusinessEvent;
import org.doubao.mall.common.enums.EventType;
import org.doubao.search.service.sync.SearchIndexSyncManager;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashMap;
import java.util.Map;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = QuoteSearchSyncListener.class)
public class QuoteSearchSyncListenerTest {

    @Autowired
    private QuoteSearchSyncListener listener;

    @MockBean
    private SearchIndexSyncManager searchIndexSyncManager;

    @Test
    public void shouldSubmitUpsertTaskWhenReceiveSearchSyncEvent() throws Exception {
        BusinessEvent event = new BusinessEvent();
        event.setEventType(EventType.QUOTE_EVENT);
        event.setTargetId(1001L);
        Map<String, Object> extInfo = new HashMap<String, Object>();
        extInfo.put("action", "SEARCH_SYNC");
        extInfo.put("bizType", "quote");
        extInfo.put("opType", "UPSERT");
        event.setExtInfo(extInfo);

        MessageProperties properties = new MessageProperties();
        properties.setDeliveryTag(1L);
        Message message = new Message(new byte[0], properties);
        Channel channel = Mockito.mock(Channel.class);

        listener.onQuoteEvent(event, message, channel);

        Mockito.verify(searchIndexSyncManager).submitUpsert("quote", 1001L);
        Mockito.verify(channel).basicAck(1L, false);
    }

    @Test
    public void shouldIgnoreNonSearchSyncEvent() throws Exception {
        BusinessEvent event = new BusinessEvent();
        event.setEventType(EventType.QUOTE_EVENT);
        event.setTargetId(1002L);
        Map<String, Object> extInfo = new HashMap<String, Object>();
        extInfo.put("action", "VERIFY_QUOTE");
        event.setExtInfo(extInfo);

        MessageProperties properties = new MessageProperties();
        properties.setDeliveryTag(2L);
        Message message = new Message(new byte[0], properties);
        Channel channel = Mockito.mock(Channel.class);

        listener.onQuoteEvent(event, message, channel);

        Mockito.verify(searchIndexSyncManager, Mockito.never())
                .submitUpsert(ArgumentMatchers.anyString(), ArgumentMatchers.anyLong());
        Mockito.verify(channel).basicAck(2L, false);
    }
}
