package org.doubao.user.service.entity.report;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 举报证据实体类（MongoDB）
 */
@Document(collection = "report_evidence")
public class ReportEvidence {

    @Id
    private String id; // MongoDB自动生成ID

    private Long reportId; // 举报ID

    private List<String> evidenceUrls; // 证据链接列表

    private String description; // 补充描述

    private LocalDateTime createTime; // 创建时间

    @Indexed(expireAfterSeconds = 0) // TTL索引，自动过期
    private LocalDateTime expireTime; // 过期时间

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getEvidenceUrls() {
        return evidenceUrls;
    }

    public void setEvidenceUrls(List<String> evidenceUrls) {
        this.evidenceUrls = evidenceUrls;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }
}
