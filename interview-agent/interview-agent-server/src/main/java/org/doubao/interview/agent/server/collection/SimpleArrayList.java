package org.doubao.interview.agent.server.collection;

import java.util.*;
import java.util.function.Predicate;

/**
 * 简易版ArrayList实现，演示ArrayList的核心原理
 * <p>
 * 1. 基于动态数组实现
 * 2. 支持自动扩容（1.5倍）
 * 3. 实现基本的增删改查操作
 */
public class SimpleArrayList<E> implements List<E> {
    // 默认初始容量
    private static final int DEFAULT_CAPACITY = 10;
    // 空数组实例
    private static final Object[] EMPTY_ELEMENT_DATA = {};
    // 默认容量的空数组实例
    private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};
    
    // 存储元素的数组
    transient Object[] elementData;
    // 实际元素个数
    private int size;
    // 默认容量
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
    // 修改次数
    protected transient int modCount = 0;

    /**
     * 无参构造函数 - 懒加载
     */
    public SimpleArrayList() {
        this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
    }

    /**
     * 指定初始容量的构造函数
     */
    public SimpleArrayList(int initialCapacity) {
        if (initialCapacity > 0) {
            this.elementData = new Object[initialCapacity];
        } else if (initialCapacity == 0) {
            this.elementData = EMPTY_ELEMENT_DATA;
        } else {
            throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
        }
    }

    /**
     * 从集合构造
     */
    public SimpleArrayList(Collection<? extends E> c) {
        elementData = c.toArray();
        if ((size = elementData.length) != 0) {
            if (elementData.getClass() != Object[].class)
                elementData = Arrays.copyOf(elementData, size, Object[].class);
        } else {
            elementData = EMPTY_ELEMENT_DATA;
        }
    }

    /**
     * 获取指定位置的元素
     */
    @Override
    public E get(int index) {
        rangeCheck(index);
        return (E) elementData[index];
    }

    /**
     * 设置指定位置的元素
     */
    @Override
    public E set(int index, E element) {
        rangeCheck(index);
        E oldValue = (E) elementData[index];
        elementData[index] = element;
        return oldValue;
    }

    /**
     * 在尾部添加元素
     */
    @Override
    public boolean add(E e) {
        ensureCapacityInternal(size + 1);  // 确保容量足够
        elementData[size++] = e;
        return true;
    }

    /**
     * 在指定位置插入元素
     */
    @Override
    public void add(int index, E element) {
        rangeCheckForAdd(index);
        ensureCapacityInternal(size + 1);
        // 将index及其后面的元素后移一位
        System.arraycopy(elementData, index, elementData, index + 1, size - index);
        elementData[index] = element;
        size++;
    }

    /**
     * 删除指定位置的元素
     */
    @Override
    @SuppressWarnings("unchecked")
    public E remove(int index) {
        rangeCheck(index);
        modCount++;
        E oldValue = (E) elementData[index];

        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(elementData, index + 1, elementData, index, numMoved);
        
        elementData[--size] = null; // 清空最后一个位置，帮助GC
        return oldValue;
    }

    /**
     * 删除指定元素（首次出现）
     */
    @Override
    public boolean remove(Object o) {
        if (o == null) {
            for (int index = 0; index < size; index++)
                if (elementData[index] == null) {
                    fastRemove(index);
                    return true;
                }
        } else {
            for (int index = 0; index < size; index++)
                if (o.equals(elementData[index])) {
                    fastRemove(index);
                    return true;
                }
        }
        return false;
    }

    /**
     * 快速删除，不进行边界检查
     */
    private void fastRemove(int index) {
        modCount++;
        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(elementData, index + 1, elementData, index, numMoved);
        elementData[--size] = null; // 清空最后一个位置，帮助GC
    }

    /**
     * 获取列表大小
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * 是否为空
     */
    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * 是否包含指定元素
     */
    @Override
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

    /**
     * 返回指定元素的首次出现索引
     */
    @Override
    public int indexOf(Object o) {
        if (o == null) {
            for (int i = 0; i < size; i++)
                if (elementData[i] == null)
                    return i;
        } else {
            for (int i = 0; i < size; i++)
                if (o.equals(elementData[i]))
                    return i;
        }
        return -1;
    }

    /**
     * 返回指定元素的最后出现索引
     */
    @Override
    public int lastIndexOf(Object o) {
        if (o == null) {
            for (int i = size - 1; i >= 0; i--)
                if (elementData[i] == null)
                    return i;
        } else {
            for (int i = size - 1; i >= 0; i--)
                if (o.equals(elementData[i]))
                    return i;
        }
        return -1;
    }

    /**
     * 转换为数组
     */
    @Override
    public Object[] toArray() {
        return Arrays.copyOf(elementData, size);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] a) {
        if (a.length < size)
            return (T[]) Arrays.copyOf(elementData, size, a.getClass());
        System.arraycopy(elementData, 0, a, 0, size);
        if (a.length > size)
            a[size] = null;
        return a;
    }

    /**
     * 确保内部容量足够
     */
    private void ensureCapacityInternal(int minCapacity) {
        if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
            minCapacity = Math.max(DEFAULT_CAPACITY, minCapacity);
        }
        ensureExplicitCapacity(minCapacity);
    }

    private void ensureExplicitCapacity(int minCapacity) {
        if (minCapacity - elementData.length > 0)
            grow(minCapacity);
    }

    /**
     * 核心扩容方法 - 扩容为原来的1.5倍
     */
    private void grow(int minCapacity) {
        int oldCapacity = elementData.length;
        // 新容量 = 旧容量 + 旧容量右移1位(即旧容量/2)，也就是1.5倍
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        
        // 如果1.5倍还不够，就用最小所需容量
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
            
        // 检查是否超过最大数组大小
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
            
        // 复制数组到新容量
        elementData = Arrays.copyOf(elementData, newCapacity);
    }

    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ?
            Integer.MAX_VALUE :
            MAX_ARRAY_SIZE;
    }

    /**
     * 边界检查
     */
    private void rangeCheck(int index) {
        if (index >= size || index < 0)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    /**
     * 添加操作的边界检查
     */
    private void rangeCheckForAdd(int index) {
        if (index > size || index < 0)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    private String outOfBoundsMsg(int index) {
        return "Index: " + index + ", Size: " + size;
    }

    // 以下方法暂时不实现或简单实现，因为我们只关注核心功能
    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object e : c)
            if (!contains(e))
                return false;
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        Object[] a = c.toArray();
        int numNew = a.length;
        ensureCapacityInternal(size + numNew);
        System.arraycopy(a, 0, elementData, size, numNew);
        size += numNew;
        return numNew != 0;
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        rangeCheckForAdd(index);

        Object[] a = c.toArray();
        int numNew = a.length;
        if (numNew == 0)
            return false;

        int numMoved = size - index;
        if (numMoved >= 0)
            System.arraycopy(elementData, index, elementData, index + numNew, numMoved);

        System.arraycopy(a, 0, elementData, index, numNew);
        size += numNew;
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        Objects.requireNonNull(c);
        boolean modified = false;
        Iterator<E> it = iterator();
        while (it.hasNext()) {
            if (c.contains(it.next())) {
                it.remove();
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        Objects.requireNonNull(c);
        boolean modified = false;
        Iterator<E> it = iterator();
        while (it.hasNext()) {
            if (!c.contains(it.next())) {
                it.remove();
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public void clear() {

    }

    @Override
    public Iterator<E> iterator() {
        return new Itr();
    }

    /**
     * 简单的迭代器实现
     */
    private class Itr implements Iterator<E> {
        int cursor;       // 下一个要返回的元素索引
        int lastRet = -1; // 上一个返回的元素索引

        @Override
        public boolean hasNext() {
            return cursor != size;
        }

        @Override
        @SuppressWarnings("unchecked")
        public E next() {
            int i = cursor;
            if (i >= size)
                throw new NoSuchElementException();
            cursor = i + 1;
            return (E) elementData[lastRet = i];
        }
    }

    @Override
    public ListIterator<E> listIterator() {
        return listIterator(0);
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        rangeCheckForAdd(index);
        return new ListItr(index);
    }

    private class ListItr extends Itr implements ListIterator<E> {
        ListItr(int index) {
            cursor = index;
        }

        @Override
        public boolean hasPrevious() {
            return cursor != 0;
        }

        @Override
        public E previous() {
            return null;
        }

        @Override
        public int nextIndex() {
            return cursor;
        }

        @Override
        public int previousIndex() {
            return cursor - 1;
        }

        @Override
        public void remove() {

        }

        @Override
        public void set(E e) {
            if (lastRet < 0)
                throw new IllegalStateException();
            SimpleArrayList.this.set(lastRet, e);
        }

        @Override
        public void add(E e) {
            int i = cursor;
            SimpleArrayList.this.add(i, e);
            cursor = i + 1;
            lastRet = -1;
        }
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        // 简单实现
        throw new UnsupportedOperationException("SubList not implemented in this demo");
    }


    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        Objects.requireNonNull(filter);
        boolean removed = false;
        final int size = this.size;
        for (int i = 0; i < size; i++) {
            @SuppressWarnings("unchecked")
            final E e = (E) elementData[i];
            if (filter.test(e)) {
                remove(i--);
                removed = true;
            }
        }
        return removed;
    }
}