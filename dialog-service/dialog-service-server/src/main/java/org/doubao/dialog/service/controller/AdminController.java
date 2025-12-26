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
 * 管理员控制器
 */
@RestController
@RequestMapping("/dialog/admin")
@Api(tags = "管理员对话接口")
public class AdminController {

	@Autowired
	private AdminService adminService;

	@Autowired
	private MessageService messageService;

	/**
	 * 获取管理员对话列表
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
	 */
	@PostMapping("/reply")
	@ApiOperation("管理员发送回复")
	public ResponseEntity<MessageDTO> sendAdminReply(@Valid @RequestBody AdminReplyRequest request) {
		MessageDTO reply = adminService.sendAdminReply(request);
		return ResponseEntity.ok(reply);
	}

	/**
	 * 标记对话为已解决
	 */
	@PutMapping("/{dialogId}/resolve")
	@ApiOperation("标记对话为已解决")
	public ResponseEntity<Boolean> markDialogResolved(@PathVariable Long dialogId) {
		boolean result = adminService.markDialogResolved(dialogId);
		return ResponseEntity.ok(result);
	}

	/**
	 * 标记对话为待跟进
	 */
	@PutMapping("/{dialogId}/pending")
	@ApiOperation("标记对话为待跟进")
	public ResponseEntity<Boolean> markDialogPending(@PathVariable Long dialogId) {
		boolean result = adminService.markDialogPending(dialogId);
		return ResponseEntity.ok(result);
	}

	/**
	 * 更新对话状态
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
