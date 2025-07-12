package org.doubao.user.server.feign;

import org.doubao.mall.common.dto.UploadResult;
import org.doubao.mall.common.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

// 在用户服务中调用OSS服务
@FeignClient(name = "oss-service", path = "/oss")
public interface OssServiceClient {

	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	Result<UploadResult> uploadFile(
			@RequestPart("file") MultipartFile file,
			@RequestParam(value = "prefix", required = false) String prefix,
			@RequestParam(value = "type", defaultValue = "file") String type
	);

	@GetMapping("/delete")
	Result<Void> deleteFile(@RequestParam("path") String filePath);
}

