package org.doubao.dialog.service.vo;


import org.doubao.dialog.service.entity.DialogSession;

import java.time.LocalDateTime;

/**
 * 会话视图对象（VO）
 * 用于前端会话列表展示，补充目标用户信息、格式化时间等
 */
public class SessionVO {

    /**
     * 会话ID
     */
    private Long id;

    /**
     * 会话类型（USER/AI）
     * 对应DialogSessionPO.SessionTypeEnum.getValue()
     */
    private String sessionType;

    /**
     * 目标用户ID
     */
    private Long targetId;

    /**
     * 目标用户昵称
     * 补充字段：AI会话固定为“AI助手”，用户会话从user-service获取
     */
    private String targetNickname;

    /**
     * 目标用户头像URL
     * 补充字段：AI会话固定为默认头像，用户会话从user-service获取
     */
    private String targetAvatarUrl;

    /**
     * 最后一条消息ID（MongoDB的ObjectId）
     */
    private String lastMsgId;

    /**
     * 最后一条消息预览
     */
    private String lastMsgContent;

    /**
     * 最后一条消息时间（格式化后）
     * 补充字段：如“10分钟前”、“15:30”、“06-12”
     */
    private String lastMsgTimeStr;

    /**
     * 最后一条消息原始时间（用于排序，前端不展示）
     */
    private LocalDateTime lastMsgTime;

    /**
     * 未读消息数
     * 优先从Redis获取，确保实时性
     */
    private Integer unreadCount;

    /**
     * 是否置顶（0=否，1=是）
     */
    private Integer isTop;

    private Integer isLastMsgOwner = 1;

    /**
     * 会话创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 会话更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 未读标识（前端用于显示左侧红色竖线）
     * 补充字段：unreadCount>0则为true
     */
    private Boolean hasUnread;

    private Boolean online = false;

    private Boolean isBlocked = false;

    public Boolean getBlocked() {
        return isBlocked;
    }

    public void setBlocked(Boolean blocked) {
        isBlocked = blocked;
    }

    public Boolean getOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }

    public Integer getIsLastMsgOwner() {
        return isLastMsgOwner;
    }

    public void setIsLastMsgOwner(Integer isLastMsgOwner) {
        this.isLastMsgOwner = isLastMsgOwner;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSessionType() {
        return sessionType;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public String getTargetNickname() {
        return targetNickname;
    }

    public void setTargetNickname(String targetNickname) {
        this.targetNickname = targetNickname;
    }

    public String getTargetAvatarUrl() {
        return targetAvatarUrl;
    }

    public void setTargetAvatarUrl(String targetAvatarUrl) {
        this.targetAvatarUrl = targetAvatarUrl;
    }

    public String getLastMsgId() {
        return lastMsgId;
    }

    public void setLastMsgId(String lastMsgId) {
        this.lastMsgId = lastMsgId;
    }

    public String getLastMsgContent() {
        return lastMsgContent;
    }

    public void setLastMsgContent(String lastMsgContent) {
        this.lastMsgContent = lastMsgContent;
    }

    public String getLastMsgTimeStr() {
        return lastMsgTimeStr;
    }

    public void setLastMsgTimeStr(String lastMsgTimeStr) {
        this.lastMsgTimeStr = lastMsgTimeStr;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getHasUnread() {
        return hasUnread;
    }

    public void setHasUnread(Boolean hasUnread) {
        this.hasUnread = hasUnread;
    }
}
  