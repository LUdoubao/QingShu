package org.doubao.quote.service.controller;

import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.entity.UserInfoDes;
import org.doubao.quote.service.service.QuoteService;
import org.doubao.quote.service.vo.QuoteVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/public/quote")
public class QuotePublicController {

	@Autowired
	private QuoteService quoteService;
	@GetMapping("/detail/{id}")
	public Result<QuoteVo> detail(@PathVariable Long id) {
		QuoteVo data = quoteService.publicGetDetailById(id,  null).getData();
		if (data != null && data.getUserInfo() != null) {
			UserInfoDes userInfo = data.getUserInfo();
			// 脱敏
			userInfo.setId(null);
		}
		return Result.success(data);
	}
}

