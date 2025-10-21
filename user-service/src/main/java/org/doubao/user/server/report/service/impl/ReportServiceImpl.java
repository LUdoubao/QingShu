package org.doubao.user.server.report.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.doubao.mall.common.enums.ErrorCode;
import org.doubao.mall.common.exception.BusinessException;
import org.doubao.mall.common.util.UserContext;
import org.doubao.user.server.report.constant.ReportConstant;
import org.doubao.user.server.report.dto.request.AdminReportQueryDTO;
import org.doubao.user.server.report.dto.request.ReportSubmitRequest;
import org.doubao.user.server.report.dto.request.ReviewHandleRequest;
import org.doubao.user.server.report.dto.response.ReportPageResponse;
import org.doubao.user.server.report.dto.response.ReportRecordDTO;
import org.doubao.user.server.report.dto.response.ReportStatusResponse;
import org.doubao.user.server.report.dto.response.ReportSubmitResponse;
import org.doubao.user.server.report.entity.ReportCategory;
import org.doubao.user.server.report.entity.ReportEvidence;
import org.doubao.user.server.report.entity.ReportMain;
import org.doubao.user.server.report.entity.ReportReviewLog;
import org.doubao.user.server.report.mapper.ReportCategoryMapper;
import org.doubao.user.server.report.mapper.ReportMainMapper;
import org.doubao.user.server.report.mapper.ReportReviewLogMapper;
import org.doubao.user.server.report.repository.ReportEvidenceRepository;
import org.doubao.user.server.report.service.AIPreCheckService;
import org.doubao.user.server.report.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 举报服务实现类
 */
