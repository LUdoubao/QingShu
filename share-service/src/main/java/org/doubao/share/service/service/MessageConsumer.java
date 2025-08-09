package org.doubao.share.service.service;
import org.doubao.share.service.entity.ShareAccessRecord;
import org.doubao.share.service.mapper.ShareAccessRecordMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

import static org.doubao.share.service.config.RabbitMQConfig.ACCESS_RECORD_QUEUE;

@Service
public class MessageConsumer {

	@Autowired
	private ShareAccessRecordMapper accessRecordMapper;

	@RabbitListener(queues = ACCESS_RECORD_QUEUE)
	public void handleAccessRecord(Map<String, Object> message) {
		ShareAccessRecord record = new ShareAccessRecord();
		record.setShareLinkId(Long.valueOf(message.get("shareLinkId").toString()));
		record.setUserId((String) message.get("userId"));
		record.setAccessTime((LocalDateTime) message.get("accessTime"));
		record.setIpAddress((String) message.get("ipAddress"));
		record.setAccessType(Integer.valueOf(message.get("accessType").toString()));

		accessRecordMapper.insert(record);
	}
}
