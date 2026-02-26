package org.doubao.topic.service.feign;

import org.doubao.mall.common.condition.MicroserviceMode;
import org.doubao.mall.common.dto.FileUploadResult;
import org.doubao.mall.common.entity.Result;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "oss-service", path = "/oss")
@MicroserviceMode
public interface OssClient {

	@GetMapping("/url")
	Result<String> generateAccessUrl(
			@RequestParam(required = false , name = "fileKey")  String fileKey,
			@RequestParam("storageType") String storageType
	);
}