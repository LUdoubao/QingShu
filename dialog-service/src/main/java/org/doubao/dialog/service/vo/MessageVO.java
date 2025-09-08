package org.doubao.dialog.service.vo;

import lombok.Data;
import org.doubao.dialog.service.entity.DialogMessage;

import java.time.LocalDateTime;

/**
 * 消息视图对象（VO）
 * 用于前端消息列表展示，补充发送者信息、消息方向、格式化时间等
 */
public class MessageVO {

    /**
     * 消息ID（MongoDB的ObjectId）
     */
    private String id;

    /**
     * 所属会话ID
     */
    private Long sessionId;

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 发送者昵称
     * 补充字段：AI会话固定为“AI助手”，用户会话从user-service获取
     */
    private String senderNickname;

    /**
     * 发送者头像URL
     * 补充字段：AI会话固定为默认头像，用户会话从user-service获取
     */
    private String senderAvatarUrl;

    /**
     * 接收者ID
     */
    private Long receiverId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息内容预览
     * 补充字段：与会话最后消息预览一致，用于离线通知
     */
    private String contentPreview;

    /**
     * 消息类型（TEXT/EMOJI）
     * 对应DialogMessagePO.ContentTypeEnum.getValue()
     */
    private String contentType;

    /**
     * 消息状态（SENT/READ/FAILED）
     * 对应DialogMessagePO.MessageStatusEnum.getValue()
     */
    private String status;

    /**
     * 发送时间（格式化后）
     * 补充字段：如“15:30”、“昨天 15:30”、“06-12 15:30”
     */
    private String sendTimeStr;

    /**
     * 发送时间原始值（用于排序，前端不展示）
     */
    private LocalDateTime sendTime;

    /**
     * 已读时间
     */
    private LocalDateTime readTime;

    /**
     * 是否撤回（0=否，1=是）
     */
    private Integer isRevoked;

    /**
     * 消息方向（是否为自己发送）
     * 补充字段：true=自己发送（靠右气泡），false=对方发送（靠左气泡）
     */
    private Boolean isSelfSend;

    /**
     * 消息气泡样式（前端用于渲染）
     * 补充字段：self=自己发送样式，other=对方发送样式
     */
    private String bubbleStyle;

    public MessageVO() {
    }

    public MessageVO(Long sessionId, Long senderId) {
        this.sessionId = sessionId;
        this.senderId = senderId;
    }

    /**
     * 从消息PO转换为VO（默认值处理）
     * @param messagePO 消息PO
     * @param currentUserId 当前用户ID（用于判断消息方向）
     * @return 消息VO
     */
    public static MessageVO fromPO(DialogMessage messagePO, Long currentUserId) {
        MessageVO messageVO = new MessageVO();
        // 基础字段复制
        messageVO.setId(messagePO.getId());
        messageVO.setSessionId(messagePO.getSessionId());
        messageVO.setSenderId(messagePO.getSenderId());
        messageVO.setReceiverId(messagePO.getReceiverId());
        messageVO.setContent(messagePO.getContent());
        messageVO.setContentType(messagePO.getContentType().getValue());
        messageVO.setStatus(messagePO.getStatus().getValue());
        messageVO.setSendTime(messagePO.getSendTime());
        messageVO.setReadTime(messagePO.getReadTime());
        messageVO.setIsRevoked(messagePO.getIsRevoked());

        // 补充字段默认值
        messageVO.setSenderNickname("未知用户");
        messageVO.setSenderAvatarUrl("https://picsum.photos/id/1005/40/40"); // 默认头像
        messageVO.setContentPreview("");
        messageVO.setSendTimeStr("");

        // 消息方向与气泡样式
        boolean isSelfSend = messagePO.getSenderId().equals(currentUserId);
        messageVO.setSelfSend(isSelfSend);
        messageVO.setBubbleStyle(isSelfSend ? "self" : "other");

        return messageVO;
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

    public String getSenderAvatarUrl() {
        return senderAvatarUrl;
    }

    public void setSenderAvatarUrl(String senderAvatarUrl) {
        this.senderAvatarUrl = senderAvatarUrl;
    }

    public String getSenderNickname() {
        return senderNickname;
    }

    public void setSenderNickname(String senderNickname) {
        this.senderNickname = senderNickname;
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

    public String getContentPreview() {
        return contentPreview;
    }

    public void setContentPreview(String contentPreview) {
        this.contentPreview = contentPreview;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSendTimeStr() {
        return sendTimeStr;
    }

    public void setSendTimeStr(String sendTimeStr) {
        this.sendTimeStr = sendTimeStr;
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

    public Boolean getSelfSend() {
        return isSelfSend;
    }

    public void setSelfSend(Boolean selfSend) {
        isSelfSend = selfSend;
    }

    public String getBubbleStyle() {
        return bubbleStyle;
    }

    public void setBubbleStyle(String bubbleStyle) {
        this.bubbleStyle = bubbleStyle;
    }
}
  