package org.doubao.topic.service.messaging;

import org.doubao.mall.common.threadpool.CommonTaskExecutor;
import org.doubao.topic.service.feign.UserClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

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
            return null;
        }).whenComplete((v, t) -> {
            if (t != null) {
                LOGGER.error("异步发送MQ消息推送话题动态失败", t);
            }
        });
    }
}