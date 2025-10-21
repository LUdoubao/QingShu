package org.doubao.user.server.report.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.doubao.mall.common.entity.Result;
import org.doubao.user.server.report.dto.request.ReportQueryDTO;
import org.doubao.user.server.report.dto.request.ReportSubmitRequest;
import org.doubao.user.server.report.dto.request.ReviewHandleRequest;
import org.doubao.user.server.report.dto.response.ReportPageResponse;
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
			@PathVariable Long reportId) {
		return Result.success(reportService.getReportStatus(reportId));
	}


	@GetMapping("/my")
	@ApiOperation("用户查询自己的举报记录")
	public Result<ReportPageResponse> getUserReports(
			@RequestParam(required = false) Integer pageNum,
			@RequestParam(required = false) Integer pageSize,
			@RequestParam(required = false) Integer status) {
		return Result.success(reportService.userQueryReports(pageNum, pageSize, status));
	}

}