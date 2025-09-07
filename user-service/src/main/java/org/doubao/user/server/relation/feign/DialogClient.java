package org.doubao.user.server.relation.feign;

import org.doubao.mall.common.entity.Result;
import org.doubao.user.server.core.config.FeignErrorDecoderConfig;
import org.doubao.user.server.relation.service.back.DialogServiceFallback;
import org.doubao.user.server.relation.vo.SessionCreateReq;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "dialog-service", fallback = DialogServiceFallback.class, configuration = FeignErrorDecoderConfig.class)
public interface DialogClient {
	@PostMapping("/dialog/session/create")
	public Result<Long> createSession( @RequestBody SessionCreateReq createReq);
}
