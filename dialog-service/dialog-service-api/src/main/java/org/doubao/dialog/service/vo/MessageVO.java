package org.doubao.dialog.service.vo;




import org.doubao.dialog.service.entity.DialogMessage;

import java.time.LocalDateTime;

/**
 * 消息视图对象（VO）
 * 适用场景：
 * 1. 前端消息列表界面展示（聊天界面消息气泡）
 * 2. 为前端提供完整的消息展示信息（发送者昵称、头像、时间格式化等）
 * 3. 用于WebSocket消息推送，包含前端展示所需的所有字段
 * 业务说明：定义消息列表展示的视图对象，封装消息及发送者、展示样式等完整信息
 */
//@ApiModel(description = "消息视图对象，包含消息内容、发送者信息、展示样式等完整信息")
public class MessageVO {

    /**
     * 消息唯一标识ID
     * 业务规则：
     * 1. 对应MongoDB中DialogMessage集合的ObjectId
     * 2. 用于消息的唯一标识、消息撤回、消息状态更新等操作
     * 业务说明：唯一标识一条消息记录，用于消息的关联和查询
     * 数据格式：MongoDB ObjectId字符串
     * 使用场景：消息唯一标识、消息操作、消息关联查询
     */
    private String id;

    /**
     * 所属会话ID
     * 业务规则：
     * 1. 与DialogMessage.sessionId字段保持一致
     * 2. 用于消息归类和会话历史管理
     * 业务说明：标识消息所属的会话，用于消息归类和会话历史管理
     * 数据格式：64位长整型
     * 使用场景：消息归类、会话历史查询、会话维度统计
     */
    private Long sessionId;

    /**
     * 可显示的会话ID
     * 业务规则：
     * 1. 特殊场景下用于显示的会话ID（如会话B中显示会话A给当前用户发送的消息）
     * 2. 通常与sessionId相同，仅在特定业务场景下不同
     * 业务说明：用于在特殊场景下控制消息显示的会话上下文
     * 数据格式：64位长整型
     * 使用场景：跨会话消息展示、会话间消息关联显示
     */
    private Long showSessionId;

    /**
     * 消息发送者ID
     * 业务规则：
     * 1. 与DialogMessage.senderId字段保持一致
     * 2. 用于标识消息的发送方，区分用户消息和AI消息
     * 业务说明：标识消息的发送者，用于消息归属判断和权限控制
     * 数据格式：64位长整型
     * 使用场景：消息归属判断、发送者信息补充、权限控制
     */
    private Long senderId;

        /**
     * 发送者昵称
     * 业务规则：
     * 1. AI会话：固定显示为“AI助手”
     * 2. 用户会话：从user-service接口获取真实用户昵称
     * 3. 若用户信息获取失败，显示“未知用户”
     * 业务说明：提供发送者的可读名称，用于前端展示
     * 数据格式：最大50个字符
     * 使用场景：消息发送者展示、用户识别
     */
    private String senderNickname;

    /**
     * 发送者头像URL
     * 业务规则：
     * 1. AI会话：固定使用默认AI助手头像
     * 2. 用户会话：从user-service接口获取用户头像URL
     * 3. 若头像URL获取失败，使用默认头像
     * 业务说明：提供发送者的头像图片URL，用于前端展示
     * 数据格式：标准HTTP/HTTPS URL
     * 使用场景：消息发送者头像展示、用户识别
     */
    private String senderAvatarUrl;

    /**
     * 消息接收者ID
     * 业务规则：
     * 1. 与DialogMessage.receiverId字段保持一致
     * 2. 用于标识消息的接收方，支持单聊和群聊场景
     * 业务说明：标识消息的接收者，用于消息路由和权限控制
     * 数据格式：64位长整型
     * 使用场景：消息接收者判断、消息路由、权限控制
     */
    private Long receiverId;

    /**
     * 消息内容
     * 业务规则：
     * 1. 根据contentType字段区分文字内容或表情编码
     * 2. 文字消息：直接存储消息文本内容
     * 3. 表情消息：存储表情编码（如[微笑]）
     * 4. 内容长度限制：最大500字符
     * 业务说明：存储消息的具体内容，支持文字和表情类型
     * 数据格式：字符串，最大500字符
     * 使用场景：消息内容展示、消息处理
     */
    private String content;

