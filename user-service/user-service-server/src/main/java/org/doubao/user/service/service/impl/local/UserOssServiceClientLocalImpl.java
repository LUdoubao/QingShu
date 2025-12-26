package org.doubao.user.service.service.impl.local;

import org.doubao.mall.common.dto.FileUploadResult;
import org.doubao.mall.common.entity.Result;
import org.doubao.oss.service.service.OssService;
import org.doubao.user.service.feign.core.OssServiceClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@Service
@ConditionalOnProperty(name = "service.run-mode", havingValue = "monolith", matchIfMissing = true)
public class UserOssServiceClientLocalImpl implements OssServiceClient {
	@Resource
	private OssService ossService;
	@Override
	public Result<FileUploadResult> uploadFile(MultipartFile file) {
		return ossService.uploadFile(file, "file");
	}

	@Override
	public Result<String> generateAccessUrl(String fileKey, String storageType) {
		return Result.success(ossService.generateAccessUrl(fileKey, storageType));
	}
}
