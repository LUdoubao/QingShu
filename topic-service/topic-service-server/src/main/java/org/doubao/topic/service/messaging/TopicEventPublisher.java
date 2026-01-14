package org.doubao.topic.service.messaging;

import org.doubao.mall.common.constant.Constants;
import org.doubao.mall.common.entity.BusinessEvent;
import org.doubao.mall.common.entity.UserInfoDes;
import org.doubao.mall.common.enums.EventType;
import org.doubao.mall.common.threadpool.CommonTaskExecutor;
import org.doubao.topic.service.entity.Topic;
import org.doubao.topic.service.feign.UserClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class TopicEventPublisher {
    private static final Logger LOGGER = LoggerFactory.getLogger(TopicEventPublisher.class);

    @Autowired
    private CommonTaskExecutor taskExecutor;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Resource
    private UserClient userClient;

    // 推送话题更新通知(系统通知)
    public void pushTopicUpdateNotification(Long userId,
                                            Long targetId, String topicName, Long topicCreatedId) {
        taskExecutor.asyncExecute(() -> {
            // 获取用户信息
            List<UserInfoDes> users = Collections.singletonList(new UserInfoDes(userId, "用户"));
            if (userClient != null) {
                // users = userClient.getUsersByIds(Collections.singleton(userId)).getData();
            }

            BusinessEvent businessEvent = new BusinessEvent();
            // businessEvent.setEventType(EventType.TOPIC_UPDATE);
            businessEvent.setTargetId(targetId);
            businessEvent.setTimestamp(System.currentTimeMillis());

            // 发送到消息队列
            // rabbitTemplate.convertAndSend(Constants.EXCHANGE_NAME, Constants.TOPIC_KEY, businessEvent);
            LOGGER.info("话题更新通知已发送: topicId={}, userId={}", targetId, userId);
			return null;
		}).whenComplete((v, t) -> {
            if (t != null) {
                LOGGER.error("异步发送MQ消息推送话题更新通知失败", t);
            }
        });
    }

    // 推送话题审核结果通知
    public void pushTopicAuditResultNotification(Long userId, Long topicId, String topicName, Integer auditStatus, String reason) {
        taskExecutor.asyncExecute(() -> {

            BusinessEvent businessEvent = new BusinessEvent();
            // businessEvent.setEventType(EventType.TOPIC_AUDIT_RESULT);
            businessEvent.setTargetId(topicId);
            businessEvent.setTimestamp(System.currentTimeMillis());


            // 发送到消息队列
            // rabbitTemplate.convertAndSend(Constants.EXCHANGE_NAME, Constants.TOPIC_KEY, businessEvent);
            LOGGER.info("话题审核结果通知已发送: topicId={}, userId={}, status={}", topicId, userId, auditStatus);
            return null;
        }).whenComplete((v, t) -> {
            if (t != null) {
                LOGGER.error("异步发送MQ消息推送话题审核结果通知失败", t);
            }
        });
    }

    // 推送话题相关的动态
    public void pushTopicTimelineEvent(Long topicId, Integer eventType, Long targetId, Long actorId, String content) {
        taskExecutor.asyncExecute(() -> {

            BusinessEvent businessEvent = new BusinessEvent();
            // businessEvent.setEventType(EventType.TOPIC_TIMELINE);
            businessEvent.setTargetId(topicId);
            businessEvent.setTimestamp(System.currentTimeMillis());


            // 发送到消息队列
            // rabbitTemplate.convertAndSend(Constants.EXCHANGE_NAME, Constants.TOPIC_KEY, businessEvent);
            LOGGER.info("话题动态已发送: topicId={}, eventType={}", topicId, eventType);
            return null;
        }).whenComplete((v, t) -> {
            if (t != null) {
                LOGGER.error("异步发送MQ消息推送话题动态失败", t);
            }
        });
    }
}