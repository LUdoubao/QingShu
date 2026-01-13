package org.doubao.user.service.service.report;


import org.doubao.user.service.dto.report.request.AdminReportQueryDTO;
import org.doubao.user.service.dto.report.request.ReportSubmitRequest;
import org.doubao.user.service.dto.report.request.ReviewHandleRequest;
import org.doubao.user.service.dto.report.response.ReportPageResponse;
import org.doubao.user.service.dto.report.response.ReportRecordDTO;
import org.doubao.user.service.dto.report.response.ReportStatusResponse;
import org.doubao.user.service.dto.report.response.ReportSubmitResponse;

import javax.validation.Valid;

/**
 * 举报服务接口
 */
public interface ReportService {

    /**
     * 提交举报
     * @param userId 举报人ID
     * @param request 举报请求参数
     * @return 举报提交结果
     */
    ReportSubmitResponse submitReport(Long userId, ReportSubmitRequest request);

    /**
     * 查询举报状态
     * @param reportId 举报ID
     * @return 举报状态信息
     */
    ReportStatusResponse getReportStatus(Long reportId);

    /**
     * 处理审核
     * @param request 审核请求参数
     * @return 处理结果
     */
    Boolean handle(ReviewHandleRequest request);

    /**
     * 管理员分页查询举报记录
     */
    ReportPageResponse adminQueryReport(AdminReportQueryDTO query);

    /**
     * 用户查询自己的举报记录
     */
    ReportPageResponse userQueryReports(Integer pageNum, Integer pageSize, Integer status);

    /**
     * 根据举报ID获取举报详情（管理员用）
     */
    ReportRecordDTO getReportDetailForAdmin(Long reportId);
}
