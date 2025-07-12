package org.doubao.oss.service.controller;

import org.doubao.mall.common.dto.FileUploadDto;
import org.doubao.mall.common.dto.UploadResult;
import org.doubao.mall.common.entity.Result;
import org.doubao.oss.service.service.OssService;
import org.doubao.oss.service.util.FileUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@RestController
@RequestMapping("/oss")
public class OssController {

	@Resource
	private OssService ossService;

	@PostMapping("/upload")
	public Result<UploadResult> uploadFile(
			@RequestParam("file") MultipartFile file,
			@RequestParam(value = "prefix", required = false) String prefix,
			@RequestParam(value = "type", defaultValue = "file") String type) {

		// 文件验证
		if ("image".equalsIgnoreCase(type)) {
			FileUtils.validateImage(file);
		} else {
			FileUtils.validateFile(file);
		}

		FileUploadDto fileUploadDto = new FileUploadDto();
		fileUploadDto.setFile(file);
		fileUploadDto.setPrefix(prefix);

		// 设置图片处理参数
		if ("image".equalsIgnoreCase(type)) {
			fileUploadDto.setMaxWidth(1024); // 最大宽度
			fileUploadDto.setQuality(0.8f);  // 压缩质量
		}

		UploadResult result = ossService.uploadFile(fileUploadDto);
		return Result.success(result);
	}

	@GetMapping("/delete")
	public Result<Void> deleteFile(@RequestParam("path") String filePath) {
		ossService.deleteFile(filePath);
		return Result.success( null);
	}

	@GetMapping("/url")
	public Result<String> getFileUrl(@RequestParam("path") String filePath) {
		String url = ossService.getFileUrl(filePath);
		return Result.success(url);
	}
}