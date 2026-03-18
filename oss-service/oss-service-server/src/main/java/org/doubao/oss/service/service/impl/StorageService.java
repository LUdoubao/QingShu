package org.doubao.oss.service.service.impl;

import org.doubao.mall.common.dto.FileUploadResult;
import org.doubao.mall.common.enums.ErrorCode;
import org.doubao.mall.common.exception.BusinessException;
import org.doubao.oss.service.config.OssProperties;
import org.doubao.oss.service.service.StorageStrategy;
import org.doubao.oss.service.util.ImageCompressor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Service
public class StorageService {
	private static final Logger LOGGER = LoggerFactory.getLogger(StorageService.class);
	// 需要压缩的图片类型
	private static final Set<String> COMPRESSIBLE_TYPES = new HashSet<>(Arrays.asList(
			"image/jpeg", "image/jpg", "image/png", "image/webp"
	));
	// 压缩阈值：超过 300KB 的图片才压缩
	private static final long COMPRESS_THRESHOLD = 300 * 1024;
	// 压缩质量
	private static final float COMPRESS_QUALITY = 0.85f;
	@Resource
	private OssProperties ossProperties;
	@Resource
	private StorageStrategy strategy;

	public FileUploadResult uploadFile(MultipartFile file, boolean saveOriginalName) {
		String contentType = file.getContentType();
		String originalFilename = file.getOriginalFilename();

		try (InputStream inputStream = file.getInputStream()) {
			byte[] fileBytes;
			long originalSize = file.getSize();

			// 图片且超过阈值，执行智能压缩
			if (isImage(contentType) && originalSize > COMPRESS_THRESHOLD) {
				fileBytes = ImageCompressor.compressIfNeeded(inputStream, COMPRESS_THRESHOLD, COMPRESS_QUALITY);

				long finalSize = fileBytes.length;
				if (finalSize < originalSize) {
					LOGGER.info("图片压缩成功: {} bytes -> {} bytes (减少 {}%)",
							originalSize, finalSize,
							Math.round((1 - (double)finalSize/originalSize) * 100));
				} else {
					LOGGER.info("图片保留原图: {} bytes", originalSize);
				}
			} else {
				fileBytes = ImageCompressor.toByteArray(inputStream);
			}

			// 上传...
			String fileKey = strategy.uploadFile(
					new ByteArrayInputStream(fileBytes),
					originalFilename,
					contentType,
					saveOriginalName
			);

			return new FileUploadResult(fileKey,
					ossProperties.getStorage().getType(),
					strategy.getFileUrl(fileKey, ossProperties.getStorage().getType()));

		} catch (IOException e) {
			LOGGER.error("文件上传失败: {}", originalFilename, e);
			throw new BusinessException(ErrorCode.FILE_UPLOAD_FAIL);
		}
	}

	private boolean isImage(String contentType) {
		return contentType != null && contentType.startsWith("image/");
	}

	public String generateAccessUrl(String fileKey, String storageType) {
		return strategy.getFileUrl(fileKey, storageType);
	}

	public InputStream downloadFile(String fileKey) {
		return strategy.download(fileKey);
	}
}