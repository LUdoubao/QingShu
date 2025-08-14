package org.doubao.quote.service.duplicate.check;

import java.util.*;

public class TextUtils {

	// 文本标准化处理
	public static String normalizeText(String text) {
		if (text == null || text.isEmpty()) {
			return "";
		}

		// 保留中文、英文、数字和常用标点
		String normalized = text.replaceAll("[^\\p{L}\\p{N}\\p{P}\\s]", "");

		// 转换全角字符为半角
		normalized = normalizeFullWidthChars(normalized);

		// 标准化标点符号
		normalized = normalized.replaceAll("\\p{P}+", " ");

		// 合并连续空格
		normalized = normalized.replaceAll("\\s+", " ").trim();

		return normalized;
	}

	private static String normalizeFullWidthChars(String input) {
		StringBuilder sb = new StringBuilder();
		for (char c : input.toCharArray()) {
			// 全角数字 (0xFF10-0xFF19) -> 半角数字 (0x30-0x39)
			if (c >= 0xFF10 && c <= 0xFF19) {
				sb.append((char)(c - 0xFF10 + '0'));
			}
			// 全角大写字母 (0xFF21-0xFF3A) -> 半角小写字母
			else if (c >= 0xFF21 && c <= 0xFF3A) {
				sb.append(Character.toLowerCase((char)(c - 0xFF21 + 'A')));
			}
			// 全角小写字母 (0xFF41-0xFF5A) -> 半角小写字母
			else if (c >= 0xFF41 && c <= 0xFF5A) {
				sb.append((char)(c - 0xFF41 + 'a'));
			}
			// 全角空格 -> 半角空格
			else if (c == 0x3000) {
				sb.append(' ');
			}
			// 其他字符保留
			else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	// 作者名称标准化
	public static String normalizeAuthor(String author) {
		if (author == null) return "";

		// 移除称谓和多余空格
		return author.replaceAll("(dr\\.?|prof\\.?|mr\\.?|mrs\\.?|ms\\.?)\\s*", "")
				.replaceAll("\\s+", " ")
				.trim();
	}
	public static long[] computeSimHash(String text) {
		// 使用512位向量（8个long表示）
		int vectorSize = 512;
		int[] vector = new int[vectorSize];
		Arrays.fill(vector, 0);

		// 按字符处理
		char[] chars = text.toCharArray();
		for (char c : chars) {
			// 使用加密级哈希函数
			long[] charHashes = secureCharHashes(c);

			// 更新向量（512位）
			for (int i = 0; i < vectorSize; i++) {
				int block = i / 64;  // 计算属于哪个64位块
				int bit = i % 64;    // 计算在块中的位位置

				// 确保block在charHashes范围内
				if (block < charHashes.length) {
					long bitmask = 1L << bit;

					if ((charHashes[block] & bitmask) != 0) {
						vector[i] += 1;
					} else {
						vector[i] -= 1;
					}
				}
			}
		}

		// 生成512位指纹（用8个long存储）
		long[] fingerprint = new long[8];
		for (int i = 0; i < vectorSize; i++) {
			if (vector[i] > 0) {
				int block = i / 64;
				int bit = i % 64;
				fingerprint[block] |= (1L << bit);
			}
		}

		return fingerprint;
	}

	// 生成8个独立哈希值（512位）
	private static long[] secureCharHashes(char c) {
		String str = String.valueOf(c);
		return new long[] {
				MurmurHash.hash64(str + "SALT1"),
				MurmurHash.hash64(str + "SALT2"),
				MurmurHash.hash64(str + "SALT3"),
				MurmurHash.hash64(str + "SALT4"),
				MurmurHash.hash64(str + "SALT5"),
				MurmurHash.hash64(str + "SALT6"),
				MurmurHash.hash64(str + "SALT7"),
				MurmurHash.hash64(str + "SALT8")
		};
	}

	// public static long computeSimHash(String text) {
	// 	int[] vector = new int[64];
	// 	Arrays.fill(vector, 0);
	//
	// 	String[] words = text.split("\\s+");
	// 	Map<String, Integer> wordFreq = new HashMap<>();
	//
	// 	// 计算词频
	// 	for (String word : words) {
	// 		wordFreq.put(word, wordFreq.getOrDefault(word, 0) + 1);
	// 	}
	//
	// 	// 计算SimHash
	// 	for (Map.Entry<String, Integer> entry : wordFreq.entrySet()) {
	// 		long wordHash = MurmurHash.hash64(entry.getKey());
	// 		for (int i = 0; i < 64; i++) {
	// 			long bitmask = 1L << i;
	// 			if ((wordHash & bitmask) != 0) {
	// 				vector[i] += entry.getValue();
	// 			} else {
	// 				vector[i] -= entry.getValue();
	// 			}
	// 		}
	// 	}
	//
	// 	// 生成指纹
	// 	long fingerprint = 0;
	// 	for (int i = 0; i < 64; i++) {
	// 		if (vector[i] > 0) {
	// 			fingerprint |= (1L << i);
	// 		}
	// 	}
	//
	// 	return fingerprint;
	// }

	// 提取n-gram特征
	public static Set<String> extractNGrams(String text, int n) {
		Set<String> ngrams = new HashSet<>();
		String[] words = text.split("\\s+");

		for (int i = 0; i <= words.length - n; i++) {
			StringBuilder sb = new StringBuilder();
			for (int j = 0; j < n; j++) {
				if (j > 0) sb.append(" ");
				sb.append(words[i + j]);
			}
			ngrams.add(sb.toString());
		}

		return ngrams;
	}
}
