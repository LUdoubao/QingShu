package org.doubao.quote.service.duplicate.check;

public class MurmurHash {

	private static final long C1 = 0x87c37b91114253d5L;
	private static final long C2 = 0x4cf5ad432745937fL;
	private static final int R1 = 31;
	private static final int R2 = 27;
	private static final int M = 5;
	private static final int N1 = 0x52dce729;
	private static final int N2 = 0x38495ab5;

	public static long hash64(String key) {
		return hash64(key.getBytes());
	}

	public static long hash64(byte[] data) {
		return hash64(data, data.length, 0);
	}

	public static long hash64(byte[] key, int len, int seed) {
		long hash = seed & 0xFFFFFFFFL;
		int i = 0;
		int remaining = len;

		// 处理16字节块
		while (remaining >= 16) {
			long k1 = getLong(key, i);
			i += 8;
			long k2 = getLong(key, i);
			i += 8;

			k1 *= C1;
			k1 = Long.rotateLeft(k1, R1);
			k1 *= C2;
			hash ^= k1;
			hash = Long.rotateLeft(hash, R2);
			hash = hash * M + N1;

			k2 *= C2;
			k2 = Long.rotateLeft(k2, R2);
			k2 *= C1;
			hash ^= k2;
			hash = Long.rotateLeft(hash, R1);
			hash = hash * M + N2;

			remaining -= 16;
		}

		// 处理尾部数据
		long k1 = 0;
		long k2 = 0;

		switch (remaining) {
			case 15:
				k2 ^= ((long) key[i + 14] & 0xFF) << 48;
			case 14:
				k2 ^= ((long) key[i + 13] & 0xFF) << 40;
			case 13:
				k2 ^= ((long) key[i + 12] & 0xFF) << 32;
			case 12:
				k2 ^= ((long) key[i + 11] & 0xFF) << 24;
			case 11:
				k2 ^= ((long) key[i + 10] & 0xFF) << 16;
			case 10:
				k2 ^= ((long) key[i + 9] & 0xFF) << 8;
			case 9:
				k2 ^= ((long) key[i + 8] & 0xFF);
				k2 *= C2;
				k2 = Long.rotateLeft(k2, R2);
				k2 *= C1;
				hash ^= k2;
			case 8:
				k1 ^= ((long) key[i + 7] & 0xFF) << 56;
			case 7:
				k1 ^= ((long) key[i + 6] & 0xFF) << 48;
			case 6:
				k1 ^= ((long) key[i + 5] & 0xFF) << 40;
			case 5:
				k1 ^= ((long) key[i + 4] & 0xFF) << 32;
			case 4:
				k1 ^= ((long) key[i + 3] & 0xFF) << 24;
			case 3:
				k1 ^= ((long) key[i + 2] & 0xFF) << 16;
			case 2:
				k1 ^= ((long) key[i + 1] & 0xFF) << 8;
			case 1:
				k1 ^= ((long) key[i] & 0xFF);
				k1 *= C1;
				k1 = Long.rotateLeft(k1, R1);
				k1 *= C2;
				hash ^= k1;
		}

		// 最终处理
		hash ^= len;
		hash = fmix64(hash);

		return hash;
	}

	private static long fmix64(long k) {
		k ^= k >>> 33;
		k *= 0xff51afd7ed558ccdL;
		k ^= k >>> 33;
		k *= 0xc4ceb9fe1a85ec53L;
		k ^= k >>> 33;
		return k;
	}

	private static long getLong(byte[] b, int offset) {
		return ((b[offset] & 0xFFL) |
				((b[offset + 1] & 0xFFL) << 8) |
				((b[offset + 2] & 0xFFL) << 16) |
				((b[offset + 3] & 0xFFL) << 24) |
				((b[offset + 4] & 0xFFL) << 32) |
				((b[offset + 5] & 0xFFL) << 40) |
				((b[offset + 6] & 0xFFL) << 48) |
				((b[offset + 7] & 0xFFL) << 56));
	}
}