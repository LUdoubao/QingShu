package org.doubao.oss.service.service;


import org.doubao.mall.common.dto.FileUploadDto;
import org.doubao.mall.common.dto.UploadResult;

public interface OssService {
	UploadResult uploadFile(FileUploadDto fileUploadDto);
	String getFileUrl(String filePath);
	void deleteFile(String filePath);
	UploadResult uploadImage(FileUploadDto fileUploadDto);
}
