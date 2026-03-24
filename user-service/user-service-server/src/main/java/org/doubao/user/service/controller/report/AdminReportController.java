package org.doubao.user.service.controller.report;


import org.doubao.mall.common.entity.Result;
import org.doubao.user.service.dto.report.request.AdminReportQueryDTO;
import org.doubao.user.service.dto.report.request.ReviewHandleRequest;
import org.doubao.user.service.dto.report.response.ReportPageResponse;
import org.doubao.user.service.dto.report.response.ReportRecordDTO;
import org.doubao.user.service.service.report.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

/**
 * 管理员举报管理控制器
 */
@RestController
@RequestMapping("/user/admin/report")
public class AdminReportController {

	@Autowired
	private ReportService reportService;

	@PostMapping("/query")
	public Result<ReportPageResponse> queryReports(@Valid @RequestBody AdminReportQueryDTO query) {
		return Result.success(reportService.adminQueryReport(query));
	}

	@GetMapping("/{reportId}")
	public Result<ReportRecordDTO> getReportDetail(@PathVariable Long reportId) {
		return Result.success(reportService.getReportDetailForAdmin(reportId));
	}
	@PostMapping("/handle")
	public Result<Boolean> handle(@Valid @RequestBody ReviewHandleRequest request) {
		return Result.success(reportService.handle(request));
	}
}
