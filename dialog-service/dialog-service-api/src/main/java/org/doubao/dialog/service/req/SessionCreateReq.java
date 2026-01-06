package org.doubao.dialog.service.req;





import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 会话创建请求参数对象
 * 前端调用/dialog/session/create接口时，需传递此参数
 * 用于指定创建会话的目标用户ID和会话类型（用户会话/AI会话）
 * 业务说明：定义创建会话时的请求参数，包含目标用户ID、会话类型等必要信息
 */
//@ApiModel(description = "会话创建请求参数，包含目标ID和会话类型")
public class SessionCreateReq {

    private Long userId;
    /**
     * 目标ID
     * 业务规则：
     * 1. 当会话类型为USER（用户会话）时，必须传递真实的目标用户ID（需通过user-service校验存在性）
     * 2. 当会话类型为AI（AI助手会话）时，此参数会被后端强制覆盖为10000（AI固定ID），前端可传任意值
     * 业务说明：标识会话的目标对象，区分用户间会话和用户AI会话
     * 数据校验：不能为空，必须为正整数（AI会话除外）
     * 使用场景：创建用户间聊天会话或用户与AI助手的对话
     */
    @NotNull(message = "目标ID不能为空，请传递合法的用户ID或AI会话占位ID")
    private Long targetId;

    /**
     * 会话类型
     * 枚举约束：仅支持USER（用户会话）和AI（AI助手会话）
     * 后端会校验此参数合法性，非法值直接返回错误码
     * 业务说明：标识会话的类型，用于确定会话的处理逻辑和权限控制
     * 数据校验：不能为空，仅支持USER和AI
     * 使用场景：区分用户间聊天和用户与AI助手对话的不同处理逻辑
     */
    @NotBlank(message = "会话类型不能为空，请选择USER或AI")
    private String sessionType;

    public @NotNull(message = "目标ID不能为空，请传递合法的用户ID或AI会话占位ID") Long getTargetId() {
        return targetId;
    }

    public void setTargetId(@NotNull(message = "目标ID不能为空，请传递合法的用户ID或AI会话占位ID") Long targetId) {
        this.targetId = targetId;
    }

    public @NotBlank(message = "会话类型不能为空，请选择USER或AI") String getSessionType() {
        return sessionType;
    }

    public void setSessionType(@NotBlank(message = "会话类型不能为空，请选择USER或AI") String sessionType) {
        this.sessionType = sessionType;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
