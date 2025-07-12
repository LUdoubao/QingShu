package org.doubao.oss.service.service.impl;

import org.apache.commons.io.FilenameUtils;
import org.doubao.mall.common.dto.FileUploadDto;
import org.doubao.mall.common.dto.UploadResult;
import org.doubao.oss.service.config.OssProperties;
import org.doubao.oss.service.exception.FileUploadException;
import org.doubao.oss.service.service.OssService;
import org.doubao.oss.service.util.ImageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class LocalStorageServiceImpl implements OssService {

	private final OssProperties ossProperties;
	private final Path rootLocation;
	private final String baseUrl;

	@Autowired
	public LocalStorageServiceImpl(OssProperties ossProperties) {
		this.rootLocation = Paths.get(ossProperties.getLocal().getStoragePath());
		this.baseUrl = ossProperties.getLocal().getBaseUrl();
		this.ossProperties = ossProperties;
		try {
			Files.createDirectories(rootLocation);
		} catch (IOException e) {
			throw new FileUploadException("无法创建存储目录", e);
		}
	}

	@Override
	public UploadResult uploadFile(FileUploadDto fileUploadDto) {
		MultipartFile file = fileUploadDto.getFile();
		String filePath = generateFilePath(fileUploadDto, file.getOriginalFilename());

		try (InputStream inputStream = file.getInputStream()) {
			Path destinationFile = rootLocation.resolve(filePath)
					.normalize()
					.toAbsolutePath();

			Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);

			return new UploadResult(filePath, getFileUrl(filePath));
		} catch (IOException e) {
			throw new FileUploadException("文件上传失败", e);
		}
	}

	@Override
	public UploadResult uploadImage(FileUploadDto fileUploadDto) {
		MultipartFile file = fileUploadDto.getFile();
		String originalFilename = file.getOriginalFilename();
		String extension = FilenameUtils.getExtension(originalFilename);
		String filePath = generateFilePath(fileUploadDto, originalFilename);
		Path destinationFile = rootLocation.resolve(filePath).normalize().toAbsolutePath();

		try {
			// 处理图片
			BufferedImage image = ImageIO.read(file.getInputStream());
			ImageUtils.compressAndSaveImage(image, extension, destinationFile.toString(),
					fileUploadDto.getMaxWidth(), fileUploadDto.getQuality());

			return new UploadResult(filePath, getFileUrl(filePath));
		} catch (IOException e) {
			throw new FileUploadException("图片上传失败", e);
		}
	}

	@Override
	public String getFileUrl(String filePath) {
		return baseUrl + "/" + filePath;
	}

	@Override
	public void deleteFile(String filePath) {
		try {
			Path file = rootLocation.resolve(filePath).normalize().toAbsolutePath();
			Files.deleteIfExists(file);
		} catch (IOException e) {
			throw new FileUploadException("文件删除失败", e);
		}
	}

	private String generateFilePath(FileUploadDto fileUploadDto, String originalFilename) {
		String prefix = fileUploadDto.getPrefix() != null ? fileUploadDto.getPrefix() + "/" : "";
		String fileName = UUID.randomUUID().toString() + "." + FilenameUtils.getExtension(originalFilename);
		return prefix + fileName;
	}
}