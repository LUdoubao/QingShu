package org.doubao.dialog.service.entity;

import com.baomidou.mybatisplus.annotation.*;
import org.doubao.mall.common.entity.BaseDel;

import java.time.LocalDateTime;

/**
 * 会话表持久化对象（PO）
 * 对应MySQL表：dialog_sessions
 * 存储用户-用户、用户-AI的会话元数据
 */
@TableName("dialog_sessions") // 数据库表名
public class DialogSession extends BaseDel {

    /**
     * 会话ID（主键，自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会话所有者ID（当前用户ID）
     * 说明：会话归属用户，每个用户对同一目标有独立会话
     */
    private Long userId;

    /**
     * 会话目标ID
     * 说明：用户会话=目标用户ID，AI会话=10000（固定）
     */
    private Long targetId;

    /**
     * 会话类型
     * 枚举：USER（用户会话）、AI（AI助手会话）
     */
    private SessionTypeEnum sessionType;

    /**
     * 最后一条消息ID（MongoDB的ObjectId）
     * 说明：用于快速预览最后一条消息
     */
    private String lastMsgId;

    /**
     * 最后一条消息预览
     * 说明：文字取前20字，表情显示[表情]
     */
    private String lastMsgContent;

    /**
     * 最后一条消息是否是自己发送的
     * 枚举：0（对方发送）、1（自己发送）
     */
    private Integer isLastMsgOwner = 1;

    /**
     * 最后一条消息时间
     * 说明：用于会话列表排序（倒序）
     */
    private LocalDateTime lastMsgTime;

    /**
     * 未读消息数
     * 说明：仅统计接收方未读的对方消息
     */
    private Integer unreadCount;

    /**
     * 是否置顶
     * 枚举：0（不置顶）、1（置顶）
     */
    private Integer isTop;

    /**
     * 会话是否隐藏
     * 枚举：0（不隐藏）、1（隐藏）
     */
    private Integer hidden;
    /**
     * 会话类型枚举
     * 与数据库字段值映射（枚举值=数据库存储值）
     */
    public enum SessionTypeEnum {
        USER("USER"), // 用户会话
        AI("AI");     // AI助手会话

        /** 数据库存储值 */
        private final String value;

        SessionTypeEnum(String value) {
            this.value = value;
        }

        /** 获取数据库存储值 */
        public String getValue() {
            return value;
        }

        /** 根据数据库存储值获取枚举 */
        public static SessionTypeEnum fromValue(String value) {
            for (SessionTypeEnum type : SessionTypeEnum.values()) {
                if (type.value.equals(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("无效的会话类型：" + value);
        }
    }

    public Integer getIsLastMsgOwner() {
        return isLastMsgOwner;
    }

    public void setIsLastMsgOwner(Integer isLastMsgOwner) {
        this.isLastMsgOwner = isLastMsgOwner;
    }

    public Integer getHidden() {
        return hidden;
    }

    public void setHidden(Integer hidden) {
        this.hidden = hidden;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public SessionTypeEnum getSessionType() {
        return sessionType;
    }

    public void setSessionType(SessionTypeEnum sessionType) {
        this.sessionType = sessionType;
    }

    public String getLastMsgContent() {
        return lastMsgContent;
    }

    public void setLastMsgContent(String lastMsgContent) {
        this.lastMsgContent = lastMsgContent;
    }

    public String getLastMsgId() {
        return lastMsgId;
    }

    public void setLastMsgId(String lastMsgId) {
        this.lastMsgId = lastMsgId;
    }

    public LocalDateTime getLastMsgTime() {
        return lastMsgTime;
    }

    public void setLastMsgTime(LocalDateTime lastMsgTime) {
        this.lastMsgTime = lastMsgTime;
    }

    public Integer getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }

    public Integer getIsTop() {
        return isTop;
    }

    public void setIsTop(Integer isTop) {
        this.isTop = isTop;
    }
}
  