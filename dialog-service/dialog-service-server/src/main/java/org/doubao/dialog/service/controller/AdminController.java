package org.doubao.dialog.service.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.doubao.dialog.service.dto.AdminReplyRequest;
import org.doubao.dialog.service.dto.MessageDTO;
import org.doubao.dialog.service.service.AdminService;
import org.doubao.dialog.service.service.MessageService;
import org.doubao.dialog.service.vo.DialogQuery;
import org.doubao.dialog.service.vo.DialogVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

/**
 * 管理员对话控制器
 * 业务说明：处理管理员对用户对话的管理操作，包括查看对话列表、发送回复、状态标记等
 * 核心功能：1. 提供管理员对话管理接口 2. 支持对话状态管理 3. 实现管理员回复功能
 * 适用场景：
 * 1. 管理员对话管理界面
 * 2. 客服对话处理
 * 3. 对话状态管理
 * 4. 用户问题处理跟踪
 */
@RestController
@RequestMapping("/dialog/admin")
@Api(tags = "管理员对话接口")
public class AdminController {

	/** 管理员服务，用于处理管理员相关的对话操作 */
	@Autowired
	private AdminService adminService;

	/** 消息服务，用于处理对话列表和状态更新操作 */
	@Autowired
	private MessageService messageService;

	/**
	 * 获取管理员对话列表
	 * 业务说明：分页获取需要处理的对话列表，支持按状态、用户、时间等条件筛选
	 * 业务流程：
	 * 1. 接收查询参数（状态、用户ID、时间范围、关键词等）
	 * 2. 调用消息服务获取分页对话列表
	 * 3. 返回分页结果
	 * 参数校验：页码和页大小有默认值，支持按多种条件筛选
	 * 数据处理：按最后消息时间倒序排列，支持分页查询
	 * 接口路径：GET /dialog/admin/list
	 * 访问权限：需管理员认证
	 * @param query 查询条件，包含状态、用户ID、时间范围、关键词等筛选条件
	 * @param pageNum 页码，从1开始，默认为1
	 * @param pageSize 每页大小，默认为10
	 * @return 分页的对话列表，包含对话ID、用户信息、状态、最后消息等
	 */
	@GetMapping("/list")
	@ApiOperation("获取管理员对话列表")
	public ResponseEntity<IPage<DialogVO>> getAdminDialogList(
			DialogQuery query,
			@RequestParam(defaultValue = "1") int pageNum,
			@RequestParam(defaultValue = "10") int pageSize) {
		IPage<DialogVO> dialogs = messageService.getAdminDialogList(query, pageNum, pageSize);
		return ResponseEntity.ok(dialogs);
	}

	/**
	 * 管理员发送回复
	 * 业务说明：管理员对用户咨询进行回复，将回复内容保存并推送给用户
	 * 业务流程：
	 * 1. 接收管理员回复请求（对话ID、管理员ID、回复内容等）
	 * 2. 调用管理员服务发送回复
	 * 3. 保存回复消息到数据库
	 * 4. 通过WebSocket实时推送给用户
	 * 5. 返回回复消息对象
	 * 参数校验：请求对象需通过验证，确保必填字段完整
	 * 数据处理：回复消息会保存到数据库并实时推送给用户
	 * 接口路径：POST /dialog/admin/reply
	 * 访问权限：需管理员认证
	 * @param request 管理员回复请求，包含对话ID、管理员ID、回复内容等信息
	 * @return 回复消息对象，包含回复内容、发送时间等信息
	 */
	@PostMapping("/reply")
	@ApiOperation("管理员发送回复")
	public ResponseEntity<MessageDTO> sendAdminReply(@Valid @RequestBody AdminReplyRequest request) {
		MessageDTO reply = adminService.sendAdminReply(request);
		return ResponseEntity.ok(reply);
	}

	/**
	 * 标记对话为已解决
	 * 业务说明：将指定对话标记为已解决状态，表示问题已处理完毕
	 * 业务流程：
	 * 1. 接收对话ID参数
	 * 2. 调用管理员服务标记对话为已解决
	 * 3. 更新对话状态
	 * 4. 返回操作结果
	 * 参数校验：对话ID不能为空
	 * 数据处理：对话状态更新为已解决状态
	 * 接口路径：PUT /dialog/admin/{dialogId}/resolve
	 * 访问权限：需管理员认证
	 * @param dialogId 对话ID，标识需要标记的对话
	 * @return 操作是否成功，true=成功，false=失败
	 */
	@PutMapping("/{dialogId}/resolve")
	@ApiOperation("标记对话为已解决")
	public ResponseEntity<Boolean> markDialogResolved(@PathVariable Long dialogId) {
		boolean result = adminService.markDialogResolved(dialogId);
		return ResponseEntity.ok(result);
	}

	/**
	 * 标记对话为待跟进
	 * 业务说明：将指定对话标记为待跟进状态，表示需要后续处理
	 * 业务流程：
	 * 1. 接收对话ID参数
	 * 2. 调用管理员服务标记对话为待跟进
	 * 3. 更新对话状态
	 * 4. 返回操作结果
	 * 参数校验：对话ID不能为空
	 * 数据处理：对话状态更新为待跟进状态
	 * 接口路径：PUT /dialog/admin/{dialogId}/pending
	 * 访问权限：需管理员认证
	 * @param dialogId 对话ID，标识需要标记的对话
	 * @return 操作是否成功，true=成功，false=失败
	 */
	@PutMapping("/{dialogId}/pending")
	@ApiOperation("标记对话为待跟进")
	public ResponseEntity<Boolean> markDialogPending(@PathVariable Long dialogId) {
		boolean result = adminService.markDialogPending(dialogId);
		return ResponseEntity.ok(result);
	}

	/**
	 * 更新对话状态
	 * 业务说明：更新指定对话的状态（活跃、已解决、待跟进）
	 * 业务流程：
	 * 1. 接收对话ID和新状态参数
	 * 2. 调用消息服务更新对话状态
	 * 3. 更新数据库中的对话状态
	 * 4. 返回操作结果
	 * 参数校验：对话ID和状态值不能为空
	 * 数据处理：对话状态更新为指定的新状态
	 * 接口路径：PUT /dialog/admin/{dialogId}/status
	 * 访问权限：需管理员认证
	 * @param dialogId 对话ID，标识需要更新状态的对话
	 * @param status 新的状态值，参考DialogStatusEnum
	 * @return 操作是否成功，true=成功，false=失败
	 */
	@PutMapping("/{dialogId}/status")
	@ApiOperation("更新对话状态")
	public ResponseEntity<Boolean> updateDialogStatus(
			@PathVariable Long dialogId,
			@RequestParam Integer status) {
		boolean result = messageService.updateDialogStatus(dialogId, status);
		return ResponseEntity.ok(result);
	}
}