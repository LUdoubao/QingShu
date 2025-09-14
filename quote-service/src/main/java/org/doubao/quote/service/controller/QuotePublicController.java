package org.doubao.quote.service.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.entity.UserInfo;
import org.doubao.quote.service.dto.PageDto;
import org.doubao.quote.service.dto.QuoteDTO;
import org.doubao.quote.service.dto.QuoteUpdateDto;
import org.doubao.quote.service.entity.Quote;
import org.doubao.quote.service.service.QuoteService;
import org.doubao.quote.service.vo.QuoteVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/public/quote")
public class QuotePublicController {

	@Autowired
	private QuoteService quoteService;
	@GetMapping("/detail/{id}")
	public Result<QuoteVo> detail(@PathVariable Long id) {
		QuoteVo data = quoteService.publicGetDetailById(id).getData();
		if (data != null && data.getUserInfo() != null) {
			UserInfo userInfo = data.getUserInfo();
			// 脱敏
			userInfo.setEmail(null);
			userInfo.setUsername(null);
			userInfo.setId(null);
			userInfo.setSignature(null);
			userInfo.setBgUrl(null);
			userInfo.setCreatedTime(null);
			userInfo.setToken(null);
			userInfo.setRole(null);
			userInfo.setFollow(false);
		}
		return Result.success(data);
	}
}

