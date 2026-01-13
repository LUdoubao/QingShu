package org.doubao.oss.service.util;


import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

public class FileUtils {

	private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
			"image/jpeg", "image/png", "image/gif", "image/webp"
	);

	private static final List<String> ALLOWED_FILE_TYPES = Arrays.asList(
			"application/pdf", "application/msword",
			"application/vnd.openxmlformats-officedocument.wordprocessingml.document",
			"application/vnd.ms-excel",
			"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
			"text/plain"
	);

	public static void validateImage(MultipartFile file) {
		if (file.isEmpty()) {
			throw new IllegalArgumentException("文件不能为空");
		}

		String contentType = file.getContentType();
		if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
			throw new IllegalArgumentException("不支持的文件类型: " + contentType);
		}

		long maxSize = 5 * 1024 * 1024; // 5MB
		if (file.getSize() > maxSize) {
			throw new IllegalArgumentException("文件大小不能超过5MB");
		}
	}

	public static void validateFile(MultipartFile file) {
		if (file.isEmpty()) {
			throw new IllegalArgumentException("文件不能为空");
		}

		String contentType = file.getContentType();
		if (contentType == null || !ALLOWED_FILE_TYPES.contains(contentType)) {
			throw new IllegalArgumentException("不支持的文件类型: " + contentType);
		}

		long maxSize = 10 * 1024 * 1024; // 10MB
		if (file.getSize() > maxSize) {
			throw new IllegalArgumentException("文件大小不能超过10MB");
		}
	}

	public static String getFileExtension(String filename) {
		return FilenameUtils.getExtension(filename);
	}
}