package org.doubao.comment.service.service.impl.local;

import org.doubao.comment.service.feign.UserClient;
import org.doubao.mall.common.condition.MonolithMode;
import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.entity.UserInfoDes;
import org.doubao.user.service.service.core.UserService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

/**
 * 评论服务本地用户客户端实现
 * <p>
 * 在单体模式下（service.run-mode=monolith）的用户客户端实现，
 * 通过直接调用本地UserService来获取用户信息，
 * 避免了微服务间的网络调用，提高性能
 */
@Service
@MonolithMode
public class CommentUserClientLocalImpl implements UserClient {

	@Resource
	private UserService userService;

	/**
	 * 根据用户ID列表获取用户信息
	 * <p>
	 * 在单体模式下直接调用本地UserService获取用户信息列表，
	 * 并返回包含用户详细信息的结果
	 * 
	 * @param userIds 用户ID集合
	 * @return 包含用户信息列表的成功响应，或包含错误信息的失败响应
	 */
	@Override
	public Result<List<UserInfoDes>> getUsersByIds(Set<Long> userIds) {
		try {
			List<UserInfoDes> users = userService.usersByIds(userIds);
			return Result.success(users);
		} catch (Exception e) {
			return Result.error("获取用户信息失败: " + e.getMessage());
		}
	}
}