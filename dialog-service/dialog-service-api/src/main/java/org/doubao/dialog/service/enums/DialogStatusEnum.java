package org.doubao.dialog.service.enums;


/**
 * 对话状态枚举
 * 业务说明：定义对话记录的不同状态，用于跟踪对话处理进度和管理对话生命周期
 * 适用场景：AI助手对话管理、客服对话跟踪、对话状态统计
 */
public enum DialogStatusEnum {

	/**
	 * 活跃状态
	 * 业务说明：对话正在进行中，等待处理或用户继续输入
	 * 编码值：0
	 * 使用场景：新创建的对话、等待AI回复的对话、用户继续输入的对话
	 */
	ACTIVE(0, "活跃"),
	/**
	 * 已解决状态
	 * 业务说明：对话问题已得到解决，无需进一步处理
	 * 编码值：1
	 * 使用场景：用户问题已解决、AI助手提供满意回复、管理员确认问题解决
	 */
	RESOLVED(1, "已解决"),
	/**
	 * 待跟进状态
	 * 业务说明：对话需要后续跟进处理，问题尚未完全解决
	 * 编码值：2
	 * 使用场景：需要更多信息、需要其他部门处理、用户要求后续跟进
	 */
	PENDING(2, "待跟进");

	/**
	 * 状态编码
	 * 业务说明：存储到数据库的状态编码值，用于数据持久化和查询
	 */
	private final Integer code;
	/**
	 * 状态描述
	 * 业务说明：用于前端展示和日志说明的友好描述
	 */
	private final String desc;

	DialogStatusEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static DialogStatusEnum getByCode(Integer code) {
		for (DialogStatusEnum status : values()) {
			if (status.getCode().equals(code)) {
				return status;
			}
		}
		return null;
	}

	public Integer getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}
}