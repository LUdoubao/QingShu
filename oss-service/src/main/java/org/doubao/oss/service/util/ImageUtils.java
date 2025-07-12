package org.doubao.oss.service.util;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class ImageUtils {

	public static void compressImage(BufferedImage image, String format, ByteArrayOutputStream outputStream,
									 Integer maxWidth, float quality) throws IOException {
		// 调整大小
		if (maxWidth != null && image.getWidth() > maxWidth) {
			double ratio = (double) maxWidth / image.getWidth();
			int height = (int) (image.getHeight() * ratio);
			image = resizeImage(image, maxWidth, height);
		}

		// 压缩质量
		compressImageQuality(image, format, outputStream, quality);
	}

	public static void compressAndSaveImage(BufferedImage image, String format, String outputPath,
											Integer maxWidth, float quality) throws IOException {
		// 调整大小
		if (maxWidth != null && image.getWidth() > maxWidth) {
			double ratio = (double) maxWidth / image.getWidth();
			int height = (int) (image.getHeight() * ratio);
			image = resizeImage(image, maxWidth, height);
		}

		// 保存图片
		File outputFile = new File(outputPath);
		ImageIO.write(image, format, outputFile);
	}

	private static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
		Image resultingImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
		BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
		outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);
		return outputImage;
	}

	private static void compressImageQuality(BufferedImage image, String format,
											 ByteArrayOutputStream outputStream, float quality) throws IOException {
		Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(format);
		if (!writers.hasNext()) {
			throw new IllegalStateException("No writers found for format: " + format);
		}

		ImageWriter writer = writers.next();
		ImageWriteParam param = writer.getDefaultWriteParam();

		// 设置压缩模式
		param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		param.setCompressionQuality(quality); // 0.0-1.0

		try (ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream)) {
			writer.setOutput(ios);
			writer.write(null, new IIOImage(image, null, null), param);
		} finally {
			writer.dispose();
		}
	}
}