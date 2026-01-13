package org.doubao.share.service.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
@RequestMapping("/share")
public class ShareLinkController {

	@Autowired
	private LinkService linkService;

	@Autowired
	private VerifyService verifyService;

	@Autowired
	private RecordService recordService;

	@PostMapping("/links")
	public Result<ShareLinkVO> createShareLink(@RequestBody ShareLinkCreateDTO dto) {
		ShareLinkVO result = linkService.createShareLink(dto);
		return Result.success(result);
	}

	@GetMapping("/verify")
	public Result<VerifyResultVO> verifyShareLink(ShareVerifyDTO dto, HttpServletRequest request) {
		VerifyResultVO result = verifyService.verifyShareLink(dto, request);
		return Result.success(result);
	}

	@GetMapping("/records")
	public Result<Page<AccessRecordVO>> getAccessRecords(
			@RequestParam String quoteId,
			@RequestParam(required = false) String startTime,
			@RequestParam(required = false) String endTime,
			@RequestParam(defaultValue = "1") int pageNum,
			@RequestParam(defaultValue = "10") int pageSize) {
		Page<AccessRecordVO> result = recordService.getAccessRecords(quoteId, startTime, endTime, pageNum, pageSize);
		return Result.success(result);
	}

	@PostMapping("/update/{linkId}")
	public Result<ShareLinkVO> updateShareLink(
			@PathVariable Long linkId,
			@RequestBody ShareLinkCreateDTO dto) {
		ShareLinkVO result = linkService.updateShareLink(linkId, dto);
		return Result.success(result);
	}

	@GetMapping("/link/{quoteId}")
	public Result<ShareLink> getShareLinkByQuoteId(
			@PathVariable Long quoteId) {
		ShareLink result = linkService.getShareLinkByQuoteId(quoteId);
		return Result.success(result);
	}
}