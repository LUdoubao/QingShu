package org.doubao.dialog.service.messaging;

import com.alibaba.fastjson.JSONObject;
import org.doubao.dialog.service.vo.MessageVO;
import org.doubao.mall.common.constant.Constants;
import org.doubao.mall.common.entity.BusinessEvent;
import org.doubao.mall.common.enums.EventType;
import org.doubao.mall.common.event.DialogEvent;
import org.doubao.mall.common.threadpool.CommonTaskExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 对话事件发布器
 * 业务说明：负责发布对话相关的业务事件，特别是离线消息通知功能
 * 核心功能：1. 发送离线系统通知 2. 构建通知事件 3. 通过MQ异步发送事件
 * 适用场景：
 * 1. 用户离线时的消息通知
 * 2. 新消息系统通知
 * 3. 业务事件异步处理
 */
@Component
public class DialogEventPublisher {
	/** 日志记录器 */
	private static final Logger LOGGER = LoggerFactory.getLogger(DialogEventPublisher.class);

	/** 通用任务执行器，用于异步执行任务 */
	@Autowired
	private CommonTaskExecutor taskExecutor;
	/** RabbitMQ模板，用于发送消息到队列 */
	@Autowired
	private RabbitTemplate rabbitTemplate;


	/**
	 * 发送离线系统通知（通过MQ）
	 * 业务说明：当用户离线时，通过消息队列发送系统通知给用户
	 * 业务流程：
	 * 1. 异步执行任务避免阻塞主流程
	 * 2. 构建消息内容和事件对象
	 * 3. 设置事件ID、时间戳、事件类型等信息
	 * 4. 通过RabbitMQ发送到指定交换机和路由键
	 * 异常处理：发送失败时记录错误日志
	 * 参数校验：消息VO和接收者ID不能为空
	 * @param messageVO 消息VO，包含消息内容和相关信息
	 * @param receiverId 接收者ID，标识消息接收方
	 * @param senderName 发送者名称，用于通知显示
	 */
	public void sendOfflineNotification(MessageVO messageVO, Long receiverId, String senderName) {

		taskExecutor.asyncExecute(() -> {
			Map<String, Object> message = getStringObjectMap(messageVO, receiverId, senderName);
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

	/**
	 * 构建消息内容映射
	 * 业务说明：根据消息VO、接收者ID和发送者名称构建用于通知的消息内容
	 * 业务流程：
	 * 1. 格式化通知内容（发送者名称：消息预览）
	 * 2. 构建额外信息（会话ID用于跳转）
	 * 3. 创建对话事件对象
	 * 4. 封装为Map返回
	 * 参数校验：消息VO、接收者ID和发送者名称不能为空
	 * 数据处理：通知内容格式为"发送者名称：消息内容"，包含跳转参数
	 * @param messageVO 消息VO，包含消息内容和相关信息
	 * @param receiverId 接收者ID，标识消息接收方
	 * @param senderName 发送者名称，用于通知显示
	 * @return 消息内容映射，用于事件对象
	 */
	private static Map<String, Object> getStringObjectMap(MessageVO messageVO, Long receiverId, String senderName) {
		// 构建通知内容（如："AI助手：你好！"）
		String content = String.format("%s：%s", senderName, messageVO.getContentPreview());
		// 设置通知跳转参数（点击通知跳转至会话页）
		JSONObject extra = new JSONObject();
		extra.put("sessionId", messageVO.getSessionId());

		DialogEvent dialogEvent = new DialogEvent(
				"NEW_MESSAGE",
				receiverId,
				"user",
				messageVO.getId(),
				content,
				messageVO.getSenderId(),
				senderName,
				messageVO.getSenderAvatarUrl(),
				extra.toJSONString(),
				"新私信消息"
		);
		Map<String, Object> message = new HashMap<>();
		message.put("NotificationEvent", dialogEvent);
		return message;
	}
}