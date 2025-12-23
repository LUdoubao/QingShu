package org.doubao.auth.service.service;

import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.vo.UserLoginVo;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

public interface AuthService {
	Result<UserLoginVo> login(UserLoginVo userLoginVo);

	Result<Long> getTokenExpiration(String token);

	Result<String> webSocket(@RequestParam String token);
}
