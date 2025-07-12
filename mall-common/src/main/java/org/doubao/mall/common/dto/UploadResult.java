package org.doubao.mall.common.dto;

public class UploadResult {
	private String filePath;
	private String fileUrl;

	public UploadResult() {
	}

	public UploadResult(String filePath, String fileUrl) {
		this.filePath = filePath;
		this.fileUrl = fileUrl;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getFileUrl() {
		return fileUrl;
	}

	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}
}