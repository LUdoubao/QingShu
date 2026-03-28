package org.doubao.interview.mysql.q002.index;

/**
 * 红黑树简化实现（用于对比演示）
 * 
 * 核心特点：
 * 1. 自平衡二叉查找树
 * 2. 每个节点是红色或黑色
 * 3. 根节点是黑色
 * 4. 叶子节点（NIL）是黑色
 * 5. 红色节点的子节点必须是黑色
 * 6. 从任一节点到叶子的所有路径包含相同数目的黑色节点
 * 
 * 缺点（相比 B+Tree）：
 * - 树高较高，磁盘 I/O 次数多
 * - 不适合范围查询
 */
public class RedBlackTree {
    
    /**
     * 红黑树节点
     */
    static class Node {
        int key;
        String value;
        boolean isRed;  // true=红色，false=黑色
        Node left, right, parent;
        
        public Node(int key, String value, boolean isRed) {
            this.key = key;
            this.value = value;
            this.isRed = isRed;
        }
    }
    
    /** 根节点 */
    private Node root;
    
    /** 插入次数统计 */
    private int insertCount = 0;
    
    /**
     * 插入 key-value 对
     */
    public void insert(int key, String value) {
        System.out.println("[红黑树] 插入 key=" + key + ", value=" + value);
        insertCount++;
        
        Node newNode = new Node(key, value, true);  // 新节点为红色
        
        if (root == null) {
            root = newNode;
            root.isRed = false;  // 根节点必须是黑色
            System.out.println("  -> 创建根节点（黑色）");
            return;
        }
        
        // 找到插入位置
        Node current = root;
        Node parent = null;
        
        while (current != null) {
            parent = current;
            if (key < current.key) {
                current = current.left;
            } else if (key > current.key) {
                current = current.right;
            } else {
                // key 已存在，更新
                current.value = value;
                System.out.println("  [更新] key=" + key);
                return;
            }
        }
        
        // 插入节点
        newNode.parent = parent;
        if (key < parent.key) {
            parent.left = newNode;
        } else {
            parent.right = newNode;
        }
        
        System.out.println("  -> 插入完成，当前树高：" + getHeight());
        
        // 修复红黑树性质（简化处理）
        fixInsert(newNode);
    }
    
    /**
     * 修复插入后的红黑树性质
     */
    private void fixInsert(Node node) {
        // 简化版：只处理简单的旋转和变色
        // 实际实现需要复杂的旋转逻辑
        
        while (node != root && node.parent.isRed) {
            if (node.parent == node.parent.parent.left) {
                Node uncle = node.parent.parent.right;
                
                if (uncle != null && uncle.isRed) {
                    // Case 1: 叔叔节点是红色
                    node.parent.isRed = false;
                    uncle.isRed = false;
                    node.parent.parent.isRed = true;
                    node = node.parent.parent;
                } else {
                    if (node == node.parent.right) {
                        // Case 2: 叔叔节点是黑色，且 node 是右孩子
                        node = node.parent;
                        rotateLeft(node);
                    }
                    // Case 3: 叔叔节点是黑色，且 node 是左孩子
                    node.parent.isRed = false;
                    node.parent.parent.isRed = true;
                    rotateRight(node.parent.parent);
                }
            } else {
                // 对称情况
                Node uncle = node.parent.parent.left;
                
                if (uncle != null && uncle.isRed) {
                    node.parent.isRed = false;
                    uncle.isRed = false;
                    node.parent.parent.isRed = true;
                    node = node.parent.parent;
                } else {
                    if (node == node.parent.left) {
                        node = node.parent;
                        rotateRight(node);
                    }
                    node.parent.isRed = false;
                    node.parent.parent.isRed = true;
                    rotateLeft(node.parent.parent);
                }
            }
        }
        
        root.isRed = false;
    }
    
    /**
     * 左旋转
     */
    private void rotateLeft(Node x) {
        Node y = x.right;
        x.right = y.left;
        
        if (y.left != null) {
            y.left.parent = x;
        }
        
        y.parent = x.parent;
        
        if (x.parent == null) {
            root = y;
        } else if (x == x.parent.left) {
            x.parent.left = y;
        } else {
            x.parent.right = y;
        }
        
        y.left = x;
        x.parent = y;
    }
    
    /**
     * 右旋转
     */
    private void rotateRight(Node x) {
        Node y = x.left;
        x.left = y.right;
        
        if (y.right != null) {
            y.right.parent = x;
        }
        
        y.parent = x.parent;
        
        if (x.parent == null) {
            root = y;
        } else if (x == x.parent.right) {
            x.parent.right = y;
        } else {
            x.parent.left = y;
        }
        
        y.right = x;
        x.parent = y;
    }
    
    /**
     * 查询 key
     */
    public String get(int key) {
        Node current = root;
        int depth = 0;
        
        while (current != null) {
            depth++;
            if (key == current.key) {
                System.out.println("[查询] key=" + key + " -> 找到（深度=" + depth + "）");
                return current.value;
            } else if (key < current.key) {
                current = current.left;
            } else {
                current = current.right;
            }
        }
        
        System.out.println("[查询] key=" + key + " -> 未找到");
        return null;
    }
    
    /**
     * 中序遍历（有序输出）
     */
    public void inorderTraversal() {
        System.out.print("[中序遍历] ");
        inorder(root);
        System.out.println();
    }
    
    private void inorder(Node node) {
        if (node == null) return;
        
        inorder(node.left);
        System.out.print(node.key + "(" + (node.isRed ? "红" : "黑") + ") ");
        inorder(node.right);
    }
    
    /**
     * 获取树的高度
     */
    public int getHeight() {
        return getHeight(root);
    }
    
    private int getHeight(Node node) {
        if (node == null) {
            return 0;
        }
        return 1 + Math.max(getHeight(node.left), getHeight(node.right));
    }
    
    /**
     * 打印红黑树结构（可视化树形）
     * 
     * 【输出格式】
     * 使用 ASCII 字符绘制树形结构，直观展示父子关系
     * 🔴 表示红色节点，⚫ 表示黑色节点
     */
    public void printTree() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║      红黑树树形结构可视化               ║");
        System.out.println("╚════════════════════════════════════════╝");
        
        if (root == null) {
            System.out.println("(空树)");
            return;
        }
        
        // 递归打印树形结构
        printTreeVisual(root, "", true);
        
        // 打印统计信息
        System.out.println("\n【统计信息】");
        System.out.println("  • 树高：" + getHeight() + " 层");
        System.out.println("  • 节点总数：" + countNodes(root));
        System.out.println();
    }
    
    /**
     * 递归打印树形结构（ASCII 艺术）
     */
    private void printTreeVisual(Node node, String prefix, boolean isTail) {
        if (node == null) return;
        
        // 打印当前节点
        String color = node.isRed ? "🔴" : "⚫";
        String marker = isTail ? "└── " : "├── ";
        System.out.println(prefix + marker + color + " " + node.key + "(" + (node.isRed ? "红" : "黑") + ")");
        
        // 打印子节点
        String childPrefix = prefix + (isTail ? "    " : "│   ");
        if (node.left != null) {
            printTreeVisual(node.left, childPrefix, false);
        }
        if (node.right != null) {
            printTreeVisual(node.right, childPrefix, true);
        }
    }
    
    /**
     * 统计节点数量
     */
    private int countNodes(Node node) {
        if (node == null) return 0;
        return 1 + countNodes(node.left) + countNodes(node.right);
    }
    
    /**
     * 获取插入次数
     */
    public int getInsertCount() {
        return insertCount;
    }
}
