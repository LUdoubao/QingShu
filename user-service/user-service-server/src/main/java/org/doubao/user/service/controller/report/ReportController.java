package org.doubao.user.service.controller.report;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.doubao.mall.common.entity.Result;
import org.doubao.user.service.dto.report.request.ReportSubmitRequest;
import org.doubao.user.service.dto.report.response.ReportPageResponse;
import org.doubao.user.service.dto.report.response.ReportStatusResponse;
import org.doubao.user.service.dto.report.response.ReportSubmitResponse;
import org.doubao.user.service.service.report.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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
	public Result<ReportStatusResponse> getReportStatus(
			@PathVariable Long reportId) {
		return Result.success(reportService.getReportStatus(reportId));
	}


	@GetMapping("/my")
	public Result<ReportPageResponse> getUserReports(
			@RequestParam(required = false) Integer pageNum,
			@RequestParam(required = false) Integer pageSize,
			@RequestParam(required = false) Integer status) {
		return Result.success(reportService.userQueryReports(pageNum, pageSize, status));
	}

}
