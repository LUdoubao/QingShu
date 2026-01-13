package org.doubao.share.service.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.doubao.share.service.entity.ShareAccessRecord;
import org.doubao.share.service.vo.AccessRecordVO;

public interface RecordService extends IService<ShareAccessRecord> {
	void recordAccess(Long shareLinkId, String userId, String ipAddress, Integer accessType);
	Page<AccessRecordVO> getAccessRecords(String quoteId, String startTime, String endTime, int pageNum, int pageSize);
}