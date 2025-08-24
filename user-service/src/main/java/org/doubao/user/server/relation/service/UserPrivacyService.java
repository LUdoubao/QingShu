package org.doubao.user.server.relation.service;


import org.doubao.user.server.relation.vo.PrivacySettings;

public interface UserPrivacyService {
	PrivacySettings getSettings();

	void updateFollowerVisibility(PrivacySettings privacySettings);

	/**
	 * 检查当前用户是否有权限查看
	 */
	boolean checkSeePermission(Long targetUserId, Long currentUserId, int seeAccessType);
}
