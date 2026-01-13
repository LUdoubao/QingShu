package org.doubao.user.service.repository;

import org.doubao.user.service.entity.report.ReportEvidence;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


/**
 * 举报证据MongoDB仓库
 */
@Repository
public interface ReportEvidenceRepository extends MongoRepository<ReportEvidence, String> {

    /**
     * 根据举报ID查询证据
     */
    ReportEvidence findByReportId(Long reportId);
}
