package org.doubao.user.server.service.back;

import org.doubao.mall.common.dto.FileUploadResult;
import org.doubao.mall.common.entity.Result;
import org.doubao.user.server.feign.OssServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

// Feign降级处理
@Component
public class OssServiceFallback implements OssServiceClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(OssServiceFallback.class);

	@Override
	public Result<FileUploadResult> uploadFile(MultipartFile file) {
		LOGGER.error("oss服务uploadFile暂时不可用，触发降级处理");
		return Result.error("文件上传服务暂时不可用，请稍后重试");
	}

	@Override
	public Result<String> generateAccessUrl(String fileKey, String storageType) {
		LOGGER.error("oss服务generateAccessUrl暂时不可用，触发降级处理");
		return Result.success(null);
	}
}