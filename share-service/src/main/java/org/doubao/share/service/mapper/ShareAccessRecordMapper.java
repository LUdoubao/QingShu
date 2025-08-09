package org.doubao.share.service.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.doubao.share.service.entity.ShareAccessRecord;

import java.util.List;

public interface ShareAccessRecordMapper extends BaseMapper<ShareAccessRecord> {
	List<ShareAccessRecord> selectByShareLinkIdAndTimeRange(
			@Param("shareLinkId") Long shareLinkId,
			@Param("startTime") String startTime,
			@Param("endTime") String endTime,
			@Param("offset") int offset,
			@Param("pageSize") int pageSize);

	long countByShareLinkIdAndTimeRange(
			@Param("shareLinkId") Long shareLinkId,
			@Param("startTime") String startTime,
			@Param("endTime") String endTime);
}

