package org.doubao.mall.common.util;

import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class EncryptionUtil {

	private static final String ALGORITHM = "AES";

	// 加密
	public static String encrypt(String data, String aesKey) throws Exception {
		SecretKeySpec keySpec = new SecretKeySpec(aesKey.getBytes(), ALGORITHM);
		Cipher cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, keySpec);
		byte[] encryptedData = cipher.doFinal(data.getBytes());
		return Base64.getEncoder().encodeToString(encryptedData);
	}

	// 解密
	public static String decrypt(String encryptedData, String aesKey) throws Exception {
		SecretKeySpec keySpec = new SecretKeySpec(aesKey.getBytes(), ALGORITHM);
		Cipher cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, keySpec);
		byte[] decodedData = Base64.getDecoder().decode(encryptedData);
		byte[] decryptedData = cipher.doFinal(decodedData);
		return new String(decryptedData);
	}
}

