package org.doubao.share.service.controller;

import org.doubao.share.service.service.LinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/share/inner")
public class InnerController {

	@Autowired
	private LinkService linkService;

	@PostMapping("/quote/delete")
	public void handleQuoteDelete(@RequestBody Map<String, String> request) {
		String quoteId = request.get("quoteId");
		if (quoteId != null && !quoteId.isEmpty()) {
			linkService.invalidateLinksByQuoteId(quoteId);
		}
	}

	@PostMapping("/view-count")
	public void triggerViewCount(@RequestBody Map<String, Object> request) {
		// 实际实现中可以在这里处理浏览量统计逻辑
		// 本示例中该功能已在VerifyService中实现
	}
}
