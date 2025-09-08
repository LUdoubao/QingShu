package org.doubao.dialog.service.messaging;

import com.alibaba.fastjson.JSONObject;
import org.doubao.dialog.service.entity.DialogSession;
import org.doubao.dialog.service.vo.MessageVO;
import org.doubao.mall.common.constant.Constants;
import org.doubao.mall.common.entity.BusinessEvent;
import org.doubao.mall.common.enums.EventType;
import org.doubao.mall.common.event.DialogEvent;
import org.doubao.mall.common.event.VerifyQuoteEvent;
import org.doubao.mall.common.threadpool.CommonTaskExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class DialogEventPublisher {
	private static final Logger LOGGER = LoggerFactory.getLogger(DialogEventPublisher.class);

	@Autowired
	private CommonTaskExecutor taskExecutor;
	@Autowired
	private RabbitTemplate rabbitTemplate;


	/**
	 * 发送离线系统通知（通过MQ）
	 * @param messageVO 消息VO
	 * @param receiverId 接收者ID
	 * @param sessionPO 会话PO
	 */
	public void sendOfflineNotification(MessageVO messageVO, Long receiverId, DialogSession sessionPO, String senderName) {

		taskExecutor.asyncExecute(() -> {
			// 构建通知内容（如：“AI助手：你好！”）
			String content = String.format("%s：%s", senderName, messageVO.getContentPreview());
			// 设置通知跳转参数（点击通知跳转至会话页）
			JSONObject extra = new JSONObject();
			extra.put("sessionId", sessionPO.getId());
			extra.put("senderId", messageVO.getSenderId());

			DialogEvent dialogEvent = new DialogEvent(
					receiverId,
					messageVO.getId(),
					"新私信消息",
					content,
					extra.toJSONString()
			);
			Map<String, Object> message = new HashMap<>();
			message.put("NotificationEvent", dialogEvent);
			BusinessEvent businessEvent = new BusinessEvent();
			Long timestamp = System.currentTimeMillis();
			String eventId = "NEW_MESSAGE_" + timestamp;
			businessEvent.setEventId(eventId);
			businessEvent.setTimestamp(timestamp);
			businessEvent.setEventType(EventType.NEW_MESSAGE);
			businessEvent.setExtInfo(message);
			rabbitTemplate.convertAndSend(
					Constants.FANOUT_EVENT_EXCHANGE,
					Constants.NEW_MESSAGE_ROUTING_KEY,
					businessEvent
			);
			return null;
		}).whenComplete((v, t) -> {
			if (t != null) {
				LOGGER.error("异步发送MQ消息通知新消息所属用户失败", t);
			}
		});
	}
}