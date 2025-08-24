package org.doubao.oss.service.controller;

import org.doubao.mall.common.dto.FileUploadDto;
import org.doubao.mall.common.dto.FileUploadResult;
import org.doubao.mall.common.entity.Result;
import org.doubao.oss.service.service.impl.StorageService;
import org.doubao.oss.service.util.FileUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@RestController
@RequestMapping("/oss")
public class OssController {

	@Resource
	private StorageService storageService;

	@PostMapping("/upload")
	public Result<FileUploadResult> uploadFile(
			@RequestParam("file") MultipartFile file,
			@RequestParam(value = "type", defaultValue = "file") String type) {

		// 文件验证
		if ("image".equalsIgnoreCase(type)) {
			FileUtils.validateImage(file);
		} else {
			FileUtils.validateFile(file);
		}

		FileUploadResult result = storageService.uploadFile(file,false);
		return Result.success(result);
	}
}