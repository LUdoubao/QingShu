package org.doubao.mall.common.dto;

import org.springframework.web.multipart.MultipartFile;

public class FileUploadDto {
	private MultipartFile file;
	private String prefix;
	private Integer maxWidth = 1024;
	private Float quality = 0.8f;

	public FileUploadDto() {
	}

	public FileUploadDto(MultipartFile file, String prefix, Float quality, Integer maxWidth) {
		this.file = file;
		this.prefix = prefix;
		this.quality = quality;
		this.maxWidth = maxWidth;
	}

	public MultipartFile getFile() {
		return file;
	}

	public void setFile(MultipartFile file) {
		this.file = file;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public Integer getMaxWidth() {
		return maxWidth;
	}

	public void setMaxWidth(Integer maxWidth) {
		this.maxWidth = maxWidth;
	}

	public Float getQuality() {
		return quality;
	}

	public void setQuality(Float quality) {
		this.quality = quality;
	}
}