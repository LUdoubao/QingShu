package org.doubao.oss.service.service.impl;

import org.doubao.oss.service.config.KeyConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

public class PathEncryptionService {
	private static final String ALGORITHM = "AES/CTR/NoPadding";
	private static final int COUNTER_SIZE = 4; // 4字节计数器
	private static final int IV_SIZE = 16; // AES IV大小

	private final KeyConfig keyConfig;
	private final SecureRandom secureRandom = new SecureRandom();

	@Autowired
	public PathEncryptionService(KeyConfig keyConfig) {
		this.keyConfig = keyConfig;
	}

	/**
	 * 加密文件路径为短键
	 * @param path 文件路径
	 * @return 加密后的短键 (格式: keyId:base62EncodedData)
	 */
	public String encryptPath(String path) {
		try {
			// 获取当前密钥ID和密钥
			String keyId = keyConfig.getCurrentKeyId();
			String secretKey = keyConfig.getCurrentKey();

			// 生成随机计数器
			byte[] counter = new byte[COUNTER_SIZE];
			secureRandom.nextBytes(counter);

			// 加密路径
			byte[] encrypted = encryptWithKey(
					path.getBytes(StandardCharsets.UTF_8),
					Base64.getDecoder().decode(secretKey),
					counter
			);

			// 组合计数器+密文
			ByteBuffer buffer = ByteBuffer.allocate(COUNTER_SIZE + encrypted.length);
			buffer.put(counter);
			buffer.put(encrypted);

			// Base62编码
			String encryptedData = Base62.encode(buffer.array());

			// 返回格式: keyId:base62EncodedData
			return keyId + "-" + encryptedData;
		} catch (Exception e) {
			throw new EncryptionException("Path encryption failed", e);
		}
	}

	/**
	 * 解密短键为文件路径
	 * @param shortKey 加密后的短键 (格式: keyId:base62EncodedData)
	 * @return 原始文件路径
	 */
	public String decryptPath(String shortKey) {
		try {
			// 分离密钥ID和加密数据
			String[] parts = shortKey.split("-", 2);
			if (parts.length != 2) {
				throw new InvalidKeyFormatException("Invalid key format");
			}

			String keyId = parts[0];
			String encryptedData = parts[1];

			// 获取对应密钥
			String secretKey = keyConfig.getKey(keyId);
			if (secretKey == null) {
				throw new KeyNotFoundException("Key not found for ID: " + keyId);
			}

			// Base62解码
			byte[] data = Base62.decode(encryptedData);

			// 分离计数器和密文
			byte[] counter = Arrays.copyOfRange(data, 0, COUNTER_SIZE);
			byte[] encrypted = Arrays.copyOfRange(data, COUNTER_SIZE, data.length);

			// 解密路径
			byte[] decrypted = decryptWithKey(
					encrypted,
					Base64.getDecoder().decode(secretKey),
					counter
			);

			return new String(decrypted, StandardCharsets.UTF_8);
		} catch (Exception e) {
			throw new EncryptionException("Path decryption failed", e);
		}
	}

	// 使用指定密钥加密
	private byte[] encryptWithKey(byte[] input, byte[] keyBytes, byte[] counter) throws Exception {
		// 创建完整16字节IV（计数器+填充）
		byte[] iv = new byte[IV_SIZE];
		System.arraycopy(counter, 0, iv, 0, COUNTER_SIZE);

		SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
		Cipher cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv));
		return cipher.doFinal(input);
	}

	// 使用指定密钥解密
	private byte[] decryptWithKey(byte[] encrypted, byte[] keyBytes, byte[] counter) throws Exception {
		// 创建完整16字节IV（计数器+填充）
		byte[] iv = new byte[IV_SIZE];
		System.arraycopy(counter, 0, iv, 0, COUNTER_SIZE);

		SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
		Cipher cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv));
		return cipher.doFinal(encrypted);
	}

	// Base62编码工具类
	public static class Base62 {
		private static final String CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		private static final int BASE = CHARACTERS.length();

		public static String encode(byte[] input) {
			// 将字节数组转换为大整数
			BigInteger number = new BigInteger(1, input);
			StringBuilder sb = new StringBuilder();

			// 转换为Base62
			while (number.compareTo(BigInteger.ZERO) > 0) {
				BigInteger[] divmod = number.divideAndRemainder(BigInteger.valueOf(BASE));
				sb.insert(0, CHARACTERS.charAt(divmod[1].intValue()));
				number = divmod[0];
			}

			// 处理前导零
			for (byte b : input) {
				if (b == 0) sb.insert(0, CHARACTERS.charAt(0));
				else break;
			}

			return sb.toString();
		}

		public static byte[] decode(String base62) {
			BigInteger number = BigInteger.ZERO;

			// 转换为大整数
			for (char c : base62.toCharArray()) {
				number = number.multiply(BigInteger.valueOf(BASE))
						.add(BigInteger.valueOf(CHARACTERS.indexOf(c)));
			}

			// 转换为字节数组
			byte[] bytes = number.toByteArray();

			// 处理前导零
			int leadingZeros = 0;
			for (char c : base62.toCharArray()) {
				if (c == CHARACTERS.charAt(0)) leadingZeros++;
				else break;
			}

			// 移除BigInteger添加的符号位
			if (bytes.length > 0 && bytes[0] == 0) {
				bytes = Arrays.copyOfRange(bytes, 1, bytes.length);
			}

			// 重建原始字节数组
			byte[] result = new byte[leadingZeros + bytes.length];
			System.arraycopy(bytes, 0, result, leadingZeros, bytes.length);
			return result;
		}
	}

	// 自定义异常类
	public static class EncryptionException extends RuntimeException {
		public EncryptionException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	public static class KeyNotFoundException extends RuntimeException {
		public KeyNotFoundException(String message) {
			super(message);
		}
	}

	public static class InvalidKeyFormatException extends RuntimeException {
		public InvalidKeyFormatException(String message) {
			super(message);
		}
	}
}