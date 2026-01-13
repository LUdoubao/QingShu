package org.doubao.user.service.constant;

/**
 * 举报服务常量类
 */
public class ReportConstant {

    /**
     * Redis缓存键前缀
     */
    public static final String REDIS_REPORT_LIMIT_PREFIX = "report:limit:";
    public static final String REDIS_REPORT_STATUS_PREFIX = "report:status:";
    public static final String REDIS_REPORT_CATEGORY_TREE = "report:category:tree";

    /**
     * 举报状态
     */
    public static final int REPORT_STATUS_PENDING = 0;      // 待审核
    public static final int REPORT_STATUS_PROCESSING = 1;   // 审核中
    public static final int REPORT_STATUS_APPROVED = 2;     // 审核通过（违规）
    public static final int REPORT_STATUS_REJECTED = 3;     // 审核不通过（无违规）
    public static final int REPORT_STATUS_REVIEW = 4;       // 待复核

    /**
     * 被举报对象类型
     */
    public static final int REPORTED_TYPE_CONTENT = 1;      // 内容
    public static final int REPORTED_TYPE_USER = 2;         // 用户
    public static final int REPORTED_TYPE_COMMENT = 3;      // 评论
    public static final int REPORTED_TYPE_DIALOG = 4;      // 对话

    /**
     * AI预检测风险等级
     */
    public static final int RISK_LEVEL_NORMAL = 0;          // 正常
    public static final int RISK_LEVEL_LOW = 1;             // 低风险
    public static final int RISK_LEVEL_HIGH = 2;            // 高风险

    /**
     * 审核结果
     */
    public static final int REVIEW_RESULT_APPROVED = 1;     // 通过（违规）
    public static final int REVIEW_RESULT_REJECTED = 2;     // 不通过（无违规）

    /**
     * 系统配置
     */
    public static final int MAX_REPORT_PER_DAY = 10;        // 单用户单日最大举报次数
    public static final int EVIDENCE_EXPIRE_DAYS = 180;     // 证据保存天数
    public static final String DEFAULT_EMPTY_CONTENT = "";  // 默认空内容
}
