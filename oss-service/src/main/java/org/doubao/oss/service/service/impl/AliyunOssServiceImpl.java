package org.doubao.oss.service.service.impl;


import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
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

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class AliyunOssServiceImpl implements OssService {

	private final OssProperties ossProperties;

	@Autowired
	public AliyunOssServiceImpl(OssProperties ossProperties) {
		this.ossProperties = ossProperties;
	}

	@Override
	public UploadResult uploadFile(FileUploadDto fileUploadDto) {
		MultipartFile file = fileUploadDto.getFile();
		String filePath = generateFilePath(fileUploadDto, file.getOriginalFilename());

		try (InputStream inputStream = file.getInputStream()) {
			uploadToOss(inputStream, filePath);
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

		try {
			// 处理图片
			BufferedImage image = ImageIO.read(file.getInputStream());
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

			// 压缩图片
			ImageUtils.compressImage(image, extension, outputStream, fileUploadDto.getMaxWidth(), fileUploadDto.getQuality());

			// 上传到OSS
			try (InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray())) {
				uploadToOss(inputStream, filePath);
			}

			return new UploadResult(filePath, getFileUrl(filePath));
		} catch (IOException e) {
			throw new FileUploadException("图片上传失败", e);
		}
	}

	@Override
	public String getFileUrl(String filePath) {
		return "https://" + ossProperties.getAliyun().getBucketName() + "." + ossProperties.getAliyun().getEndpoint() + "/" + filePath;
	}

	@Override
	public void deleteFile(String filePath) {
		OSS ossClient = new OSSClientBuilder().build(
				ossProperties.getAliyun().getEndpoint(),
				ossProperties.getAliyun().getAccessKeyId(),
				ossProperties.getAliyun().getAccessKeySecret()
		);

		try {
			ossClient.deleteObject(ossProperties.getAliyun().getBucketName(), filePath);
		} finally {
			ossClient.shutdown();
		}
	}

	private void uploadToOss(InputStream inputStream, String filePath) {
		OSS ossClient = new OSSClientBuilder().build(
				ossProperties.getAliyun().getEndpoint(),
				ossProperties.getAliyun().getAccessKeyId(),
				ossProperties.getAliyun().getAccessKeySecret()
		);

		try {
			PutObjectRequest putObjectRequest = new PutObjectRequest(
					ossProperties.getAliyun().getBucketName(),
					filePath,
					inputStream
			);
			ossClient.putObject(putObjectRequest);
		} finally {
			ossClient.shutdown();
		}
	}

	private String generateFilePath(FileUploadDto fileUploadDto, String originalFilename) {
		String prefix = fileUploadDto.getPrefix() != null ? fileUploadDto.getPrefix() + "/" : "";
		String fileName = UUID.randomUUID().toString() + "." + FilenameUtils.getExtension(originalFilename);
		return prefix + fileName;
	}
}