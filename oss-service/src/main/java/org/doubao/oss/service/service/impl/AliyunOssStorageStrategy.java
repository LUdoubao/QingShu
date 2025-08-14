package org.doubao.oss.service.service.impl;


import org.doubao.oss.service.config.OssProperties;
import org.doubao.oss.service.service.StorageStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.InputStream;

public class AliyunOssStorageStrategy implements StorageStrategy {

	private final OssProperties ossProperties;

	@Autowired
	public AliyunOssStorageStrategy(OssProperties ossProperties) {
		this.ossProperties = ossProperties;
	}


	@Override
	public String uploadFile(InputStream inputStream, String fileName, String contentType, boolean saveOriginalName) {
		return null;
	}

	@Override
	public String getFileUrl(String filePath, String storageType) {
		return "";
	}

	@Override
	public void deleteFile(String filePath) {

	}

	@Override
	public InputStream download(String fileKey) {
		return null;
	}
}