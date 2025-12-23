package org.doubao.user.service.controller.relation;

import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.util.UserContext;
import org.doubao.user.service.service.relation.UserPrivacyService;
import org.doubao.user.service.vo.relation.PrivacySettings;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 用户隐私设置控制器（处理粉丝列表可见性配置）
 */
@RestController
@RequestMapping("/user/privacy")
public class UserPrivacyController {

	@Resource
	private UserPrivacyService privacyService;


	@GetMapping("/settings")
	public Result<PrivacySettings> getSettings() {
		PrivacySettings settings = privacyService.getSettings();
		return Result.success(settings);
	}


	@PostMapping("/update")
	public Result<Void> updateFollowerVisibility(
			@RequestBody PrivacySettings privacySettings
	) {
		privacyService.updateFollowerVisibility(privacySettings);
		return Result.success();
	}
	@GetMapping("/profile")
	public Result<Boolean> profilePermission(@RequestParam("targetUserId") Long targetUserId) {
		boolean permission = privacyService.checkSeePermission(targetUserId, UserContext.getUserId(), PrivacySettings.SeeAccessType.PROFILE);
		return Result.success(permission);
	}
	@GetMapping("/work")
	public Result<Boolean> checkQuoteQueryPermission(@RequestParam ("targetUserId") Long targetUserId,
													 @RequestParam("currentUserId") Long currentUserId) {
		return Result.success(privacyService.checkSeePermission(targetUserId, currentUserId, PrivacySettings.SeeAccessType.WORK));
	}
	@GetMapping("/chat")
	public Result<Boolean> checkChatPermission(@RequestParam ("targetUserId") Long targetUserId,
													 @RequestParam("currentUserId") Long currentUserId) {
		return Result.success(privacyService.checkSeePermission(targetUserId, currentUserId, PrivacySettings.SeeAccessType.CHAT));
	}

}
