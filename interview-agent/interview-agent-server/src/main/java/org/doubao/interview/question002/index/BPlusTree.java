package org.doubao.interview.question002.index;

/**
 * B+Tree 简化实现（用于 InnoDB 索引原理演示）
 * 
 * 【核心特点】
 * 1. 非叶子节点只存 key 和指针，不存数据 - 提高单页存储的分支数
 * 2. 所有数据都在叶子节点 - 查询路径一致，性能稳定
 * 3. 叶子节点之间通过指针连接（链式遍历）- 范围查询高效
 * 4. 树的高度低 - 减少磁盘 I/O 次数
 * 
 * 【与红黑树对比】
 * - 红黑树：二叉树，每个节点存数据，树高 O(log n)，磁盘 I/O 多
 * - B+Tree：多叉树，非叶子节点只存索引，树高更低（通常 3-4 层）
 * 
 * 【与 Hash 对比】
 * - Hash：O(1) 查询，但只支持等值查，不支持范围查
 * - B+Tree：O(log n) 查询，但支持范围查和有序遍历
 * 
 * 【InnoDB 选择 B+Tree 的原因】
 * 1. 磁盘友好：树高低，一次查询只需 3-4 次磁盘 I/O
 * 2. 范围查询：叶子节点链表结构，范围扫描效率极高
 * 3. 排序优化：叶子节点天然有序，ORDER BY 无需额外排序
 * 4. 性能稳定：所有查询都要到叶子节点，不存在最坏情况
 * 
 * @author Interview Demo
 */
public class BPlusTree {
    
    /**
     * B+Tree 节点
     * 
     * 【节点结构设计】
     * - keys: 有序的 key 列表，用于索引定位
     * - children: 子节点指针，数量 = keys.size() + 1
     * - isLeaf: 标记是否是叶子节点
     * - next: 下一个叶子节点（仅叶子节点使用），形成链表
     * - parent: 父节点引用，用于向上查找
     * 
     * 【非叶子节点 vs 叶子节点】
     * - 非叶子节点：keys[i] < key < keys[i+1] 的数据在 children[i+1] 中
     * - 叶子节点：存储实际数据（本演示简化为只存 key）
     */
    static class Node {
        /** 节点中的 key 列表（有序） */
        java.util.List<Integer> keys = new java.util.ArrayList<>();
        
        /** 子节点指针列表 */
        java.util.List<Node> children = new java.util.ArrayList<>();
        
        /** 是否是叶子节点 */
        boolean isLeaf;
        
        /** 下一个叶子节点（仅叶子节点使用） */
        Node next;
        
        /** 父节点 */
        Node parent;
        
        public Node(boolean isLeaf) {
            this.isLeaf = isLeaf;
        }
        
        /**
         * 获取 key 在节点中的位置
         * @return 返回 index，如果找到返回正数，否则返回插入位置（负数）
         */
        public int findKeyIndex(int key) {
            for (int i = 0; i < keys.size(); i++) {
                if (keys.get(i) == key) {
                    return i;
                } else if (keys.get(i) > key) {
                    return -(i + 1);  // 返回插入位置
                }
            }
            return -(keys.size() + 1);  // 应该插入到最后
        }
    }
    
    /** 根节点 */
    private Node root;
    
    /** 每个节点的最大 key 数（阶数 -1） */
    private static final int ORDER = 4;  // 4 阶 B+Tree
    
    /** 第一个叶子节点（用于范围遍历） */
    private Node firstLeaf;
    
    /**
     * 插入 key-value 对
     * 
     * 【插入流程】
     * 1. 空树：创建根节点（也是叶子节点）
     * 2. 非空树：找到对应的叶子节点
     * 3. 在叶子节点中插入 key
     * 4. 如果节点溢出（超过 ORDER-1），执行分裂操作
     * 5. 递归向上分裂，直到根节点
     * 
     * 【时间复杂度】O(log_N n)，其中 N 为树的阶数
     * 【空间复杂度】O(n)
     * 
     * @param key 要插入的键
     * @param value 要插入的值（本演示简化，实际应存储数据）
     */
    public void insert(int key, String value) {
        // 【调试日志】记录插入操作
        System.out.println("[B+Tree] 插入 key=" + key + ", value=" + value);
        
        if (root == null) {
            // 【特殊情况】空树，创建根节点
            root = new Node(true);
            root.keys.add(key);
            firstLeaf = root;
            System.out.println("  -> 创建根节点（叶子节点）");
            return;
        }
        
        // 【核心步骤 1】查找应该插入的叶子节点
        Node leaf = findLeaf(key);
        
        // 【核心步骤 2】在叶子节点中插入 key
        insertIntoNode(leaf, key, value);
        
        // 【调试日志】输出当前树高
        System.out.println("  -> 插入完成，当前树高：" + getHeight());
    }
    