@Service
public class ReportServiceImpl implements ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportServiceImpl.class);
    @Autowired
    private ReportMainMapper reportMainMapper;

    @Autowired
    private ReportCategoryMapper reportCategoryMapper;

    @Autowired
    private ReportReviewLogMapper reportReviewLogMapper;

    @Autowired
    private ReportEvidenceRepository evidenceRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired(required = false) // 允许AI服务未实现
    private AIPreCheckService aiPreCheckService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReportSubmitResponse submitReport(Long userId, ReportSubmitRequest request) {
        // 1. 检查用户当日举报次数是否超限
        checkReportLimit(userId);

        // 2. 验证分类是否有效
        validateCategory(request.getFirstCategoryId(), request.getSecondCategoryId(), request.getThirdCategoryId());

        // 3. 保存举报主数据
        ReportMain reportMain = buildReportMain(userId, request);
        reportMainMapper.insert(reportMain);
        Long reportId = reportMain.getId();
        log.info("用户[{}]提交举报成功，举报ID：{}", userId, reportId);

        // 4. 保存证据信息
        saveEvidence(reportId, request);

        // 5. 执行AI预检测（预留）
        executeAIPreCheck(reportId, request, reportMain);

        // 6. 更新Redis缓存
        updateReportStatusCache(reportId, ReportConstant.REPORT_STATUS_PENDING);

        // 7. 构建返回结果
        ReportSubmitResponse response = new ReportSubmitResponse();
        response.setReportId(reportId);
        response.setStatus(ReportConstant.REPORT_STATUS_PENDING);
        response.setTips("举报提交成功，我们将尽快处理，您可以查询举报状态了解进度");
        return response;
    }

    @Override
    public ReportStatusResponse getReportStatus(Long reportId) {
        Long userId = UserContext.getUserId();
        // 1. 从缓存查询状态
        ReportStatusResponse statusResponse = getReportStatusFromCache(reportId);
        if (statusResponse != null) {
            return statusResponse;
        }

        // 2. 缓存未命中，从数据库查询
        ReportMain reportMain = reportMainMapper.selectById(reportId);
        if (reportMain == null) {
            throw new BusinessException(ErrorCode.USER_REPORT_NOT_FOUND);
        }

        // 3. 权限校验：只能查询自己提交的举报
        if (!reportMain.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.USER_REPORT_NOT_AUTHORIZED);
        }

        // 5. 构建响应
        statusResponse = new ReportStatusResponse();
        statusResponse.setReportId(reportId);
        statusResponse.setStatus(reportMain.getStatus());
        statusResponse.setStatusDesc(getStatusDesc(reportMain.getStatus()));
        statusResponse.setHandleResult(reportMain.getHandleResult());
        statusResponse.setHandleTime(reportMain.getHandleTime() != null ?
                reportMain.getHandleTime().toString() : null);
        statusResponse.setCreatedTime(reportMain.getCreatedTime());

        // 查询证据信息
        ReportEvidence evidence = evidenceRepository.findByReportId(reportId);
        if (evidence != null) {
            if (evidence.getEvidenceUrls() != null) {
                statusResponse.setEvidenceUrls(evidence.getEvidenceUrls());
            }
            statusResponse.setDescription(evidence.getDescription());
        }

        // 6. 更新缓存
        updateReportStatusCache(reportId, reportMain.getStatus(), statusResponse);
        return statusResponse;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean handleReview(ReviewHandleRequest request) {
        // 1. 验证举报是否存在
        ReportMain reportMain = reportMainMapper.selectById(request.getReportId());
        if (reportMain == null) {
            throw new BusinessException(ErrorCode.USER_REPORT_NOT_FOUND);
        }

        // 2. 验证举报状态是否可处理
        if (reportMain.getStatus() != ReportConstant.REPORT_STATUS_PENDING
                && reportMain.getStatus() != ReportConstant.REPORT_STATUS_PROCESSING) {
            throw new BusinessException(ErrorCode.USER_REPORT_STATUS_ERROR);
        }

        // 3. 更新举报状态
        reportMain.setStatus(getTargetStatusByReviewResult(request.getReviewResult()));
        reportMain.setHandleResult(buildHandleResult(request));
        reportMain.setHandleTime(LocalDateTime.now());
        reportMain.setUpdatedTime(LocalDateTime.now());
        reportMainMapper.updateById(reportMain);

        // 4. 保存审核记录
        ReportReviewLog reviewLog = buildReviewLog(request);
        reportReviewLogMapper.insert(reviewLog);
        log.info("举报[{}]审核处理完成，结果：{}", request.getReportId(), request.getReviewResult());

        // 5. 更新缓存
        updateReportStatusCache(request.getReportId(), reportMain.getStatus());

        // 6. 发送通知（实际项目中应通过消息队列异步处理）
        sendNotification(reportMain, request);

        return true;
    }


    /**
     * 检查用户当日举报次数是否超限
     */
    private void checkReportLimit(Long userId) {
        String limitKey = ReportConstant.REDIS_REPORT_LIMIT_PREFIX + userId;
        Long count = redisTemplate.opsForValue().increment(limitKey, 1);

        // 设置过期时间（24小时）
        if (count != null && count == 1) {
            redisTemplate.expire(limitKey, 24, TimeUnit.HOURS);
        }

        // 检查是否超过限制
        if (count != null && count > ReportConstant.MAX_REPORT_PER_DAY) {
            throw new BusinessException(ErrorCode.USER_REPORT_LIMIT);
        }
    }

    /**
     * 验证分类是否有效
     */
    private void validateCategory(Integer firstId, Integer secondId, Integer thirdId) {
        // 检查一级分类是否存在且启用
        ReportCategory firstCategory = reportCategoryMapper.selectById(firstId);
        if (firstCategory == null || firstCategory.getStatus() != 1 || firstCategory.getParentId() != 0) {
            throw new BusinessException(ErrorCode.USER_INVALID_FIRST_CATEGORY);
        }

        // 检查二级分类是否存在且启用
        ReportCategory secondCategory = reportCategoryMapper.selectById(secondId);
        if (secondCategory == null || secondCategory.getStatus() != 1 || !Objects.equals(secondCategory.getParentId(), firstId)) {
            throw new BusinessException(ErrorCode.USER_INVALID_SECOND_CATEGORY);
        }

        // 检查三级分类（如果存在）
        if (thirdId != null) {
            ReportCategory thirdCategory = reportCategoryMapper.selectById(thirdId);
            if (thirdCategory == null || thirdCategory.getStatus() != 1 || !thirdCategory.getParentId().equals(secondId)) {
                throw new BusinessException(ErrorCode.USER_INVALID_THIRD_CATEGORY);
            }
        }
    }

    /**
     * 构建举报主数据实体
     */
    private ReportMain buildReportMain(Long userId, ReportSubmitRequest request) {
        ReportMain reportMain = new ReportMain();
        reportMain.setUserId(userId);
        reportMain.setReportedType(request.getReportedType());
        reportMain.setReportedId(request.getReportedId());
        reportMain.setFirstCategoryId(request.getFirstCategoryId());
        reportMain.setSecondCategoryId(request.getSecondCategoryId());
        reportMain.setThirdCategoryId(request.getThirdCategoryId());
        reportMain.setRiskLevel(ReportConstant.RISK_LEVEL_NORMAL); // 默认正常
        reportMain.setStatus(ReportConstant.REPORT_STATUS_PENDING); // 初始状态：待审核
        reportMain.setCreatedTime(LocalDateTime.now());
        reportMain.setUpdatedTime(LocalDateTime.now());
        return reportMain;
    }

    /**
     * 保存证据信息
     */
    private void saveEvidence(Long reportId, ReportSubmitRequest request) {
        if ((request.getEvidenceUrls() == null || request.getEvidenceUrls().isEmpty())
                && StrUtil.isEmpty(request.getDescription())) {
            return; // 无证据和描述，不保存
        }

        ReportEvidence evidence = new ReportEvidence();
        evidence.setReportId(reportId);
        evidence.setEvidenceUrls(request.getEvidenceUrls());
        evidence.setDescription(request.getDescription());
        evidence.setCreateTime(LocalDateTime.now());
        // 设置过期时间（180天后）
        evidence.setExpireTime(LocalDateTime.now().plusDays(ReportConstant.EVIDENCE_EXPIRE_DAYS));

        evidenceRepository.save(evidence);
    }

    /**
     * 执行AI预检测（完整实现）
     */
    private void executeAIPreCheck(Long reportId, ReportSubmitRequest request, ReportMain reportMain) {
        if (aiPreCheckService == null) {
            log.info("AI预检测服务未实现，跳过预检测流程");
            return;
        }

        try {
            // 调用AI预检测服务
            Integer riskLevel = aiPreCheckService.preCheck(reportId, request.getDescription());

            // 根据AI检测结果更新风险等级
            if (riskLevel != null && (riskLevel == ReportConstant.RISK_LEVEL_HIGH
                    || riskLevel == ReportConstant.RISK_LEVEL_LOW)) {
                reportMain.setRiskLevel(riskLevel);
                reportMain.setUpdatedTime(LocalDateTime.now());
                reportMainMapper.updateById(reportMain);
                log.info("举报[{}]AI预检测完成，风险等级：{}", reportId, riskLevel);
            }
        } catch (Exception e) {
            // AI服务异常不影响主流程，仅记录日志
            log.error("举报[{}]AI预检测失败", reportId, e);
        }
    }

    /**
     * 根据审核结果获取目标状态
     */
    private Integer getTargetStatusByReviewResult(Integer reviewResult) {
        switch (reviewResult) {
            case 1:
                return ReportConstant.REPORT_STATUS_APPROVED;
            case 2:
                return ReportConstant.REPORT_STATUS_REJECTED;
            case 3:
                return ReportConstant.REPORT_STATUS_REVIEW;
            default:
                throw new BusinessException(ErrorCode.USER_INVALID_REPORT_RESULT);
        }
    }

    /**
     * 构建处理结果描述
     */
    private String buildHandleResult(ReviewHandleRequest request) {
        StringBuilder result = new StringBuilder();
        result.append("审核结果：")
                .append(request.getReviewResult() == 1 ? "通过" :
                        request.getReviewResult() == 2 ? "驳回" : "需进一步处理")
                .append("；");

        if (StrUtil.isNotBlank(request.getReviewOpinion())) {
            result.append("审核意见：").append(request.getReviewOpinion());
        }

        return result.toString();
    }

    /**
     * 构建审核日志
     */
    private ReportReviewLog buildReviewLog(ReviewHandleRequest request) {
        ReportReviewLog reviewLog = new ReportReviewLog();
        reviewLog.setReportId(request.getReportId());
        reviewLog.setReviewerId(request.getReviewerId());
        reviewLog.setReviewResult(request.getReviewResult());
        reviewLog.setReviewOpinion(request.getReviewOpinion());
        reviewLog.setCreatedTime(LocalDateTime.now());
        return reviewLog;
    }

    /**
     * 发送处理结果通知
     */
    private void sendNotification(ReportMain reportMain, ReviewHandleRequest request) {
        // 实际项目中应通过消息队列发送通知，这里仅做示例
        log.info("准备发送举报[{}]处理结果通知给用户[{}]",
                reportMain.getId(), reportMain.getUserId());

        // 伪代码：消息队列发送逻辑
    }

    /**
     * 获取证据URL列表
     */
    private List<String> getEvidenceUrls(Long reportId) {
       ReportEvidence reportEvidence = evidenceRepository.findByReportId(reportId);

        List<String> urls = new ArrayList<>();
        if (reportEvidence != null && reportEvidence.getEvidenceUrls() != null) {
            urls.addAll(reportEvidence.getEvidenceUrls());
        }
        return urls;
    }

    /**
     * 获取状态描述
     */
    private String getStatusDesc(Integer status) {
        switch (status) {
            case ReportConstant.REPORT_STATUS_PENDING:
                return "待审核";
            case ReportConstant.REPORT_STATUS_PROCESSING:
                return "处理中";
            case ReportConstant.REPORT_STATUS_APPROVED:
                return "审核通过";
            case ReportConstant.REPORT_STATUS_REJECTED:
                return "审核驳回";
            default:
                return "未知状态";
        }
    }

    /**
     * 从缓存获取举报状态
     */
    private ReportStatusResponse getReportStatusFromCache(Long reportId) {
        String cacheKey = ReportConstant.REDIS_REPORT_STATUS_PREFIX + reportId;
        Object cachedObj = redisTemplate.opsForValue().get(cacheKey);
        return cachedObj instanceof ReportStatusResponse ? (ReportStatusResponse) cachedObj : null;
    }

    /**
     * 更新举报状态缓存
     */
    private void updateReportStatusCache(Long reportId, Integer status) {
        // 如果状态是终态，缓存时间更长
        long expireDays = (status == ReportConstant.REPORT_STATUS_APPROVED
                || status == ReportConstant.REPORT_STATUS_REJECTED)
                ? 30 : 7;

        String cacheKey = ReportConstant.REDIS_REPORT_STATUS_PREFIX + reportId;
        redisTemplate.expire(cacheKey, expireDays, TimeUnit.DAYS);
    }

    /**
     * 更新举报状态缓存（带完整响应数据）
     */
    private void updateReportStatusCache(Long reportId, Integer status, ReportStatusResponse response) {
        String cacheKey = ReportConstant.REDIS_REPORT_STATUS_PREFIX + reportId;
        redisTemplate.opsForValue().set(cacheKey, response);

        // 设置过期时间
        updateReportStatusCache(reportId, status);
    }

    @Override
    public ReportPageResponse adminQueryReport(AdminReportQueryDTO query) {
        // 分页查询
        IPage<ReportRecordDTO> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<ReportRecordDTO> resultPage = reportMainMapper.selectAdminReportPage(page, query);

        // 处理返回结果，设置类型名称等
        resultPage.getRecords().forEach(this::fillReportRecordInfo);

        // 构建分页响应
        ReportPageResponse response = new ReportPageResponse();
        response.setTotal(resultPage.getTotal());
        response.setTotalPages((int) resultPage.getPages());
        response.setPageNum(query.getPageNum());
        response.setPageSize(query.getPageSize());
        response.setRecords(resultPage.getRecords());

        return response;
    }

    @Override
    public ReportPageResponse userQueryReports(Integer pageNum, Integer pageSize, Integer status) {
        Long userId = UserContext.getUserId();
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            pageSize = 10;
        }

        // 分页查询用户的举报记录
        IPage<ReportRecordDTO> page = new Page<>(pageNum, pageSize);
        IPage<ReportRecordDTO> resultPage = reportMainMapper.selectUserReportPage(page, userId, status);

        // 处理返回结果
        resultPage.getRecords().forEach(this::fillReportRecordInfo);

        // 构建分页响应
        ReportPageResponse response = new ReportPageResponse();
        response.setTotal(resultPage.getTotal());
        response.setTotalPages((int) resultPage.getPages());
        response.setPageNum(pageNum);
        response.setPageSize(pageSize);
        response.setRecords(resultPage.getRecords());

        return response;
    }

    @Override
    public ReportRecordDTO getReportDetailForAdmin(Long reportId) {
        // 先检查举报是否存在
        ReportMain reportMain = reportMainMapper.selectById(reportId);
        if (reportMain == null) {
            throw new BusinessException(ErrorCode.USER_REPORT_NOT_FOUND);
        }

        // 查询举报详情
        AdminReportQueryDTO query = new AdminReportQueryDTO();
        query.setPageNum(1);
        query.setPageSize(1);
        query.setReportId(reportId);

        IPage<ReportRecordDTO> page = new Page<>(1, 1);
        IPage<ReportRecordDTO> resultPage = reportMainMapper.selectAdminReportPage(page, query);

        if (resultPage.getRecords().isEmpty()) {
            throw new BusinessException(ErrorCode.USER_REPORT_NOT_FOUND);
        }

        ReportRecordDTO recordDTO = resultPage.getRecords().get(0);
        fillReportRecordInfo(recordDTO);

        // 查询证据信息
        ReportEvidence evidence = evidenceRepository.findByReportId(reportId);
        if (evidence != null) {
            if (evidence.getEvidenceUrls() != null) {
                recordDTO.setEvidenceUrls(evidence.getEvidenceUrls());
            }
            recordDTO.setDescription(evidence.getDescription());
        }

        return recordDTO;
    }

    /**
     * 填充举报记录的辅助信息（类型名称、状态描述等）
     */
    private void fillReportRecordInfo(ReportRecordDTO record) {
        // 设置被举报对象类型名称
        switch (record.getReportedType()) {
            case ReportConstant.REPORTED_TYPE_CONTENT:
                record.setReportedTypeName("内容");
                break;
            case ReportConstant.REPORTED_TYPE_USER:
                record.setReportedTypeName("用户");
                break;
            case ReportConstant.REPORTED_TYPE_COMMENT:
                record.setReportedTypeName("评论");
                break;
            case ReportConstant.REPORTED_TYPE_DIALOG:
                record.setReportedTypeName("对话");
                break;
            default:
                record.setReportedTypeName("未知");
        }

        // 设置风险等级名称
        switch (record.getRiskLevel()) {
            case ReportConstant.RISK_LEVEL_NORMAL:
                record.setRiskLevelName("正常");
                break;
            case ReportConstant.RISK_LEVEL_LOW:
                record.setRiskLevelName("低风险");
                break;
            case ReportConstant.RISK_LEVEL_HIGH:
                record.setRiskLevelName("高风险");
                break;
            default:
                record.setRiskLevelName("未知");
        }

        // 设置状态描述
        record.setStatusDesc(getStatusDesc(record.getStatus()));
    }

}
