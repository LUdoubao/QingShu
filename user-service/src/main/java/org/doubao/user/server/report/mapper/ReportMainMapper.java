package org.doubao.user.server.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.user.server.report.dto.request.AdminReportQueryDTO;
import org.doubao.user.server.report.dto.response.ReportRecordDTO;
import org.doubao.user.server.report.entity.ReportMain;

/**
 * 举报主表Mapper接口
 */
@Mapper
public interface ReportMainMapper extends BaseMapper<ReportMain> {
	/**
	 * 管理员分页查询举报记录
	 */
	IPage<ReportRecordDTO> selectAdminReportPage(IPage<ReportRecordDTO> page,
												 @Param("query") AdminReportQueryDTO query);

	/**
	 * 用户查询自己的举报记录
	 */
	IPage<ReportRecordDTO> selectUserReportPage(IPage<ReportRecordDTO> page,
												@Param("userId") Long userId,
												@Param("status") Integer status);
}
