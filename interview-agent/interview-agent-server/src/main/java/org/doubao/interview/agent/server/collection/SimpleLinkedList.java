package org.doubao.interview.agent.server.collection;

import lombok.extern.slf4j.Slf4j;

/**
 * 简易 LinkedList 实现（面试解析专用）
 * 核心原理：基于双向链表实现，支持 O(1) 的头尾增删，O(n) 的随机访问。
 * 
 * @param <E> 元素类型
 */
@Slf4j
public class SimpleLinkedList<E> {

    /**
     * 链表大小
     */
    private int size = 0;

    /**
     * 指向第一个节点的指针
     */
    private Node<E> first;

    /**
     * 指向最后一个节点的指针
     */
    private Node<E> last;

    /**
     * 内部节点类：双向链表的最小存储单元
     */
    private static class Node<E> {
        E item;
        Node<E> next;
        Node<E> prev;

        Node(Node<E> prev, E element, Node<E> next) {
            this.item = element;
            this.next = next;
            this.prev = prev;
        }
    }

    /**
     * 在链表尾部添加元素
     * 对应面试点：LinkedList 默认添加是在尾部，时间复杂度 O(1)
     */
    public void add(E e) {
        linkLast(e);
    }

    /**
     * 链接到末尾
     */
    void linkLast(E e) {
        final Node<E> l = last;
        final Node<E> newNode = new Node<>(l, e, null);
        last = newNode;
        if (l == null) {
            // 如果原链表为空，新节点也是头节点
            first = newNode;
        } else {
            // 原尾节点的 next 指向新节点
            l.next = newNode;
        }
        size++;
    }

    /**
     * 在链表头部添加元素
     * 对应面试点：LinkedList 实现 Deque 接口，支持高效头插，时间复杂度 O(1)
     */
    public void addFirst(E e) {
        linkFirst(e);
    }

    private void linkFirst(E e) {
        final Node<E> f = first;
        final Node<E> newNode = new Node<>(null, e, f);
        first = newNode;
        if (f == null) {
            last = newNode;
        } else {
            f.prev = newNode;
        }
        size++;
    }

    /**
     * 获取指定索引的元素
     * 对应面试点：LinkedList 随机访问慢，需要遍历，时间复杂度 O(n)
     * 优化点：根据 index 决定从头还是从尾开始遍历（二分思想）
     */
    public E get(int index) {
        checkElementIndex(index);
        return node(index).item;
    }

    /**
     * 核心查找方法：根据索引查找节点
     */
    Node<E> node(int index) {
        // 二分查找优化：如果 index 小于 size 的一半，从头往后找；否则从后往前找
        if (index < (size >> 1)) {
            Node<E> x = first;
            for (int i = 0; i < index; i++)
                x = x.next;
            return x;
        } else {
            Node<E> x = last;
            for (int i = size - 1; i > index; i--)
                x = x.prev;
            return x;
        }
    }

    /**
     * 删除指定位置的元素
     * 对应面试点：中间删除需要先找到节点 O(n)，再修改指针 O(1)
     */
    public E remove(int index) {
        checkElementIndex(index);
        return unlink(node(index));
    }

    /**
     * 断开节点链接
     */
    E unlink(Node<E> x) {
        final E element = x.item;
        final Node<E> next = x.next;
        final Node<E> prev = x.prev;

        if (prev == null) {
            // 如果是头节点
            first = next;
        } else {
            prev.next = next;
            x.prev = null;
        }

        if (next == null) {
            // 如果是尾节点
            last = prev;
        } else {
            next.prev = prev;
            x.next = null;
        }

        x.item = null; // 帮助 GC
        size--;
        return element;
    }

    public int size() {
        return size;
    }

    private void checkElementIndex(int index) {
        if (!(index >= 0 && index < size))
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        Node<E> curr = first;
        while (curr != null) {
            sb.append(curr.item);
            if (curr.next != null) sb.append(", ");
            curr = curr.next;
        }
        sb.append("]");
        return sb.toString();
    }
}
