package org.doubao.user.server.relation.feign;

import org.doubao.mall.common.entity.Result;
import org.doubao.user.server.relation.vo.SessionCreateReq;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "dialog-service")
public interface DialogClient {
	@PostMapping("/dialog/session/create")
	public Result<Long> createSession( @RequestBody SessionCreateReq createReq);
}