        /**
     * 消息内容预览
     * 业务规则：
     * 1. 通常为消息内容的前30个字符摘要
     * 2. 用于会话列表中显示最后消息预览
     * 3. 用于离线通知的简要内容展示
     * 业务说明：提供消息的简要预览内容，便于快速了解消息要点
     * 数据格式：字符串，最大30字符
     * 使用场景：会话列表预览、离线通知内容展示
     */
    private String contentPreview;

    /**
     * 消息类型
     * 枚举约束：TEXT（文字消息）、EMOJI（表情消息）
     * 业务规则：
     * 1. TEXT：普通文字消息，内容为文本字符串
     * 2. EMOJI：表情消息，内容为表情编码
     * 3. 对应DialogMessage.ContentTypeEnum.getValue()
     * 业务说明：标识消息的类型，用于消息处理和前端渲染
     * 数据校验：必须为TEXT或EMOJI
     * 使用场景：消息类型判断、前端渲染逻辑
     */
    private String contentType;

    /**
     * 消息状态
     * 枚举约束：SENT（已发送）、READ（已读）、FAILED（发送失败）
     * 业务规则：
     * 1. SENT：消息已成功发送到接收方
     * 2. READ：接收方已读取消息
     * 3. FAILED：消息发送失败，需要重发
     * 4. 对应DialogMessage.MessageStatusEnum.getValue()
     * 业务说明：标识消息的传输状态，用于消息状态追踪
     * 数据校验：必须为SENT、READ或FAILED
     * 使用场景：消息状态展示、消息重发控制
     */
    private String status;

    /**
     * 格式化后的发送时间
     * 业务规则：
     * 1. 根据当前时间和消息发送时间计算出友好格式
     * 2. 如“15:30”（今天）、“昨天 15:30”（昨天）、“06-12 15:30”（更早日期）
     * 3. 便于用户直观了解消息发送时间
     * 业务说明：提供用户友好的时间显示格式，用于前端展示
     * 数据格式：字符串格式的时间
     * 使用场景：前端消息时间展示
     */
    private String sendTimeStr;

        /**
     * 消息原始发送时间
     * 业务规则：
     * 1. 与DialogMessage.sendTime字段保持一致
     * 2. 用于消息排序和时间线管理
     * 3. 为sendTimeStr字段提供原始数据
     * 业务说明：记录消息的准确发送时间，用于排序和时间线管理
     * 数据格式：本地时间格式
     * 使用场景：消息排序、时间线管理、数据库同步
     */
    private LocalDateTime sendTime;

    /**
     * 消息已读时间
     * 业务规则：
     * 1. 仅当消息状态为READ时，此字段才有值
     * 2. 记录接收方首次阅读消息的时间
     * 3. 未读消息此字段为null
     * 业务说明：记录消息被阅读的时间，用于消息状态追踪
     * 数据格式：本地时间格式，可为空
     * 使用场景：消息已读状态追踪、消息统计
     */
    private LocalDateTime readTime;

    /**
     * 消息撤回状态
     * 枚举约束：0（未撤回）、1（已撤回）
     * 业务规则：
     * 1. 0：消息未被撤回，正常显示
     * 2. 1：消息已被发送者撤回，显示为“此消息已撤回”
     * 3. 撤回消息仍保留在数据库中，仅前端展示变化
     * 业务说明：标识消息是否被撤回，用于撤回功能实现
     * 数据校验：必须为0或1
     * 使用场景：消息撤回功能、前端展示控制
     */
    private Integer isRevoked;

    /**
     * 消息方向标识
     * 业务规则：
     * 1. true：当前用户发送的消息（在前端显示为右侧气泡）
     * 2. false：对方发送的消息（在前端显示为左侧气泡）
     * 3. 用于前端消息气泡的样式区分
     * 业务说明：标识消息的发送方向，用于前端展示样式控制
     * 数据格式：布尔值
     * 使用场景：前端消息气泡样式控制、消息方向判断
     */
    private Boolean isSelfSend;

        /**
     * 消息气泡样式
     * 枚举约束：self（自己发送样式）、other（对方发送样式）
     * 业务规则：
     * 1. self：对应自己发送的消息样式（通常为右侧气泡）
     * 2. other：对应对方发送的消息样式（通常为左侧气泡）
     * 3. 为前端提供直接可用的样式标识
     * 业务说明：标识消息的展示样式，用于前端消息气泡渲染
     * 数据校验：必须为self或other
     * 使用场景：前端消息气泡样式控制、消息展示渲染
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

    public Long getShowSessionId() {
        return showSessionId;
    }

    public void setShowSessionId(Long showSessionId) {
        this.showSessionId = showSessionId;
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
  