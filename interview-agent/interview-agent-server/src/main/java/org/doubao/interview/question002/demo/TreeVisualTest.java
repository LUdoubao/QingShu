package org.doubao.interview.question002.demo;

import org.doubao.interview.question002.index.BPlusTree;
import org.doubao.interview.question002.index.RedBlackTree;

/**
 * 树形结构可视化测试（单独运行）
 */
public class TreeVisualTest {
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║     树形结构可视化对比演示              ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        // ========== B+Tree 可视化 ===========
        System.out.println("【1】B+Tree 树形结构");
        System.out.println("──────────────────────────────────────\n");
        BPlusTree bpt = new BPlusTree();
        int[] keys = {50, 25, 75, 10, 30, 60, 90, 5, 15};
        for (int key : keys) {
            bpt.insert(key, "Value-" + key);
        }
        bpt.printTree();
        
        // ========== 红黑树可视化 ===========
        System.out.println("\n【2】红黑树树形结构");
        System.out.println("──────────────────────────────────────\n");
        RedBlackTree rbt = new RedBlackTree();
        for (int key : keys) {
            rbt.insert(key, "Value-" + key);
        }
        rbt.printTree();
        
        // ========== 对比总结 ===========
        System.out.println("\n【3】树高对比");
        System.out.println("──────────────────────────────────────");
        System.out.println("  B+Tree:   " + bpt.getHeight() + " 层");
        System.out.println("  红黑树：   " + rbt.getHeight() + " 层");
        System.out.println("\n结论：B+Tree 树高更低，磁盘 I/O 更少 ✅");
    }
}
