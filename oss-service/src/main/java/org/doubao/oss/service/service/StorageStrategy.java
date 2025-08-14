package org.doubao.oss.service.service;


import java.io.InputStream;

public interface StorageStrategy {
	String uploadFile(InputStream inputStream, String fileName, String contentType, boolean saveOriginalName);
	String getFileUrl(String fileKey, String storageType);
	void deleteFile(String fileKey);
	InputStream download(String fileKey);
}
