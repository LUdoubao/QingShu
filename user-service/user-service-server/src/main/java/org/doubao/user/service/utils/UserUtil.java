package org.doubao.user.service.utils;

import java.util.Random;

public class UserUtil {
	/**
	 * 生成4个中文字符的文艺风格昵称
	 */
	public static String generateArtisticNickname() {
		Random random = new Random();

		// 随机选择组合模式
		int mode = random.nextInt(3);

		switch (mode) {
			case 0:
				// 2+2组合：两个双字词组合
				return twoCharWords[random.nextInt(twoCharWords.length)] +
						twoCharWords[random.nextInt(twoCharWords.length)];
			case 1:
				// 1+3组合：单字词 + 三字�?
				return singleCharWords[random.nextInt(singleCharWords.length)] +
						threeCharWords[random.nextInt(threeCharWords.length)];
			case 2:
				// 3+1组合：三字词 + 单字�?
				return threeCharWords[random.nextInt(threeCharWords.length)] +
						singleCharWords[random.nextInt(singleCharWords.length)];
			default:
				return twoCharWords[0] + twoCharWords[0];
		}
	}

	// 单字词库（自然意象为主）
	private static final String[] singleCharWords = {

	};

	// 双字词库（偏文艺意境�?
	private static final String[] twoCharWords = {
			"听风", "望云", "观雨", "踏雪", "寻花", "问柳", "伴月", "随星",
			"枕石", "漱流", "听雨", "煮茶", "焚琴", "煮鹤", "弄影", "含章",
			"清欢", "浅喜", "淡愁", "微醺", "静好", "安然", "无恙", "长安",
			"若梦", "如幻", "似真", "若初", "如初", "如故", "如斯", "如云",
			"疏影", "暗香", "冷月", "残阳", "断桥", "古道", "长亭", "短亭",
			"浅唱", "低吟", "轻舞", "漫步", "凝眸", "浅笑", "嫣然", "默然",
			"知意", "解语", "倾城", "倾国", "无双", "绝世", "独立", "孤芳",
			"疏狂", "淡泊", "宁静", "致远", "逍遥", "自在", "无拘", "无束"
	};

	// 三字词库（古典韵味）
	private static final String[] threeCharWords = {

	};
}
