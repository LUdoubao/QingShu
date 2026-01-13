package org.doubao.quote.service.duplicate.check;

import java.util.HashSet;
import java.util.Set;

public class SimilarityCalculator {

	// 计算SimHash相似度（汉明距离）
	public static double simHashSimilarity(long[] hash1, long[] hash2) {
		if (hash1.length != 8 || hash2.length != 8) {
			throw new IllegalArgumentException("Invalid hash length");
		}

		int totalBits = 512;
		int distance = 0;

		// 计算总汉明距离
		for (int i = 0; i < 8; i++) {
			distance += Long.bitCount(hash1[i] ^ hash2[i]);
		}

		// 使用指数衰减公式
		double normalizedDistance = distance / (double) totalBits;
		return Math.exp(-15.0 * normalizedDistance);
	}
	// public static double simHashSimilarity(long hash1, long hash2) {
	// 	long xor = hash1 ^ hash2;
	// 	int distance = Long.bitCount(xor);
	// 	return 1.0 - (distance / 64.0);
	// }

	// 计算Jaccard相似度（基于n-gram）
	public static double jaccardSimilarity(Set<String> set1, Set<String> set2) {
		if (set1.isEmpty() && set2.isEmpty()) return 1.0;

		Set<String> intersection = new HashSet<>(set1);
		intersection.retainAll(set2);

		Set<String> union = new HashSet<>(set1);
		union.addAll(set2);

		return (double) intersection.size() / union.size();
	}

	// 计算编辑距离相似度
	public static double editDistanceSimilarity(String s1, String s2) {
		if (s1 == null || s2 == null) return 0.0;

		int maxLength = Math.max(s1.length(), s2.length());
		if (maxLength == 0) return 1.0;

		int distance = calculateLevenshteinDistance(s1, s2);
		return 1.0 - ((double) distance / maxLength);
	}

	private static int calculateLevenshteinDistance(String s, String t) {
		int m = s.length();
		int n = t.length();
		int[][] dp = new int[m + 1][n + 1];

		for (int i = 0; i <= m; i++) dp[i][0] = i;
		for (int j = 0; j <= n; j++) dp[0][j] = j;

		for (int i = 1; i <= m; i++) {
			for (int j = 1; j <= n; j++) {
				int cost = (s.charAt(i - 1) == t.charAt(j - 1)) ? 0 : 1;
				dp[i][j] = Math.min(Math.min(
								dp[i - 1][j] + 1,
								dp[i][j - 1] + 1),
						dp[i - 1][j - 1] + cost);
			}
		}

		return dp[m][n];
	}
}

