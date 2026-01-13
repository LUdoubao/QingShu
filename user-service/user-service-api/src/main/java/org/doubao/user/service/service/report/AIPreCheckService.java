package org.doubao.user.service.service.report;


import org.doubao.user.service.constant.ReportConstant;

/**
 * AI预检测服务接口（预留扩展）
 */
public interface AIPreCheckService {

    /**
     * AI预检测
     * @param reportId 举报ID
     * @param content 被举报内容
     * @return 风险等级：0=正常，1=低风险，2=高风险
     */
    default Integer preCheck(Long reportId, String content) {
        // 暂时默认返回正常，后续实现AI检测时重写此方法
        return ReportConstant.RISK_LEVEL_NORMAL;
    }
}
