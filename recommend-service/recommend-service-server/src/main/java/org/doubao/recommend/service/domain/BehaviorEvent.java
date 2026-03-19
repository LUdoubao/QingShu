package org.doubao.recommend.service.domain;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 用户行为事件对象
 * 用于记录和追踪用户在推荐系统中的各种交互行为
 * 包括浏览、点赞、收藏、评论、分享等操作
 */
public class BehaviorEvent {
    
    /**
     * 用户 ID
     * 标识当前行为的用户唯一标识符
     */
    private Long userId;
    
    /**
     * 用户身份标识
     * 用于区分不同类型的用户身份（如普通用户、VIP 用户等）
     */
    private String userIdentity;
    
    /**
     * 内容 ID
     * 用户行为作用的目标内容 ID
     */
    private Long contentId;
    
    /**
     * 场景标识
     * 标识用户行为发生的场景（如首页、详情页、话题页等）
     */
    private String scene;
    
    /**
     * 行为类型
     * 可选值：view(浏览)/like(点赞)/favorite(收藏)/comment(评论)/share(分享)
     */
    private String actionType;

    /**
     * 行为数值
     * 用于量化行为强度，默认为 1
     * 例如：点赞=1，多次点击可以累加
     */
    private Integer actionValue = 1;
    
    /**
     * 行为持续时间（秒）
     * 记录用户在该内容上的停留时长，默认为 0
     * 主要用于浏览行为的深度评估
     */
    private Integer duration = 0;
    
    /**
     * IP 地址
     * 记录用户行为发生时的客户端 IP 地址
     */
    private String ipAddress;
    
    /**
     * User-Agent
     * 记录用户设备的浏览器或客户端信息
     */
    private String userAgent;
    
    /**
     * 额外信息
     * 存储其他扩展字段，使用 Map 结构便于灵活扩展
     */
    private Map<String, Object> extra;
    
    /**
     * 创建时间
     * 记录行为事件发生的时间戳
     */
    private LocalDateTime createdTime;

    /**
     * 获取用户 ID
     * @return 用户 ID
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 设置用户 ID
     * @param userId 用户 ID
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * 获取用户身份标识
     * @return 用户身份标识
     */
    public String getUserIdentity() {
        return userIdentity;
    }

    /**
     * 设置用户身份标识
     * @param userIdentity 用户身份标识
     */
    public void setUserIdentity(String userIdentity) {
        this.userIdentity = userIdentity;
    }

    /**
     * 获取场景标识
     * @return 场景标识
     */
    public String getScene() {
        return scene;
    }

    /**
     * 设置场景标识
     * @param scene 场景标识
     */
    public void setScene(String scene) {
        this.scene = scene;
    }

    /**
     * 获取内容 ID
     * @return 内容 ID
     */
    public Long getContentId() {
        return contentId;
    }

    /**
     * 设置内容 ID
     * @param contentId 内容 ID
     */
    public void setContentId(Long contentId) {
        this.contentId = contentId;
    }

    /**
     * 获取行为类型
     * @return 行为类型
     */
    public String getActionType() {
        return actionType;
    }

    /**
     * 设置行为类型
     * @param actionType 行为类型
     */
    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    /**
     * 获取行为数值
     * @return 行为数值
     */
    public Integer getActionValue() {
        return actionValue;
    }

    /**
     * 设置行为数值
     * @param actionValue 行为数值
     */
    public void setActionValue(Integer actionValue) {
        this.actionValue = actionValue;
    }

    /**
     * 获取行为持续时间
     * @return 持续时间（秒）
     */
    public Integer getDuration() {
        return duration;
    }

    /**
     * 设置行为持续时间
     * @param duration 持续时间（秒）
     */
    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    /**
     * 获取 IP 地址
     * @return IP 地址
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * 设置 IP 地址
     * @param ipAddress IP 地址
     */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * 获取 User-Agent
     * @return User-Agent
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * 设置 User-Agent
     * @param userAgent User-Agent
     */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    /**
     * 获取额外信息
     * @return 额外信息 Map
     */
    public Map<String, Object> getExtra() {
        return extra;
    }

    /**
     * 设置额外信息
     * @param extra 额外信息 Map
     */
    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }

    /**
     * 获取创建时间
     * @return 创建时间
     */
    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    /**
     * 设置创建时间
     * @param createdTime 创建时间
     */
    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }
}