    /**
     * 查找 key 所在的叶子节点
     * 
     * 【查找算法】
     * 1. 从根节点开始，向下遍历
     * 2. 在非叶子节点中，根据 key 的大小选择子节点
     * 3. 一直走到叶子节点为止
     * 
     * 【为什么只查叶子节点？】
     * - B+Tree 的数据都存储在叶子节点
     * - 非叶子节点只起索引作用
     * 
     * @param key 要查找的键
     * @return 对应的叶子节点
     */
    private Node findLeaf(int key) {
        Node current = root;
        // 【循环不变量】current 始终指向当前层的某个节点
        while (!current.isLeaf) {
            int idx = current.findKeyIndex(key);
            if (idx >= 0) {
                // 【找到相等的 key】进入该 key 对应的子树
                current = current.children.get(idx);
            } else {
                // 【未找到】返回插入位置，进入对应的子树
                int insertPos = -(idx + 1);
                current = current.children.get(insertPos);
            }
        }
        return current;  // 【返回值】一定是叶子节点
    }
    
    /**
     * 在节点中插入 key
     */
    private void insertIntoNode(Node node, int key, String value) {
        int idx = node.findKeyIndex(key);
        
        if (idx >= 0) {
            // key 已存在，更新（简化处理，实际应存储 value）
            System.out.println("  [更新] key=" + key + " 已存在");
            return;
        }
        
        // 插入位置
        int insertPos = -(idx + 1);
        node.keys.add(insertPos, key);
        
        System.out.println("  [插入] 节点 keys: " + node.keys);
        
        // 检查是否需要分裂
        if (node.keys.size() > ORDER - 1) {
            splitNode(node);
        }
    }
    
    /**
     * 分裂节点
     */
    private void splitNode(Node node) {
        System.out.println("  [分裂] 节点需要分裂，keys: " + node.keys);
        
        int mid = node.keys.size() / 2;
        int midKey = node.keys.get(mid);
        
        // 创建新节点
        Node newNode = new Node(node.isLeaf);
        
        // 移动一半的 key 到新节点
        while (node.keys.size() > mid) {
            int lastIdx = node.keys.size() - 1;
            int key = node.keys.remove(lastIdx);
            newNode.keys.add(0, key);
        }
        
        // 如果是叶子节点，处理 next 指针
        if (node.isLeaf) {
            newNode.next = node.next;
            node.next = newNode;
            
            // 更新 firstLeaf
            if (node == firstLeaf || node.parent != null && node == node.parent.children.get(0)) {
                // 找到最左边的叶子节点
                updateFirstLeaf();
            }
        }
        
        // 如果不是叶子节点，移动子节点
        if (!node.isLeaf) {
            while (node.children.size() > mid + 1) {
                int lastIdx = node.children.size() - 1;
                Node child = node.children.remove(lastIdx);
                child.parent = newNode;
                newNode.children.add(0, child);
            }
        }
        
        System.out.println("    左节点 keys: " + node.keys);
        System.out.println("    右节点 keys: " + newNode.keys);
        System.out.println("    上移 key: " + midKey);
        
        // 将中间 key 提升到父节点
        if (node.parent == null) {
            // 没有父节点，创建新的根节点
            Node newRoot = new Node(false);
            newRoot.keys.add(midKey);
            newRoot.children.add(node);
            newRoot.children.add(newNode);
            node.parent = newRoot;
            newNode.parent = newRoot;
            root = newRoot;
            System.out.println("    创建新根节点");
            return;  // 【重要】直接返回，避免后续操作
        }
        
        // 有父节点的情况
        Node oldParent = node.parent;
        
        // 找到当前节点在父节点 children 中的索引位置
        int childIndex = -1;
        for (int i = 0; i < oldParent.children.size(); i++) {
            if (oldParent.children.get(i) == node) {
                childIndex = i;
                break;
            }
        }
        
        // 将中间 key 插入到父节点（可能导致父节点分裂）
        insertIntoNode(oldParent, midKey, null);
        
        // 确定当前的父节点（可能已经变化）
        Node currentParent = node.parent != null ? node.parent : root;
        
        // 根据 midKey 在父节点中找到插入位置
        int insertPos = currentParent.findKeyIndex(midKey);
        if (insertPos < 0) {
            insertPos = -(insertPos + 1);
        } else {
            insertPos = insertPos + 1;
        }
        
        // 在正确位置插入新节点
        currentParent.children.add(insertPos, newNode);
        newNode.parent = currentParent;
        
        System.out.println("    插入到父节点，key=" + midKey + ", 位置=" + insertPos);
    }
    
    /**
     * 更新第一个叶子节点引用
     */
    private void updateFirstLeaf() {
        Node current = root;
        while (!current.isLeaf) {
            current = current.children.get(0);
        }
        firstLeaf = current;
    }
    
    /**
     * 查询 key 对应的值
     */
    public String get(int key) {
        if (root == null) {
            return null;
        }
        
        Node leaf = findLeaf(key);
        int idx = leaf.findKeyIndex(key);
        
        if (idx >= 0) {
            System.out.println("[查询] key=" + key + " -> 找到（深度=" + getHeight() + "）");
            return "Value-" + key;
        } else {
            System.out.println("[查询] key=" + key + " -> 未找到");
            return null;
        }
    }
    
