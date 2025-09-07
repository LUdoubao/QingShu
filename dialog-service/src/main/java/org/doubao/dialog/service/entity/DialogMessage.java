package org.doubao.dialog.service.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * 消息表持久化对象（PO）
 * 对应MongoDB集合：dialog_messages
 * 存储私信消息内容及状态
 */
@Document(collection = "dialog_messages") // MongoDB集合名
public class DialogMessage {

    /**
     * 消息ID（MongoDB自动生成的ObjectId）
     * 注解@Id：标记为MongoDB文档主键
     */
    @Id
    private String id;

    /**
     * 所属会话ID
     * 关联MySQL dialog_sessions.id
     */
    private Long sessionId;

    /**
     * 发送者ID
     * 说明：用户会话=用户ID，AI会话=10000（固定）
     */
    private Long senderId;

    /**
     * 接收者ID
     * 说明：始终为用户ID（AI会话接收者是发起会话的用户）
     */
    private Long receiverId;

    /**
     * 消息内容
     * 说明：文字消息=原文，表情消息=编码（如[微笑]）
     */
    private String content;

    /**
     * 消息类型
     * 枚举：TEXT（文字）、EMOJI（表情）
     */
    private ContentTypeEnum contentType;

    /**
     * 消息状态
     * 枚举：SENDING（发送中）、SENT（已发送）、READ（已读）、FAILED（失败）
     */
    private MessageStatusEnum status;

    /**
     * 发送时间
     */
    private LocalDateTime sendTime;

    /**
     * 已读时间
     * 说明：接收者标记消息为已读后填充，初始为null
     */
    private LocalDateTime readTime;

    /**
     * 是否撤回
     * 枚举：0（未撤回）、1（已撤回）
     * 预留字段：后续版本支持消息撤回功能
     */
    private Integer isRevoked;

    /**
     * 消息更新时间
     * 说明：用于记录消息状态更新时间（如重发、标记已读）
     */
    private LocalDateTime updatedAt;

    /**
     * 消息类型枚举
     */
    public enum ContentTypeEnum {
        TEXT("TEXT"),   // 文字消息
        EMOJI("EMOJI"); // 表情消息

        private final String value;

        ContentTypeEnum(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static ContentTypeEnum fromValue(String value) {
            for (ContentTypeEnum type : ContentTypeEnum.values()) {
                if (type.value.equals(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("无效的消息类型：" + value);
        }
    }

    /**
     * 消息状态枚举
     */
    public enum MessageStatusEnum {
        SENDING("SENDING"), // 发送中
        SENT("SENT"),       // 已发送
        READ("READ"),       // 已读
        FAILED("FAILED");   // 发送失败

        private final String value;

        MessageStatusEnum(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static MessageStatusEnum fromValue(String value) {
            for (MessageStatusEnum status : MessageStatusEnum.values()) {
                if (status.value.equals(value)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("无效的消息状态：" + value);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ContentTypeEnum getContentType() {
        return contentType;
    }

    public void setContentType(ContentTypeEnum contentType) {
        this.contentType = contentType;
    }

    public MessageStatusEnum getStatus() {
        return status;
    }

    public void setStatus(MessageStatusEnum status) {
        this.status = status;
    }

    public LocalDateTime getSendTime() {
        return sendTime;
    }

    public void setSendTime(LocalDateTime sendTime) {
        this.sendTime = sendTime;
    }

    public LocalDateTime getReadTime() {
        return readTime;
    }

    public void setReadTime(LocalDateTime readTime) {
        this.readTime = readTime;
    }

    public Integer getIsRevoked() {
        return isRevoked;
    }

    public void setIsRevoked(Integer isRevoked) {
        this.isRevoked = isRevoked;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
  