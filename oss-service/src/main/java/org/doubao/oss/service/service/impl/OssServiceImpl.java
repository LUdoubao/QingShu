package org.doubao.oss.service.service.impl;

import org.doubao.mall.common.dto.FileUploadResult;
import org.doubao.mall.common.entity.Result;
import org.doubao.oss.service.service.OssService;
import org.doubao.oss.service.util.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@Service
public class OssServiceImpl implements OssService {

	@Resource
	private StorageService storageService;
	@Override
	public Result<FileUploadResult> uploadFile(MultipartFile file, String type) {
		// 文件验证
		if ("image".equalsIgnoreCase(type)) {
			FileUtils.validateImage(file);
		} else {
			FileUtils.validateFile(file);
		}

		FileUploadResult result = storageService.uploadFile(file,false);
		return Result.success(result);
	}

	@Override
	public String generateAccessUrl(String fileKey, String storageType) {
		return storageService.generateAccessUrl(fileKey, storageType);
	}
}
