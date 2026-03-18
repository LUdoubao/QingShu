package org.doubao.oss.service.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * 图片压缩工具类
 */
public class ImageCompressor {
	private static final Logger LOGGER = LoggerFactory.getLogger(ImageCompressor.class);

	/**
	 * 压缩图片（超过阈值强制转JPEG）
	 *
	 * @param inputStream 原始图片流
	 * @param quality     JPEG压缩质量 (0.0 - 1.0，建议 0.7-0.9)
	 * @param maxSizeBytes 大小阈值，超过则强制转JPEG
	 * @return 压缩后的图片字节数组，如果压缩无效则返回原图
	 */
	public static byte[] compress(InputStream inputStream, float quality, long maxSizeBytes)
			throws IOException {

		// 先读取原图字节，用于后续比较
		byte[] originalBytes = toByteArray(inputStream);
		long originalSize = originalBytes.length;

		// 读取原始图片
		BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(originalBytes));
		if (originalImage == null) {
			throw new IOException("无法读取图片");
		}

		// 获取原始图片属性
		int width = originalImage.getWidth();
		int height = originalImage.getHeight();
		boolean hasAlpha = originalImage.getColorModel().hasAlpha();

		// 决定输出格式：超过阈值强制JPEG，否则根据是否有透明决定
		String outputFormat;
		boolean forceJpeg = originalSize > maxSizeBytes;

		if (forceJpeg) {
			outputFormat = "jpeg";  // 超过阈值，强制JPEG（丢弃透明通道）
			LOGGER.info("图片大小 {} bytes 超过阈值 {}，强制转为JPEG",
					originalSize, maxSizeBytes);
		} else if (hasAlpha) {
			outputFormat = "png";   // 未超阈值且有透明，保留PNG
		} else {
			outputFormat = "jpeg";  // 未超阈值无透明，也用JPEG（更优压缩）
		}

		// 创建输出图像
		BufferedImage outputImage;
		if ("jpeg".equals(outputFormat)) {
			// JPEG 不支持透明，创建 RGB 图像（透明变白色背景）
			outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

			// 白色背景填充（避免透明变黑色）
			Graphics2D bgG2d = outputImage.createGraphics();
			bgG2d.setColor(Color.WHITE);
			bgG2d.fillRect(0, 0, width, height);
			bgG2d.dispose();
		} else {
			// PNG 保留透明
			outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		}

		// 高质量绘制原图
		Graphics2D g2d = outputImage.createGraphics();
		setupHighQualityRendering(g2d);
		g2d.drawImage(originalImage, 0, 0, width, height, null);
		g2d.dispose();

		// 编码输出
		byte[] compressedBytes = encodeImage(outputImage, outputFormat, quality);

		// 保险机制：如果压缩后更大，返回原图
		if (compressedBytes.length >= originalSize) {
			LOGGER.warn("压缩无效，保留原图: {} -> {}", originalSize, compressedBytes.length);
			return originalBytes;
		}
		LOGGER.info("压缩成功: {} -> {} ({}%)",
				originalSize,
				compressedBytes.length,
				Math.round((1 - (double)compressedBytes.length / originalSize) * 100));

		return compressedBytes;
	}

	/**
	 * 智能压缩：超过指定大小才压缩，且确保压缩有效
	 */
	public static byte[] compressIfNeeded(InputStream inputStream, long maxSizeBytes,
										  float quality) throws IOException {
		byte[] originalBytes = toByteArray(inputStream);

		if (originalBytes.length <= maxSizeBytes) {
			return originalBytes; // 无需压缩
		}

		byte[] compressed = compress(new ByteArrayInputStream(originalBytes), quality, maxSizeBytes);

		// 压缩后必须比原图小至少10%，否则用原图
		if (compressed.length < originalBytes.length * 0.9) {
			return compressed;
		}
		return originalBytes;
	}

	/**
	 * 创建输出图像
	 */
	private static BufferedImage createOutputImage(BufferedImage source,
												   int width, int height,
												   boolean hasAlpha) {
		int imageType = source.getType();

		if (hasAlpha || imageType == BufferedImage.TYPE_CUSTOM || imageType == 0) {
			return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		} else {
			// 使用与原图相同的类型，保持属性一致
			return new BufferedImage(width, height, imageType);
		}
	}

	/**
	 * 设置高质量渲染参数
	 */
	private static void setupHighQualityRendering(Graphics2D g2d) {
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
				RenderingHints.VALUE_COLOR_RENDER_QUALITY);
	}

	/**
	 * 编码图像为字节数组
	 */
	private static byte[] encodeImage(BufferedImage image, String format,
									  float quality) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		if ("jpeg".equals(format) || "jpg".equals(format)) {
			// JPEG有损压缩
			Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
			if (!writers.hasNext()) {
				throw new IOException("No JPEG writer available");
			}

			ImageWriter writer = writers.next();
			try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
				writer.setOutput(ios);

				JPEGImageWriteParam param = new JPEGImageWriteParam(null);
				param.setCompressionMode(JPEGImageWriteParam.MODE_EXPLICIT);
				param.setCompressionQuality(quality);

				writer.write(null, new IIOImage(image, null, null), param);
			} finally {
				writer.dispose();
			}
		} else {
			// PNG无损压缩（可能无效）
			ImageIO.write(image, "png", baos);
		}

		return baos.toByteArray();
	}

	/**
	 * 判断是否为照片（色彩丰富）
	 */
	private static boolean isPhotographic(BufferedImage img) {
		// 采样判断色彩丰富度
		int sampleSize = Math.max(1, (img.getWidth() * img.getHeight()) / 1000);
		Set<Integer> colorSamples = new HashSet<>();

		for (int x = 0; x < img.getWidth(); x += Math.max(1, img.getWidth() / 32)) {
			for (int y = 0; y < img.getHeight(); y += Math.max(1, img.getHeight() / 32)) {
				colorSamples.add(img.getRGB(x, y) & 0xFFF00000); // 粗略采样
				if (colorSamples.size() > 50) return true; // 色彩丰富认为是照片
			}
		}

		return false;
	}

	/**
	 * InputStream 转 byte[]（Java 8 兼容）
	 */
	public static byte[] toByteArray(InputStream input) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[8192];
		int bytesRead;
		while ((bytesRead = input.read(buffer)) != -1) {
			baos.write(buffer, 0, bytesRead);
		}
		return baos.toByteArray();
	}
}