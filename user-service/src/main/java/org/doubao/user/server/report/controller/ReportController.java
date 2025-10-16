package org.doubao.user.server.report.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.doubao.mall.common.entity.Result;
import org.doubao.user.server.report.dto.request.ReportQueryDTO;
import org.doubao.user.server.report.dto.request.ReportSubmitRequest;
import org.doubao.user.server.report.dto.request.ReviewHandleRequest;
import org.doubao.user.server.report.dto.response.ReportStatusResponse;
import org.doubao.user.server.report.dto.response.ReportSubmitResponse;
import org.doubao.user.server.report.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 举报功能控制器
 */
@RestController
@RequestMapping("/user/report")
@Api(tags = "举报管理接口")
public class ReportController {

	@Autowired
	private ReportService reportService;

	@PostMapping("/submit")
	@ApiOperation("用户提交举报")
	public Result<ReportSubmitResponse> submitReport(
			@RequestHeader("userId") Long userId,
			@Valid @RequestBody ReportSubmitRequest request) {
		return Result.success(reportService.submitReport(userId, request));
	}

	@GetMapping("/status/{reportId}")
	@ApiOperation("查询举报状态")
	public Result<ReportStatusResponse> getReportStatus(
			@PathVariable Long reportId,
			@RequestHeader("userId") Long userId) {
		return Result.success(reportService.getReportStatus(reportId, userId));
	}

	@PostMapping("/review/handle")
	@ApiOperation("管理员处理审核")
	public Result<Boolean> handleReview(@Valid @RequestBody ReviewHandleRequest request) {
		return Result.success(reportService.handleReview(request));
	}
}