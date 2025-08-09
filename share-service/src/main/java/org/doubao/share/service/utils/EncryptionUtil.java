package org.doubao.share.service.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
public class EncryptionUtil {

	// 默认密钥doubao
	@Value("${share.aes.key:doubao}")
	private String aesKey;

	private static final String ALGORITHM = "AES";

	// 加密
	public String encrypt(String data) throws Exception {
		SecretKeySpec keySpec = new SecretKeySpec(aesKey.getBytes(), ALGORITHM);
		Cipher cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, keySpec);
		byte[] encryptedData = cipher.doFinal(data.getBytes());
		return Base64.getEncoder().encodeToString(encryptedData);
	}

	// 解密
	public String decrypt(String encryptedData) throws Exception {
		SecretKeySpec keySpec = new SecretKeySpec(aesKey.getBytes(), ALGORITHM);
		Cipher cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, keySpec);
		byte[] decodedData = Base64.getDecoder().decode(encryptedData);
		byte[] decryptedData = cipher.doFinal(decodedData);
		return new String(decryptedData);
	}
}

