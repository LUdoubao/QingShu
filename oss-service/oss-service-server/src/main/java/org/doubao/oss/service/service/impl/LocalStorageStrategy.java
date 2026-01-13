package org.doubao.oss.service.service.impl;

import org.doubao.mall.common.enums.ErrorCode;
import org.doubao.mall.common.exception.BusinessException;
import org.doubao.mall.common.util.DoubaoUtils;
import org.doubao.mall.common.util.EncryptionUtil;
import org.doubao.mall.common.util.NanoId;
import org.doubao.oss.service.config.OssProperties;
import org.doubao.oss.service.service.StorageStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class LocalStorageStrategy implements StorageStrategy {
	private final static Logger LOGGER = LoggerFactory.getLogger(LocalStorageStrategy.class);
	private final String rootLocation;
	private final String baseUrl;
	private final String defaultAvatar;
	private final String aesKey;

	@Resource
	private PathEncryptionService pathEncryptionService;

	@Autowired
	public LocalStorageStrategy(OssProperties ossProperties) {
		this.rootLocation = ossProperties.getLocal().getStoragePath();
		this.baseUrl = ossProperties.getLocal().getBaseUrl();
		this.defaultAvatar = ossProperties.getLocal().getDefaultAvatar();
		this.aesKey = ossProperties.getLocal().getAesKey();
		try {
			Files.createDirectories(Paths.get(rootLocation));
		} catch (IOException e) {
			LOGGER.error("Local folder creation failed", e);
			throw new BusinessException(ErrorCode.LOCAL_FOLDER_CREATED_ERROR);
		}
	}


	@Override
	public String uploadFile(InputStream inputStream, String fileName, String contentType, boolean saveOriginalName) {
		String fileKey = generateFileKey(fileName, saveOriginalName);
		Path path = Paths.get(rootLocation, fileKey);
		try {
			Files.createDirectories(path.getParent());
			Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
			return pathEncryptionService.encryptPath(fileKey);
		} catch (Exception e) {
			LOGGER.error("Local file upload failed", e);
			throw new BusinessException(ErrorCode.LOCAL_FILE_ERROR);
		}
	}
	private String generateFileKey(String fileName, boolean saveOriginalName) {
		if (!saveOriginalName) {
			fileName = NanoId.randomNanoId(8) + "." + fileName.substring(fileName.lastIndexOf(".") + 1);
		} else {
			fileName = NanoId.randomNanoId(6)+ "/" +
					fileName.replaceAll("[^a-zA-Z0-9.\\-]", "_");
		}
		LocalDate currentDate = LocalDate.now();

		// 格式化日期部分为 "年/月/日"
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
		String datePath = currentDate.format(formatter);
		return datePath + "/" + fileName;
	}
	@Override
	public String getFileUrl(String fileKey, String storageType) {
		if (DoubaoUtils.isNull(fileKey) || DoubaoUtils.isNull(storageType) || !storageType.equals("local")) {
			try {
				return baseUrl  + pathEncryptionService.encryptPath(defaultAvatar);
			} catch (Exception e) {
				throw new BusinessException(ErrorCode.LOCAL_URL_ERROR);
			}
		}
		return baseUrl +  fileKey;
	}

	@Override
	public void deleteFile(String fileKey) {
		try {
			String decrypt = pathEncryptionService.decryptPath(fileKey);

			Path path = Paths.get(rootLocation, decrypt);

			Files.deleteIfExists(path);
		} catch (Exception e) {
			LOGGER.error("File deletion failed: {}", fileKey);
			throw new BusinessException(ErrorCode.DELETE_FAIL);
		}
	}

	@Override
	public InputStream download(String fileKey) {
		try {
			String decrypt = pathEncryptionService.decryptPath(fileKey);
			Path path = Paths.get(rootLocation, decrypt);
			return Files.newInputStream(path);
		} catch (Exception e) {
			LOGGER.error("File not found: {}", fileKey);
			LOGGER.error("File not found,error:", e);
			throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
		}
	}
}