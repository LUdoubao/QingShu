package org.doubao.oss.service.service;

import org.doubao.mall.common.dto.FileUploadResult;
import org.doubao.mall.common.entity.Result;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

public interface OssService {
	Result<FileUploadResult> uploadFile(
			MultipartFile file,
			String type);

	String generateAccessUrl(String fileKey, String storageType);
}
