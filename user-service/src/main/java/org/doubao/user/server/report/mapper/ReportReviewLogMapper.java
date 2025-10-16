package org.doubao.user.server.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.doubao.user.server.report.entity.ReportReviewLog;

/**
 * 审核记录Mapper接口
 */
@Mapper
public interface ReportReviewLogMapper extends BaseMapper<ReportReviewLog> {
}
