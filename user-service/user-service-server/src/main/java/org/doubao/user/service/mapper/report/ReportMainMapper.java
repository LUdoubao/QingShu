package org.doubao.user.service.mapper.report;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.user.service.dto.report.request.AdminReportQueryDTO;
import org.doubao.user.service.dto.report.response.ReportRecordDTO;
import org.doubao.user.service.entity.report.ReportMain;

/**
 * 举报主表Mapper接口
 */
@Mapper
public interface ReportMainMapper extends BaseMapper<ReportMain> {

	IPage<ReportRecordDTO> selectAdminReportPage(IPage<ReportRecordDTO> page,
												 @Param("query") AdminReportQueryDTO query);


	IPage<ReportRecordDTO> selectUserReportPage(IPage<ReportRecordDTO> page,
												@Param("userId") Long userId,
												@Param("status") Integer status);
}
