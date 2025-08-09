package org.doubao.share.service.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.doubao.mall.common.entity.Result;
import org.doubao.share.service.dto.ShareLinkCreateDTO;
import org.doubao.share.service.dto.ShareVerifyDTO;
import org.doubao.share.service.entity.ShareLink;
import org.doubao.share.service.service.LinkService;
import org.doubao.share.service.service.RecordService;
import org.doubao.share.service.service.VerifyService;
import org.doubao.share.service.vo.AccessRecordVO;
import org.doubao.share.service.vo.ShareLinkVO;
import org.doubao.share.service.vo.VerifyResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/public/share")
public class ShareLinkPublicController {

	@Autowired
	private LinkService linkService;

	@Autowired
	private VerifyService verifyService;

	@Autowired
	private RecordService recordService;

	@GetMapping("/verify")
	@ApiOperation("验证分享链接访问权限")
	public Result<VerifyResultVO> verifyShareLink(ShareVerifyDTO dto, HttpServletRequest request) {
		VerifyResultVO result = verifyService.verifyShareLink(dto, request);
		return Result.success(result);
	}

	@GetMapping("/detail")
	@ApiOperation("查询引文外链")
	public Result<ShareLink> getShareLinkByQuoteId(
			@RequestParam String shareUrl) {
		ShareLink result = linkService.getShareLinkByUrl(shareUrl);
		return Result.success(result);
	}
}