    /**
     * 范围查询 [startKey, endKey]
     * 
     * 【核心优势】B+Tree 最强大的功能
     * - 利用叶子节点的链表结构，实现高效的范围扫描
     * - 时间复杂度：O(log_N n + k)，其中 k 是结果集大小
     * - 对比：Hash 索引需要全表扫描 O(n)
     * 
     * 【应用场景】
     * 1. SELECT * FROM users WHERE age BETWEEN 20 AND 30
     * 2. SELECT * FROM orders WHERE create_time >= '2024-01-01'
     * 3. ORDER BY id LIMIT 10（利用有序性）
     * 
     * @param startKey 范围起始值（包含）
     * @param endKey 范围结束值（包含）
     * @return 范围内的所有值
     */
    public java.util.List<String> rangeQuery(int startKey, int endKey) {
        // 【调试日志】记录范围查询
        System.out.println("[范围查询] [" + startKey + ", " + endKey + "]");
        
        java.util.List<String> result = new java.util.ArrayList<>();
        
        if (root == null) {
            return result;  // 空树，返回空列表
        }
        
        // 【步骤 1】找到起始 key 所在的叶子节点
        Node leaf = findLeaf(startKey);
        
        // 【步骤 2】从 startKey 开始，沿着链表向后遍历
        while (leaf != null) {
            for (int key : leaf.keys) {
                if (key >= startKey && key <= endKey) {
                    // 【在范围内】加入结果集
                    result.add("Value-" + key);
                    System.out.print(key + " ");
                } else if (key > endKey) {
                    // 【超出范围】提前结束
                    System.out.println();
                    return result;
                }
            }
            // 【移动到下一个叶子节点】
            leaf = leaf.next;
        }
        
        System.out.println();
        return result;
    }
    
    /**
     * 获取树的高度
     */
    public int getHeight() {
        if (root == null) {
            return 0;
        }
        
        int height = 0;
        Node current = root;
        while (!current.isLeaf) {
            height++;
            current = current.children.get(0);
        }
        return height + 1;
    }
    
    /**
     * 打印 B+Tree 结构（可视化树形）
     * 
     * 【输出格式】
     * 使用 ASCII 字符绘制树形结构，直观展示父子关系
     * 非叶子节点在上，叶子节点在下
     * 同时打印叶子节点的链表连接
     */
    public void printTree() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║        B+Tree 树形结构可视化            ║");
        System.out.println("╚════════════════════════════════════════╝");
        
        if (root == null) {
            System.out.println("(空树)");
            return;
        }
        
        // 按层打印树结构
        printTreeVisual(root, "", true);
        
        // 打印叶子节点链表
        System.out.println("\n┌────────────────────────────────────────┐");
        System.out.println("│ 叶子节点链表（范围查询的关键） │");
        System.out.println("├────────────────────────────────────────┤");
        System.out.print("│ ");
        Node leaf = firstLeaf;
        boolean first = true;
        while (leaf != null) {
            if (!first) {
                System.out.print(" → ");
            }
            System.out.print("[" + String.join(",", leaf.keys.stream().map(String::valueOf).toArray(java.lang.String[]::new)) + "]");
            leaf = leaf.next;
            first = false;
        }
        System.out.println(" → null");
        System.out.println("└────────────────────────────────────────┘");
        
        // 打印统计信息
        System.out.println("\n【统计信息】");
        System.out.println("  • 树高：" + getHeight() + " 层");
        System.out.println("  • 根节点 keys: " + root.keys.size());
        System.out.println("  • 叶子节点数：" + countLeafNodes(root));
        System.out.println();
    }
    
    /**
     * 递归打印树形结构（ASCII 艺术）
     */
    private void printTreeVisual(Node node, String prefix, boolean isTail) {
        if (node == null) return;
        
        // 打印当前节点
        String nodeType = node.isLeaf ? "叶" : "内";
        String keysStr = String.join(",", node.keys.stream().map(String::valueOf).toArray(java.lang.String[]::new));
        System.out.println(prefix + (isTail ? "└── " : "├── ") + "[" + nodeType + "] {" + keysStr + "}");
        
        // 打印子节点
        if (!node.isLeaf && node.children != null) {
            String childPrefix = prefix + (isTail ? "    " : "│   ");
            for (int i = 0; i < node.children.size(); i++) {
                boolean lastChild = (i == node.children.size() - 1);
                printTreeVisual(node.children.get(i), childPrefix, lastChild);
            }
        }
    }
    
    /**
     * 统计叶子节点数量
     */
    private int countLeafNodes(Node node) {
        if (node == null) return 0;
        if (node.isLeaf) return 1;
        
        int count = 0;
        for (Node child : node.children) {
            count += countLeafNodes(child);
        }
        return count;
    }
}
