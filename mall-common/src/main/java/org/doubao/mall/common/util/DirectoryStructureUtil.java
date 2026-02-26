package org.doubao.mall.common.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * 文件夹目录结构获取工具类
 * 功能：完整遍历指定文件夹的所有层级，以美观的树形结构输出（忽略.git、.idea、target文件夹）
 */
public class DirectoryStructureUtil {
	// 目录树展示的符号常量
	private static final String PREFIX = "│   ";       // 非最后一级的前缀
	private static final String LAST_PREFIX = "    ";  // 最后一级的前缀
	private static final String BRANCH = "├── ";       // 非最后一个节点的分支
	private static final String LAST_BRANCH = "└── ";  // 最后一个节点的分支

	// 定义需要忽略的文件夹名称集合（重点新增）
	private static final Set<String> IGNORED_DIR_NAMES = new HashSet<>();
	static {
		// 添加需要忽略的文件夹名称
		IGNORED_DIR_NAMES.add(".git");
		IGNORED_DIR_NAMES.add(".idea");
		IGNORED_DIR_NAMES.add("target");
		IGNORED_DIR_NAMES.add("files");
	}

	/**
	 * 对外暴露的核心方法：获取并打印指定目录的完整结构（默认不显示隐藏文件）
	 * @param rootDirPath 根目录路径（绝对路径/相对路径）
	 */
	public static void printDirectoryStructure(String rootDirPath) {
		printDirectoryStructure(rootDirPath, false);
	}

	/**
	 * 对外暴露的核心方法：获取并打印指定目录的完整结构（可配置是否显示隐藏文件）
	 * @param rootDirPath 根目录路径（绝对路径/相对路径）
	 * @param showHiddenFile 是否显示隐藏文件/文件夹
	 */
	public static void printDirectoryStructure(String rootDirPath, boolean showHiddenFile) {
		File rootDir = new File(rootDirPath);
		// 校验根目录是否存在且是文件夹
		if (!rootDir.exists()) {
			System.err.println("错误：指定的目录不存在 -> " + rootDirPath);
			return;
		}
		if (!rootDir.isDirectory()) {
			System.err.println("错误：指定的路径不是文件夹 -> " + rootDirPath);
			return;
		}

		System.out.println("📂 " + rootDir.getAbsolutePath()); // 打印根目录
		try {
			// 递归遍历子节点（根目录无上级前缀，初始层级为0）
			traverseDirectory(rootDir, "", true, showHiddenFile);
		} catch (Exception e) {
			System.err.println("遍历目录时发生异常：" + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * 递归遍历目录的核心方法
	 * @param currentDir 当前遍历的目录
	 * @param prefix 层级前缀（用于控制缩进和符号）
	 * @param isLast 是否是上级目录的最后一个子节点
	 * @param showHiddenFile 是否显示隐藏文件
	 */
	private static void traverseDirectory(File currentDir, String prefix, boolean isLast, boolean showHiddenFile) {
		// 获取当前目录下的所有文件/子文件夹（过滤隐藏文件 + 忽略指定文件夹）
		File[] files = currentDir.listFiles(file -> {
			// 第一步：过滤需要忽略的文件夹（重点修改）
			if (file.isDirectory() && IGNORED_DIR_NAMES.contains(file.getName())) {
				return false; // 直接排除指定的忽略文件夹
			}

			// 第二步：原有隐藏文件过滤逻辑
			if (!showHiddenFile) {
				try {
					// 跨平台判断隐藏文件（兼容Windows/Linux/Mac）
					Path path = file.toPath();
					return !Files.isHidden(path);
				} catch (IOException e) {
					System.err.println("无法判断文件是否隐藏，跳过：" + file.getAbsolutePath() + " -> " + e.getMessage());
					return false;
				}
			}
			return true;
		});

		// 处理空目录或权限不足的情况
		if (files == null) {
			System.err.println("警告：无权限访问目录 -> " + currentDir.getAbsolutePath());
			return;
		}

		// 遍历所有子节点
		int fileCount = files.length;
		for (int i = 0; i < fileCount; i++) {
			File file = files[i];
			boolean isLastFile = (i == fileCount - 1); // 是否是当前目录的最后一个子节点

			// 拼接当前节点的展示前缀
			String branch = isLastFile ? LAST_BRANCH : BRANCH;
			System.out.println(prefix + branch + (file.isDirectory() ? "📂 " : "📄 ") + file.getName());

			// 如果是文件夹，递归遍历其子节点
			if (file.isDirectory()) {
				// 计算子节点的前缀：如果当前是最后一个节点，用LAST_PREFIX，否则用PREFIX
				String newPrefix = prefix + (isLastFile ? LAST_PREFIX : PREFIX);
				traverseDirectory(file, newPrefix, isLastFile, showHiddenFile);
			}
		}
	}

	// 测试主方法
	public static void main(String[] args) {
		// 示例：遍历指定目录，显示隐藏文件（但会忽略.git/.idea/target）
		String targetDir = "D:\\workspace\\doubao\\QingShu";
		System.out.println("===== 目录结构开始 =====");
		DirectoryStructureUtil.printDirectoryStructure(targetDir, true);
		System.out.println("===== 目录结构结束 =====");
	}
}