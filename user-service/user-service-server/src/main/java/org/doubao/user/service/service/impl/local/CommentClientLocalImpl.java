package org.doubao.user.service.service.impl.local;

import org.doubao.comment.service.service.CommentService;
import org.doubao.mall.common.condition.MonolithMode;
import org.doubao.mall.common.entity.Result;
import org.doubao.user.service.feign.report.CommentClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

@Service
@MonolithMode
public class CommentClientLocalImpl implements CommentClient {

	@Resource
	private CommentService commentService;

	@Override
	public Result<Void> updateStatus(Map<String, String> request) {
		try {
			commentService.updateStatus(request);
			return Result.success();
		} catch (Exception e) {
			return Result.error("更新评论状态失败: " + e.getMessage());
		}
	}
}