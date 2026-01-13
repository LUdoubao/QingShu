package org.doubao.user.service.service.relation;


import org.doubao.user.service.vo.relation.PrivacySettings;

public interface UserPrivacyService {
	PrivacySettings getSettings();

	void updateFollowerVisibility(PrivacySettings privacySettings);

	/**
	 * 检查当前用户是否有权限查看
	 */
	boolean checkSeePermission(Long targetUserId, Long currentUserId, int seeAccessType);
}
