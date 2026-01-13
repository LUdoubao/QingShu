package org.doubao.oss.service.controller;

import org.doubao.oss.service.service.impl.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/public/oss")
public class FilePublicController {

	private static final Logger LOGGER =  LoggerFactory.getLogger(FilePublicController.class);
	@Resource
	private StorageService storageService;

	@GetMapping("/files")
	public ResponseEntity<org.springframework.core.io.Resource> serveFile(@RequestParam("fileKey") String fileKey) {
		try {
			// 本地存储专用访问接口
			InputStreamResource resource = new InputStreamResource(
					storageService.downloadFile(fileKey)
			);

			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "inline")
					.body(resource);
		} catch (Exception  e) {
			LOGGER.error("文件获取失败", e);
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

	}
}