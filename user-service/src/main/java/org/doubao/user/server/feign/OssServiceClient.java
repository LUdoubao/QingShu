package org.doubao.user.server.feign;

import org.doubao.mall.common.dto.FileUploadResult;
import org.doubao.mall.common.entity.Result;
import org.doubao.user.server.config.FeignErrorDecoderConfig;
import org.doubao.user.server.service.back.OssServiceFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

// 在用户服务中调用OSS服务
@FeignClient(name = "oss-service", path = "/oss", fallback = OssServiceFallback.class, configuration = FeignErrorDecoderConfig.class)
public interface OssServiceClient {

	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	Result<FileUploadResult> uploadFile(
			@RequestPart("file") MultipartFile file
	);

	@GetMapping("/url")
	Result<String> generateAccessUrl(
			@RequestParam(required = false , name = "fileKey")  String fileKey,
			@RequestParam("storageType") String storageType
	);
}

