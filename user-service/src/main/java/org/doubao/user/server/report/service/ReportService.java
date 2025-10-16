package org.doubao.user.server.report.service;

import org.doubao.user.server.report.dto.request.ReportSubmitRequest;
import org.doubao.user.server.report.dto.request.ReviewHandleRequest;
import org.doubao.user.server.report.dto.response.ReportStatusResponse;
import org.doubao.user.server.report.dto.response.ReportSubmitResponse;

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
     * @param userId 举报人ID（用于权限校验）
     * @return 举报状态信息
     */
    ReportStatusResponse getReportStatus(Long reportId, Long userId);

    /**
     * 处理审核
     * @param request 审核请求参数
     * @return 处理结果
     */
    Boolean handleReview(ReviewHandleRequest request);
}
