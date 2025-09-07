package org.doubao.user.server.relation.service.back;

import org.doubao.mall.common.entity.Result;
import org.doubao.user.server.relation.feign.DialogClient;
import org.doubao.user.server.relation.vo.SessionCreateReq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DialogServiceFallback implements DialogClient {

	private static final Logger log = LoggerFactory.getLogger(DialogServiceFallback.class);
	@Override
	public Result<Long> createSession(SessionCreateReq createReq) {
		log.error("调用对话服务创建会话失败");
		return Result.error("对话服务暂时不可用，请稍后重试");
	}
}
