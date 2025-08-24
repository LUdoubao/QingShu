package org.doubao.view.count.service;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class DirectoryTreeGenerator {

	public static void main(String[] args) throws IOException {
		// 指定要扫描的根目录路径
		Path rootPath = Paths.get("D:\\workspace\\doubao\\SpringCloudDemo\\user-service");

		// 生成目录结构树
		String tree = generateDirectoryTree(rootPath);
		System.out.println(tree);
	}

	public static String generateDirectoryTree(Path rootPath) throws IOException {
		// 验证路径是否存在且是目录
		if (!Files.exists(rootPath) || !Files.isDirectory(rootPath)) {
			throw new IllegalArgumentException("路径不存在或不是目录: " + rootPath);
		}

		// 构建树形节点
		TreeNode rootNode = new TreeNode(rootPath.getFileName().toString());
		buildTree(rootPath, rootNode, 0);

		// 生成树形文本
		StringBuilder sb = new StringBuilder();
		generateTreeText(rootNode, "", true, sb);
		return sb.toString();
	}

	private static void buildTree(Path path, TreeNode parentNode, int depth) throws IOException {
		// 限制递归深度防止无限循环（可选）
		if (depth > 10) return;

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
			for (Path entry : stream) {
				// 跳过隐藏文件（可选）
				if (entry.getFileName().toString().startsWith(".")) continue;

				TreeNode childNode = new TreeNode(entry.getFileName().toString());
				parentNode.addChild(childNode);

				// 如果是目录则递归处理
				if (Files.isDirectory(entry)) {
					buildTree(entry, childNode, depth + 1);
				}
			}
		}
	}

	private static void generateTreeText(TreeNode node, String prefix, boolean isLast, StringBuilder sb) {
		// 添加当前节点
		sb.append(prefix)
				.append(isLast ? "└── " : "├── ")
				.append(node.name)
				.append("\n");

		// 准备子节点的前缀
		String newPrefix = prefix + (isLast ? "    " : "│   ");

		// 递归处理子节点
		for (int i = 0; i < node.children.size(); i++) {
			boolean lastChild = (i == node.children.size() - 1);
			generateTreeText(node.children.get(i), newPrefix, lastChild, sb);
		}
	}

	static class TreeNode {
		String name;
		List<TreeNode> children = new ArrayList<>();

		TreeNode(String name) {
			this.name = name;
		}

		void addChild(TreeNode child) {
			children.add(child);
		}
	}
}
