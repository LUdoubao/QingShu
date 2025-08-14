package org.doubao.oss.service.service.impl;

import org.doubao.mall.common.dto.FileUploadResult;
import org.doubao.mall.common.enums.ErrorCode;
import org.doubao.mall.common.exception.BusinessException;
import org.doubao.oss.service.config.OssProperties;
import org.doubao.oss.service.service.StorageStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;

@Service
public class StorageService {
	private static final Logger LOGGER = LoggerFactory.getLogger(StorageService.class);
	@Resource
	private OssProperties ossProperties;
	@Resource
	private StorageStrategy strategy;
	public FileUploadResult uploadFile(MultipartFile file, boolean saveOriginalName) {
		try (InputStream inputStream = file.getInputStream()) {
			String fileKey = strategy.uploadFile(
					inputStream,
					file.getOriginalFilename(),
					file.getContentType(),
					saveOriginalName
			);

			return new FileUploadResult(
					fileKey,
					ossProperties.getStorage().getType(),
					strategy.getFileUrl(fileKey, ossProperties.getStorage().getType())
			);
		} catch (IOException e) {
			LOGGER.error("文件上传失败", e);
			throw new BusinessException(ErrorCode.FILE_UPLOAD_FAIL);
		}
	}

	public String generateAccessUrl(String fileKey, String storageType) {
		return strategy.getFileUrl(fileKey, storageType);
	}

	public InputStream downloadFile(String fileKey) {
		return strategy.download(fileKey);
	}
}
