package org.doubao.share.service.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.doubao.mall.common.util.IPUtil;
import org.doubao.share.service.entity.ShareAccessRecord;
import org.doubao.share.service.entity.ShareLink;
import org.doubao.share.service.mapper.ShareAccessRecordMapper;
import org.doubao.share.service.mapper.ShareLinkMapper;
import org.doubao.share.service.service.RecordService;
import org.doubao.share.service.vo.AccessRecordVO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class RecordServiceImpl extends ServiceImpl<ShareAccessRecordMapper, ShareAccessRecord> implements RecordService {

	@Autowired
	private ShareAccessRecordMapper accessRecordMapper;

	@Autowired
	private ShareLinkMapper shareLinkMapper;

	@Autowired
	private RabbitTemplate rabbitTemplate;


	@Override
	public void recordAccess(Long shareLinkId, String userId, String ipAddress, Integer accessType) {
		// 异步记录访问信息
		Map<String, Object> message = new HashMap<>();
		message.put("shareLinkId", shareLinkId);
		message.put("userId", userId);
		message.put("accessTime", new Date());
		message.put("ipAddress", IPUtil.maskIp(ipAddress));
		message.put("accessType", accessType);

		// rabbitTemplate.convertAndSend(SHARE_EXCHANGE, ACCESS_RECORD_ROUTING_KEY, message);
	}

	@Override
	public Page<AccessRecordVO> getAccessRecords(String quoteId, String startTime, String endTime,
												 int pageNum, int pageSize) {
		// 1. 获取该文案的所有分享链接
		List<ShareLink> shareLinks = shareLinkMapper.selectByQuoteId(quoteId);
		if (shareLinks.isEmpty()) {
			Page<AccessRecordVO> result = new Page<>();
			result.setTotal(0);
			result.setRecords(Collections.emptyList());
			return result;
		}

		// 2. 分页查询访问记录
		int offset = (pageNum - 1) * pageSize;
		List<ShareAccessRecord> records = new ArrayList<>();

		for (ShareLink link : shareLinks) {
			records.addAll(accessRecordMapper.selectByShareLinkIdAndTimeRange(
					link.getId(), startTime, endTime, offset, pageSize));
		}

		// 3. 计算总记录数
		long total = 0;
		for (ShareLink link : shareLinks) {
			total += accessRecordMapper.countByShareLinkIdAndTimeRange(link.getId(), startTime, endTime);
		}

		// 4. 转换为VO
		List<AccessRecordVO> recordVOs = records.stream().map(record -> {
			AccessRecordVO vo = new AccessRecordVO();
			vo.setId(record.getId().toString());
			vo.setAccessTime(record.getAccessTime());
			vo.setIp(record.getIpAddress());
			vo.setAccessType(getAccessTypeString(record.getAccessType()));
			vo.setUserId(record.getUserId());
			return vo;
		}).collect(Collectors.toList());

		// 5. 构建分页结果
		Page<AccessRecordVO> result = new Page<>();
		result.setTotal(total);
		result.setRecords(recordVOs);

		return result;
	}

	private String getAccessTypeString(int accessType) {
		switch (accessType) {
			case 1: return "PUBLIC";
			case 2: return "PASSWORD";
			case 3: return "SPECIFIED";
			default: return "UNKNOWN";
		}
	}
}
