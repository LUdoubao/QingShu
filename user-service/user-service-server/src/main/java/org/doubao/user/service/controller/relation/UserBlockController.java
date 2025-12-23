package org.doubao.user.service.controller.relation;

import io.swagger.annotations.ApiOperation;
import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.util.UserContext;
import org.doubao.mall.common.vo.PageResult;
import org.doubao.user.service.dto.relation.UserBlockReq;
import org.doubao.user.service.service.relation.UserBlockService;
import org.doubao.user.service.vo.relation.BlockedUserVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 黑名单控制器
 */
@RestController
@RequestMapping("/user/block")
public class UserBlockController {

	@Resource
	private UserBlockService userBlockService;

	/**
	 * 拉黑用户
	 */
	@PostMapping("/block")
	@ApiOperation("拉黑用户")
	public Result<Void> blockUser(@Valid @RequestBody UserBlockReq req) {
		Long userId = UserContext.getUserId(); // 从上下文获取当前用户ID
		userBlockService.blockUser(userId, req.getTargetUserId());
		return Result.success();
	}

	/**
	 * 解除拉黑
	 */
	@PostMapping("/unblock")
	@ApiOperation("解除拉黑")
	public Result<Void> unblockUser(@Valid @RequestBody UserBlockReq req) {
		Long userId = UserContext.getUserId();
		userBlockService.unblockUser(userId, req.getTargetUserId());
		return Result.success();
	}


	@GetMapping("/list")
	public Result<PageResult<BlockedUserVO>> getBlockList(@RequestParam int page, @RequestParam int size) {
		Long userId = UserContext.getUserId();
		PageResult<BlockedUserVO> list = userBlockService.getBlockList(userId, page, size);
		return Result.success(list);
	}


	@GetMapping("/count")
	public Result<Integer> getBlockCount() {
		Long userId = UserContext.getUserId();
		return Result.success(userBlockService.getBlockCount(userId));
	}

	/**
	 * 检查是否被拉黑
	 */
	@GetMapping("/check")
	@ApiOperation("检查是否被拉黑")
	public Result<Boolean> checkIsBlocked(@RequestParam Long targetUserId) {
		Long userId = UserContext.getUserId();
		boolean isBlocked = userBlockService.checkIsBlocked(userId, targetUserId);
		return Result.success(isBlocked);
	}

	@PostMapping("/checkBatch")
	public Result<Map<Long, Boolean>> checkBatch(@RequestParam Long userId, @RequestBody List<Long> targetUserIds) {
		Map<Long, Boolean> map = userBlockService.checkBatch(userId, targetUserIds);
		return Result.success(map);
	}
}
