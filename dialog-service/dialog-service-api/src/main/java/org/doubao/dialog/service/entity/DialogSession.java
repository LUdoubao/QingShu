package org.doubao.dialog.service.entity;

import com.baomidou.mybatisplus.annotation.*;
import org.doubao.mall.common.entity.BaseDel;

import java.time.LocalDateTime;

/**
 * 会话表持久化对象（PO）
 * 对应MySQL表：dialog_sessions
 * 存储用户-用户、用户-AI的会话元数据
 * 业务说明：存储用户会话的元数据信息，包括会话归属、目标用户、消息统计等，支持用户间聊天和用户与AI助手对话
 */
@TableName("dialog_sessions") // 数据库表名
public class DialogSession extends BaseDel {

    /**
     * 会话ID（主键，自增）
     * 业务说明：会话记录的唯一标识符，自增主键
     * 数据类型：Long类型，自动生成
     * 关联关系：作为外键被dialog_messages表引用
     * 使用场景：会话唯一标识、消息关联、权限验证
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会话所有者ID（当前用户ID）
     * 说明：会话归属用户，每个用户对同一目标有独立会话
     * 业务说明：标识会话的创建者和归属用户，用于权限控制和数据隔离
     * 数据类型：Long类型，外键关联user表
     * 使用场景：会话列表查询、权限验证、用户数据隔离
     */
    private Long userId;

    /**
     * 会话目标ID
     * 说明：用户会话=目标用户ID，AI会话=10000（固定）
     * 业务说明：标识会话的目标对象，区分用户间对话和用户AI对话
     * 数据类型：Long类型，用户会话关联user表，AI会话固定为10000
     * 使用场景：消息路由、会话类型判断、目标用户信息获取
     */
    private Long targetId;

    /**
     * 会话类型
     * 枚举：USER（用户会话）、AI（AI助手会话）
     * 业务说明：标识会话的类型，区分用户间聊天和用户与AI助手对话
     * 枚举值：USER（用户会话）、AI（AI助手会话）
     * 使用场景：会话逻辑处理、权限控制、功能限制
     */
    private SessionTypeEnum sessionType;

    /**
     * 最后一条消息ID（MongoDB的ObjectId）
     * 说明：用于快速预览最后一条消息
     * 业务说明：存储会话中最后一条消息的MongoDB ObjectId，用于快速获取最后消息信息
     * 数据格式：MongoDB ObjectId格式的字符串
     * 使用场景：会话列表最后消息预览、消息快速定位
     */
    private String lastMsgId;

    /**
     * 最后一条消息预览
     * 说明：文字取前20字，表情显示[表情]
     * 业务说明：会话列表中显示的最后一条消息内容预览，提升用户体验
     * 数据格式：文字消息取前20字符加...，表情消息显示[表情]
     * 更新时机：发送新消息时同步更新
     * 使用场景：会话列表消息预览、快速了解会话内容
     */
    private String lastMsgContent;

    /**
     * 最后一条消息是否是自己发送的
     * 枚举：0（对方发送）、1（自己发送）
     * 业务说明：标识会话中最后一条消息的发送方，用于前端消息气泡样式判断
     * 枚举值：0=对方发送、1=自己发送
     * 更新时机：发送或接收新消息时同步更新
     * 使用场景：前端消息显示样式、消息方向判断
     */
    private Integer isLastMsgOwner = 1;

    /**
     * 最后一条消息时间
     * 说明：用于会话列表排序（倒序）
     * 业务说明：会话中最后一条消息的发送时间，用于会话列表按时间排序
     * 数据类型：LocalDateTime，精确到秒
     * 更新时机：发送或接收新消息时同步更新
     * 使用场景：会话列表时间排序、最新消息提醒
     */
    private LocalDateTime lastMsgTime;

    /**
     * 未读消息数
     * 说明：仅统计接收方未读的对方消息
     * 业务说明：统计当前用户在该会话中的未读消息数量，用于消息提醒
     * 统计规则：仅统计对方发送的未读消息，不包括自己发送的消息
     * 更新时机：接收新消息时递增，标记已读时清零
     * 使用场景：未读消息提醒、消息状态显示
     */
    private Integer unreadCount;

    /**
     * 是否置顶
     * 枚举：0（不置顶）、1（置顶）
     * 业务说明：标识会话是否置顶显示，置顶会话在会话列表中优先显示
     * 枚举值：0=不置顶、1=置顶
     * 更新时机：用户操作置顶/取消置顶时更新
     * 使用场景：会话列表排序、重要会话优先显示
     */
    private Integer isTop;

    /**
     * 会话是否隐藏
     * 枚举：0（不隐藏）、1（隐藏）
     * 业务说明：标识会话是否被用户隐藏，隐藏的会话不在会话列表中显示
     * 枚举值：0=不隐藏、1=隐藏
     * 更新时机：用户操作隐藏会话时更新
     * 使用场景：会话管理、用户自定义会话显示
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
  