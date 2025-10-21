package org.doubao.user.server.report.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.doubao.mall.common.entity.Result;
import org.doubao.user.server.report.dto.request.AdminReportQueryDTO;
import org.doubao.user.server.report.dto.request.ReviewHandleRequest;
import org.doubao.user.server.report.dto.response.ReportPageResponse;
import org.doubao.user.server.report.dto.response.ReportRecordDTO;
import org.doubao.user.server.report.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 管理员举报管理控制器
 */
@RestController
@RequestMapping("/user/admin/report")
@Api(tags = "管理员举报管理接口")
public class AdminReportController {

	@Autowired
	private ReportService reportService;

	@PostMapping("/query")
	@ApiOperation("管理员查询举报记录（分页）")
	public Result<ReportPageResponse> queryReports(@Valid @RequestBody AdminReportQueryDTO query) {
		return Result.success(reportService.adminQueryReport(query));
	}

	@GetMapping("/{reportId}")
	@ApiOperation("管理员获取举报详情")
	public Result<ReportRecordDTO> getReportDetail(@PathVariable Long reportId) {
		return Result.success(reportService.getReportDetailForAdmin(reportId));
	}
	@PostMapping("/review/handle")
	@ApiOperation("管理员处理审核")
	public Result<Boolean> handleReview(@Valid @RequestBody ReviewHandleRequest request) {
		return Result.success(reportService.handleReview(request));
	}
}