package org.doubao.dialog.service.entity;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * 消息表持久化对象（PO）
 * 对应MongoDB集合：dialog_messages
 * 存储私信消息内容及状态
 * 业务说明：存储用户间私信消息的详细信息，包括内容、状态、时间等，使用MongoDB存储以支持高并发和水平扩展
 */
@Document(collection = "dialog_messages") // MongoDB集合名
public class DialogMessage {

    /**
     * 消息ID（MongoDB自动生成的ObjectId）
     * 注解@Id：标记为MongoDB文档主键
     * 业务说明：MongoDB自动生成的唯一标识符，使用ObjectId类型确保全局唯一性
     * 数据格式：24位十六进制字符串，保证分布式环境下的唯一性
     */
    @Id
    private String id;

    /**
     * 所属会话ID
     * 关联MySQL dialog_sessions.id
     * 业务说明：关联消息所属的对话会话，用于消息归类和会话历史检索
     * 数据类型：Long类型，外键关联MySQL的dialog_sessions表
     * 使用场景：会话消息查询、消息分组显示
     */
    private Long sessionId;

    /**
     * 发送者ID
     * 说明：用户会话=用户ID，AI会话=10000（固定）
     * 业务说明：标识消息的发送方，区分用户发送和AI助手发送
     * 枚举值：普通用户=实际用户ID，AI助手=10000（固定值）
     * 使用场景：消息归属判断、权限控制、用户信息查询
     */
    private Long senderId;

    /**
     * 接收者ID
     * 说明：始终为用户ID（AI会话接收者是发起会话的用户）
     * 业务说明：标识消息的接收方，用于消息路由和权限验证
     * 数据类型：Long类型，关联user表
     * 使用场景：消息推送、未读状态管理、用户在线状态检查
     */
    private Long receiverId;

    /**
     * 消息内容
     * 说明：文字消息=原文，表情消息=编码（如[微笑]）
     * 业务说明：消息的具体内容，支持文字和表情两种类型
     * 数据格式：文字消息存储原始内容，表情消息存储编码格式（如[微笑]）
     * 内容限制：最大500字符，防止过长消息影响系统性能
     */
    private String content;

    /**
     * 消息类型
     * 枚举：TEXT（文字）、EMOJI（表情）
     * 业务说明：标识消息的内容类型，用于前端渲染和消息处理
     * 枚举值：TEXT（文字消息）、EMOJI（表情消息）
     * 使用场景：前端消息样式渲染、消息内容解析、功能权限控制
     */
    private ContentTypeEnum contentType;

    /**
     * 消息状态
     * 枚举：SENDING（发送中）、SENT（已发送）、READ（已读）、FAILED（失败）
     * 业务说明：标识消息的当前状态，用于消息传输控制和状态跟踪
     * 枚举值：SENDING（发送中）、SENT（已发送）、READ（已读）、FAILED（发送失败）
     * 使用场景：消息重发、状态推送、失败处理
     */
    private MessageStatusEnum status;

    /**
     * 发送时间
     * 业务说明：消息的实际发送时间，用于消息排序和时间线展示
     * 数据类型：LocalDateTime，精确到毫秒
     * 时区处理：系统默认时区（东八区）
     * 使用场景：消息时间显示、对话历史按时间排序
     */
    private LocalDateTime sendTime;

    /**
     * 已读时间
     * 说明：接收者标记消息为已读后填充，初始为null
     * 业务说明：记录消息被接收者标记为已读的时间，用于未读消息管理
     * 数据类型：LocalDateTime，精确到毫秒
     * 更新时机：接收者进入会话或手动标记已读时更新
     * 使用场景：未读消息数统计、消息状态显示
     */
    private LocalDateTime readTime;

    /**
     * 是否撤回
     * 枚举：0（未撤回）、1（已撤回）
     * 预留字段：后续版本支持消息撤回功能
     * 业务说明：标识消息是否被发送者撤回，用于消息状态管理
     * 枚举值：0=未撤回、1=已撤回
     * 使用场景：消息撤回功能、消息状态显示
     */
    private Integer isRevoked;

    /**
     * 消息更新时间
     * 说明：用于记录消息状态更新时间（如重发、标记已读）
     * 业务说明：记录消息记录的最后更新时间，用于数据同步和状态跟踪
     * 数据类型：LocalDateTime，精确到毫秒
     * 更新时机：消息状态变更、内容更新时同步更新
     * 使用场景：数据一致性检查、消息同步、状态管理
     */
    private LocalDateTime updatedAt;

    private Integer deleted = 0;

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

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
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
  