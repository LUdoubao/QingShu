package org.doubao.user.service.entity.log;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志实体�?
 */
@TableName("operation_log")
public class OperationLog implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 主键ID
	 */
	@TableId(type = IdType.AUTO)
	private Long id;

	/**
	 * 用户账号
	 */
	private String username;

	/**
	 * 操作时间
	 */
	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime operationTime;

	/**
	 * 操作类型
	 * 例如：LOGIN(登录), LOGOUT(登出), CREATE(创建), UPDATE(更新), DELETE(删除)�?
	 */
	private String operationType;

	/**
	 * 网络源地址(IP)
	 */
	private String sourceIp;

	/**
	 * 目标地址
	 */
	private String targetUrl;

	/**
	 * 网络源端�?
	 */
	private Integer sourcePort;

	/**
	 * 客户端硬件特�?
	 * 可存储浏览器信息、设备信息等
	 */
	private String clientHardwareInfo;

	/**
	 * 操作结果
	 */
	private String operationResult;

	/**
	 * 操作详情
	 */
	private String operationDetail;

	/**
	 * 所属服务名
	 */
	private String serviceName;

	/**
	 * 数据创建时间
	 */
	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime createTime;

	/**
	 * 数据更新时间
	 */
	@TableField(fill = FieldFill.INSERT_UPDATE)
	private LocalDateTime updateTime;

	/**
	 * 逻辑删除标识
	 */
	@TableLogic
	private Integer deleted;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public LocalDateTime getOperationTime() {
		return operationTime;
	}

	public void setOperationTime(LocalDateTime operationTime) {
		this.operationTime = operationTime;
	}

	public String getOperationType() {
		return operationType;
	}

	public void setOperationType(String operationType) {
		this.operationType = operationType;
	}

	public String getSourceIp() {
		return sourceIp;
	}

	public void setSourceIp(String sourceIp) {
		this.sourceIp = sourceIp;
	}

	public String getTargetUrl() {
		return targetUrl;
	}

	public void setTargetUrl(String targetUrl) {
		this.targetUrl = targetUrl;
	}

	public Integer getSourcePort() {
		return sourcePort;
	}

	public void setSourcePort(Integer sourcePort) {
		this.sourcePort = sourcePort;
	}

	public String getClientHardwareInfo() {
		return clientHardwareInfo;
	}

	public void setClientHardwareInfo(String clientHardwareInfo) {
		this.clientHardwareInfo = clientHardwareInfo;
	}

	public String getOperationResult() {
		return operationResult;
	}

	public void setOperationResult(String operationResult) {
		this.operationResult = operationResult;
	}

	public String getOperationDetail() {
		return operationDetail;
	}

	public void setOperationDetail(String operationDetail) {
		this.operationDetail = operationDetail;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public Integer getDeleted() {
		return deleted;
	}

	public void setDeleted(Integer deleted) {
		this.deleted = deleted;
	}

	public LocalDateTime getCreateTime() {
		return createTime;
	}

	public void setCreateTime(LocalDateTime createTime) {
		this.createTime = createTime;
	}

	public LocalDateTime getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(LocalDateTime updateTime) {
		this.updateTime = updateTime;
	}
}
