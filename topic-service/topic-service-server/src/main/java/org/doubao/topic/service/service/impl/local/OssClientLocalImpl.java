package org.doubao.topic.service.service.impl.local;

import org.doubao.mall.common.entity.Result;
import org.doubao.oss.service.service.OssService;
import org.doubao.topic.service.feign.OssClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class OssClientLocalImpl implements OssClient {

	@Resource
	private OssService ossService;

	@Override
	public Result<String> generateAccessUrl(String fileKey, String storageType) {
		return Result.success(ossService.generateAccessUrl(fileKey, storageType));
	}
}
