package java.util;

import java.util.function.Consumer;

/**
 * {@code List} 和 {@code Deque} 接口的双向链表实现。
 * 实现了所有可选的列表操作，并允许所有元素（包括 {@code null}）。
 *
 * <p>所有操作的执行都与双向链表的预期一致。索引到列表中的操作将从开头或结尾遍历列表，
 * 以更接近指定索引的一端为准。
 *
 * <p><strong>请注意，此实现不是同步的。</strong>
 * 如果多个线程同时访问一个链表，并且至少有一个线程在结构上修改了列表，则必须在外部进行同步。
 * （结构修改是指添加或删除一个或多个元素的任何操作；仅设置元素的值不是结构修改。）
 * 这通常通过同步一些自然封装列表的对象来完成。
 *
 * 如果不存在这样的对象，则应使用 {@link Collections#synchronizedList Collections.synchronizedList}
 * 方法“包装”列表。最好在创建时执行此操作，以防止意外地不同步访问列表：
 * <pre>
 *   List list = Collections.synchronizedList(new LinkedList(...));</pre>
 *
 * <p>此类的 {@code iterator} 和 {@code listIterator} 方法返回的迭代器是 <i>快速失败</i> 的：
 * 如果在创建迭代器之后的任何时间对列表进行结构修改，除非通过迭代器自己的 {@code remove} 或
 * {@code add} 方法，否则迭代器将抛出 {@link ConcurrentModificationException}。
 * 因此，在面对并发修改时，迭代器会快速而干净地失败，而不是在未来不确定的时间冒任意、不确定行为的风险。
 *
 * <p>请注意，迭代器的快速失败行为无法得到保证，因为通常来说，在存在不同步的并发修改的情况下，
 * 不可能做出任何硬性保证。快速失败迭代器会尽最大努力抛出 {@code ConcurrentModificationException}。
 * 因此，编写依赖于此异常的程序是错误的：<i>迭代器的快速失败行为应该仅用于检测错误。</i>
 *
 * <p>此类是 <a href="{@docRoot}/../technotes/guides/collections/index.html">Java 集合框架</a> 的成员。
 *
 * @author  Josh Bloch
 * @see     List
 * @see     ArrayList
 * @since   1.2
 * @param <E> 此集合中保存的元素类型
 */
public class LinkedList<E>
        extends AbstractSequentialList<E>
        implements List<E>, Deque<E>, Cloneable, java.io.Serializable
{
    transient int size = 0;  // 列表中的元素个数，transient 表示不参与默认序列化

    /**
     * 指向第一个节点的指针。
     * 不变式： (first == null && last == null) ||
     *          (first.prev == null && first.item != null)
     */
    transient Node<E> first;

    /**
     * 指向最后一个节点的指针。
     * 不变式： (first == null && last == null) ||
     *          (last.next == null && last.item != null)
     */
    transient Node<E> last;

    /**
     * 构造一个空列表。
     */
    public LinkedList() {
    }

    /**
     * 按照指定集合的迭代器返回的顺序构造一个包含指定集合元素的列表。
     *
     * @param  c 其元素将被放入此列表的集合
     * @throws NullPointerException 如果指定的集合为 null
     */
    public LinkedList(Collection<? extends E> c) {
        this();
        addAll(c);
    }

    /**
     * 将 e 作为第一个元素链接。
     */
    private void linkFirst(E e) {
        final Node<E> f = first;
        final Node<E> newNode = new Node<>(null, e, f);
        first = newNode;
        if (f == null)
            last = newNode;
        else
            f.prev = newNode;
        size++;
        modCount++;
    }

    /**
     * 将 e 作为最后一个元素链接。
     */
    void linkLast(E e) {
        final Node<E> l = last;
        final Node<E> newNode = new Node<>(l, e, null);
        last = newNode;
        if (l == null)
            first = newNode;
        else
            l.next = newNode;
        size++;
        modCount++;
    }

    /**
     * 在非空节点 succ 之前插入元素 e。
     */
    void linkBefore(E e, Node<E> succ) {
        // assert succ != null;
        final Node<E> pred = succ.prev;
        final Node<E> newNode = new Node<>(pred, e, succ);
        succ.prev = newNode;
        if (pred == null)
            first = newNode;
        else
            pred.next = newNode;
        size++;
        modCount++;
    }

    /**
     * 取消链接非空的首节点 f。
     */
    private E unlinkFirst(Node<E> f) {
        // assert f == first && f != null;
        final E element = f.item;
        final Node<E> next = f.next;
        f.item = null;
        f.next = null; // 帮助 GC
        first = next;
        if (next == null)
            last = null;
        else
            next.prev = null;
        size--;
        modCount++;
        return element;
    }

    /**
     * 取消链接非空的尾节点 l。
     */
    private E unlinkLast(Node<E> l) {
        // assert l == last && l != null;
        final E element = l.item;
        final Node<E> prev = l.prev;
        l.item = null;
        l.prev = null; // 帮助 GC
        last = prev;
        if (prev == null)
            first = null;
        else
            prev.next = null;
        size--;
        modCount++;
        return element;
    }

    /**
     * 取消链接非空节点 x。
     */
    E unlink(Node<E> x) {
        // assert x != null;
        final E element = x.item;
        final Node<E> next = x.next;
        final Node<E> prev = x.prev;

        if (prev == null) {
            first = next;
        } else {
            prev.next = next;
            x.prev = null;
        }

        if (next == null) {
            last = prev;
        } else {
            next.prev = prev;
            x.next = null;
        }

        x.item = null;
        size--;
        modCount++;
        return element;
    }

    /**
     * 返回此列表中的第一个元素。
     *
     * @return 此列表中的第一个元素
     * @throws NoSuchElementException 如果此列表为空
     */
    public E getFirst() {
        final Node<E> f = first;
        if (f == null)
            throw new NoSuchElementException();
        return f.item;
    }

    /**
     * 返回此列表中的最后一个元素。
     *
     * @return 此列表中的最后一个元素
     * @throws NoSuchElementException 如果此列表为空
     */
    public E getLast() {
        final Node<E> l = last;
        if (l == null)
            throw new NoSuchElementException();
        return l.item;
    }

    /**
     * 移除并返回此列表中的第一个元素。
     *
     * @return 此列表中的第一个元素
     * @throws NoSuchElementException 如果此列表为空
     */
    public E removeFirst() {
        final Node<E> f = first;
        if (f == null)
            throw new NoSuchElementException();
        return unlinkFirst(f);
    }

    /**
     * 移除并返回此列表中的最后一个元素。
     *
     * @return 此列表中的最后一个元素
     * @throws NoSuchElementException 如果此列表为空
     */
    public E removeLast() {
        final Node<E> l = last;
        if (l == null)
            throw new NoSuchElementException();
        return unlinkLast(l);
    }

    /**
     * 将指定元素插入此列表的开头。
     *
     * @param e 要添加的元素
     */
    public void addFirst(E e) {
        linkFirst(e);
    }

    /**
     * 将指定元素追加到此列表的末尾。
     *
     * <p>此方法等效于 {@link #add}。
     *
     * @param e 要添加的元素
     */
    public void addLast(E e) {
        linkLast(e);
    }

    /**
     * 如果此列表包含指定元素，则返回 {@code true}。
     * 更正式地说，当且仅当此列表包含至少一个满足
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt> 的元素 e 时返回 {@code true}。
     *
     * @param o 要测试是否存在于列表中的元素
     * @return 如果此列表包含指定元素，则返回 {@code true}
     */
    public boolean contains(Object o) {
        return indexOf(o) != -1;
    }

    /**
     * 返回此列表中的元素数。
     *
     * @return 此列表中的元素数
     */
    public int size() {
        return size;
    }

    /**
     * 将指定元素追加到此列表的末尾。
     *
     * <p>此方法等效于 {@link #addLast}。
     *
     * @param e 要追加到此列表的元素
     * @return {@code true}（根据 {@link Collection#add} 的规定）
     */
    public boolean add(E e) {
        linkLast(e);
        return true;
    }

    /**
     * 如果存在，则从此列表中移除指定元素的第一个出现。
     * 如果列表不包含该元素，则不变。
     * 更正式地说，移除具有最低索引 i 的元素，使得
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>
     * （如果存在这样的元素）。如果此列表包含指定元素（或等效地，如果此列表因调用而更改），则返回 {@code true}。
     *
     * @param o 如果存在，要从此列表中移除的元素
     * @return 如果此列表包含指定元素，则返回 {@code true}
     */
    public boolean remove(Object o) {
        if (o == null) {
            for (Node<E> x = first; x != null; x = x.next) {
                if (x.item == null) {
                    unlink(x);
                    return true;
                }
            }
        } else {
            for (Node<E> x = first; x != null; x = x.next) {
                if (o.equals(x.item)) {
                    unlink(x);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 将指定集合中的所有元素按该集合迭代器返回的顺序追加到此列表的末尾。
     * 如果在操作进行中修改了指定的集合，则此操作的行为未定义。
     * （注意，如果指定的集合是此列表且非空，则会发生这种情况。）
     *
     * @param c 包含要添加到此列表的元素的集合
     * @return 如果此列表因调用而更改，则返回 {@code true}
     * @throws NullPointerException 如果指定的集合为 null
     */
    public boolean addAll(Collection<? extends E> c) {
        return addAll(size, c);
    }

    /**
     * 从指定位置开始，将指定集合中的所有元素插入此列表。
     * 将当前位于该位置的元素（如果有）和任何后续元素向右移动（增加其索引）。
     * 新元素将按照指定集合迭代器返回的顺序出现在列表中。
     *
     * @param index 从指定集合插入第一个元素的索引
     * @param c 包含要添加到此列表的元素的集合
     * @return 如果此列表因调用而更改，则返回 {@code true}
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @throws NullPointerException 如果指定的集合为 null
     */
    public boolean addAll(int index, Collection<? extends E> c) {
        checkPositionIndex(index);

        Object[] a = c.toArray();
        int numNew = a.length;
        if (numNew == 0)
            return false;

        Node<E> pred, succ;
        if (index == size) {
            succ = null;
            pred = last;
        } else {
            succ = node(index);
            pred = succ.prev;
        }

        for (Object o : a) {
            @SuppressWarnings("unchecked") E e = (E) o;
            Node<E> newNode = new Node<>(pred, e, null);
            if (pred == null)
                first = newNode;
            else
                pred.next = newNode;
            pred = newNode;
        }

        if (succ == null) {
            last = pred;
        } else {
            pred.next = succ;
            succ.prev = pred;
        }

        size += numNew;
        modCount++;
        return true;
    }

    /**
     * 从此列表中删除所有元素。此调用返回后，列表将为空。
     */
    public void clear() {
        // 清除节点之间的所有链接是“不必要的”，但是：
        // - 如果废弃的节点存在于多个代中，则有助于分代 GC
        // - 即使存在可达的迭代器，也确保释放内存
        for (Node<E> x = first; x != null; ) {
            Node<E> next = x.next;
            x.item = null;
            x.next = null;
            x.prev = null;
            x = next;
        }
        first = last = null;
        size = 0;
        modCount++;
    }


    // 位置访问操作

    /**
     * 返回此列表中指定位置的元素。
     *
     * @param index 要返回的元素的索引
     * @return 此列表中指定位置的元素
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public E get(int index) {
        checkElementIndex(index);
        return node(index).item;
    }

    /**
     * 用指定元素替换此列表中指定位置的元素。
     *
     * @param index 要替换的元素的索引
     * @param element 要存储在指定位置的元素
     * @return 先前在指定位置的元素
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public E set(int index, E element) {
        checkElementIndex(index);
        Node<E> x = node(index);
        E oldVal = x.item;
        x.item = element;
        return oldVal;
    }

    /**
     * 在此列表中的指定位置插入指定元素。
     * 将当前位于该位置的元素（如果有）和任何后续元素向右移动（增加其索引）。
     *
     * @param index 要插入指定元素的索引
     * @param element 要插入的元素
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public void add(int index, E element) {
        checkPositionIndex(index);

        if (index == size)
            linkLast(element);
        else
            linkBefore(element, node(index));
    }

    /**
     * 移除此列表中指定位置的元素。将所有后续元素向左移动（减少其索引）。
     * 返回从列表中移除的元素。
     *
     * @param index 要移除的元素的索引
     * @return 先前在指定位置的元素
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public E remove(int index) {
        checkElementIndex(index);
        return unlink(node(index));
    }

    /**
     * 判断参数是否为现有元素的索引。
     */
    private boolean isElementIndex(int index) {
        return index >= 0 && index < size;
    }

    /**
     * 判断参数是否为迭代器或添加操作的有效位置的索引。
     */
    private boolean isPositionIndex(int index) {
        return index >= 0 && index <= size;
    }

    /**
     * 构造 IndexOutOfBoundsException 的详细消息。
     * 在可能的错误处理代码重构中，这种“提取”方式在服务器和客户端 VM 中表现最佳。
     */
    private String outOfBoundsMsg(int index) {
        return "Index: "+index+", Size: "+size;
    }

    private void checkElementIndex(int index) {
        if (!isElementIndex(index))
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    private void checkPositionIndex(int index) {
        if (!isPositionIndex(index))
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    /**
     * 返回指定元素索引处的（非空）节点。
     */
    Node<E> node(int index) {
        // assert isElementIndex(index);

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

    // 搜索操作

    /**
     * 返回此列表中指定元素的第一次出现的索引，如果此列表不包含该元素，则返回 -1。
     * 更正式地说，返回最低索引 i，使得
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>，
     * 如果没有这样的索引，则返回 -1。
     *
     * @param o 要搜索的元素
     * @return 此列表中指定元素的第一次出现的索引，如果此列表不包含该元素，则返回 -1
     */
    public int indexOf(Object o) {
        int index = 0;
        if (o == null) {
            for (Node<E> x = first; x != null; x = x.next) {
                if (x.item == null)
                    return index;
                index++;
            }
        } else {
            for (Node<E> x = first; x != null; x = x.next) {
                if (o.equals(x.item))
                    return index;
                index++;
            }
        }
        return -1;
    }

    /**
     * 返回此列表中指定元素的最后一次出现的索引，如果此列表不包含该元素，则返回 -1。
     * 更正式地说，返回最高索引 i，使得
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>，
     * 如果没有这样的索引，则返回 -1。
     *
     * @param o 要搜索的元素
     * @return 此列表中指定元素的最后一次出现的索引，如果此列表不包含该元素，则返回 -1
     */
    public int lastIndexOf(Object o) {
        int index = size;
        if (o == null) {
            for (Node<E> x = last; x != null; x = x.prev) {
                index--;
                if (x.item == null)
                    return index;
            }
        } else {
            for (Node<E> x = last; x != null; x = x.prev) {
                index--;
                if (o.equals(x.item))
                    return index;
            }
        }
        return -1;
    }

    // 队列操作

    /**
     * 检索但不移除此列表的头（第一个元素）。
     *
     * @return 此列表的头，如果此列表为空则返回 {@code null}
     * @since 1.5
     */
    public E peek() {
        final Node<E> f = first;
        return (f == null) ? null : f.item;
    }

    /**
     * 检索但不移除此列表的头（第一个元素）。
     *
     * @return 此列表的头
     * @throws NoSuchElementException 如果此列表为空
     * @since 1.5
     */
    public E element() {
        return getFirst();
    }

    /**
     * 检索并移除此列表的头（第一个元素）。
     *
     * @return 此列表的头，如果此列表为空则返回 {@code null}
     * @since 1.5
     */
    public E poll() {
        final Node<E> f = first;
        return (f == null) ? null : unlinkFirst(f);
    }

    /**
     * 检索并移除此列表的头（第一个元素）。
     *
     * @return 此列表的头
     * @throws NoSuchElementException 如果此列表为空
     * @since 1.5
     */
    public E remove() {
        return removeFirst();
    }

    /**
     * 将指定元素添加为此列表的尾部（最后一个元素）。
     *
     * @param e 要添加的元素
     * @return {@code true}（根据 {@link Queue#offer} 的规定）
     * @since 1.5
     */
    public boolean offer(E e) {
        return add(e);
    }

    // 双端队列操作
    /**
     * 在此列表的前面插入指定元素。
     *
     * @param e 要插入的元素
     * @return {@code true}（根据 {@link Deque#offerFirst} 的规定）
     * @since 1.6
     */
    public boolean offerFirst(E e) {
        addFirst(e);
        return true;
    }

    /**
     * 在此列表的末尾插入指定元素。
     *
     * @param e 要插入的元素
     * @return {@code true}（根据 {@link Deque#offerLast} 的规定）
     * @since 1.6
     */
    public boolean offerLast(E e) {
        addLast(e);
        return true;
    }

    /**
     * 检索但不移除此列表的第一个元素，如果此列表为空，则返回 {@code null}。
     *
     * @return 此列表的第一个元素，如果此列表为空则返回 {@code null}
     * @since 1.6
     */
    public E peekFirst() {
        final Node<E> f = first;
        return (f == null) ? null : f.item;
    }

    /**
     * 检索但不移除此列表的最后一个元素，如果此列表为空，则返回 {@code null}。
     *
     * @return 此列表的最后一个元素，如果此列表为空则返回 {@code null}
     * @since 1.6
     */
    public E peekLast() {
        final Node<E> l = last;
        return (l == null) ? null : l.item;
    }

    /**
     * 检索并移除此列表的第一个元素，如果此列表为空，则返回 {@code null}。
     *
     * @return 此列表的第一个元素，如果此列表为空则返回 {@code null}
     * @since 1.6
     */
    public E pollFirst() {
        final Node<E> f = first;
        return (f == null) ? null : unlinkFirst(f);
    }

    /**
     * 检索并移除此列表的最后一个元素，如果此列表为空，则返回 {@code null}。
     *
     * @return 此列表的最后一个元素，如果此列表为空则返回 {@code null}
     * @since 1.6
     */
    public E pollLast() {
        final Node<E> l = last;
        return (l == null) ? null : unlinkLast(l);
    }

    /**
     * 将元素推入此列表表示的栈中。换句话说，在列表的前面插入元素。
     *
     * <p>此方法等效于 {@link #addFirst}。
     *
     * @param e 要推送的元素
     * @since 1.6
     */
    public void push(E e) {
        addFirst(e);
    }

    /**
     * 从此列表表示的栈中弹出一个元素。换句话说，移除并返回此列表的第一个元素。
     *
     * <p>此方法等效于 {@link #removeFirst()}。
     *
     * @return 此列表前面的元素（即此列表表示的栈的顶部）
     * @throws NoSuchElementException 如果此列表为空
     * @since 1.6
     */
    public E pop() {
        return removeFirst();
    }

    /**
     * 移除在此列表中第一次出现的指定元素（从头部到尾部遍历列表时）。
     * 如果列表不包含该元素，则不变。
     *
     * @param o 如果存在，要从此列表中移除的元素
     * @return 如果列表包含指定元素，则返回 {@code true}
     * @since 1.6
     */
    public boolean removeFirstOccurrence(Object o) {
        return remove(o);
    }

    /**
     * 移除在此列表中最后一次出现的指定元素（从头部到尾部遍历列表时）。
     * 如果列表不包含该元素，则不变。
     *
     * @param o 如果存在，要从此列表中移除的元素
     * @return 如果列表包含指定元素，则返回 {@code true}
     * @since 1.6
     */
    public boolean removeLastOccurrence(Object o) {
        if (o == null) {
            for (Node<E> x = last; x != null; x = x.prev) {
                if (x.item == null) {
                    unlink(x);
                    return true;
                }
            }
        } else {
            for (Node<E> x = last; x != null; x = x.prev) {
                if (o.equals(x.item)) {
                    unlink(x);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 返回此列表中元素的列表迭代器（按适当顺序），从列表中的指定位置开始。
     * 遵循 {@code List.listIterator(int)} 的通用合同。<p>
     *
     * 列表迭代器是 <i>快速失败</i> 的：如果在创建迭代器之后的任何时间对列表进行结构修改，
     * 除非通过列表迭代器自己的 {@code remove} 或 {@code add} 方法，否则列表迭代器将抛出
     * {@code ConcurrentModificationException}。因此，在面对并发修改时，
     * 迭代器会快速而干净地失败，而不是在未来不确定的时间冒任意、不确定行为的风险。
     *
     * @param index 从列表迭代器返回的第一个元素的索引（通过调用 {@code next}）
     * @return 此列表中元素的列表迭代器（按适当顺序），从列表中的指定位置开始
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @see List#listIterator(int)
     */
    public ListIterator<E> listIterator(int index) {
        checkPositionIndex(index);
        return new ListItr(index);
    }

    private class ListItr implements ListIterator<E> {
        private Node<E> lastReturned;   // 最后返回的节点
        private Node<E> next;            // 下一个节点
        private int nextIndex;           // 下一个节点的索引
        private int expectedModCount = modCount;  // 期望的修改计数，用于快速失败

        ListItr(int index) {
            // assert isPositionIndex(index);
            next = (index == size) ? null : node(index);
            nextIndex = index;
        }

        public boolean hasNext() {
            return nextIndex < size;
        }

        public E next() {
            checkForComodification();
            if (!hasNext())
                throw new NoSuchElementException();

            lastReturned = next;
            next = next.next;
            nextIndex++;
            return lastReturned.item;
        }

        public boolean hasPrevious() {
            return nextIndex > 0;
        }

        public E previous() {
            checkForComodification();
            if (!hasPrevious())
                throw new NoSuchElementException();

            lastReturned = next = (next == null) ? last : next.prev;
            nextIndex--;
            return lastReturned.item;
        }

        public int nextIndex() {
            return nextIndex;
        }

        public int previousIndex() {
            return nextIndex - 1;
        }

        public void remove() {
            checkForComodification();
            if (lastReturned == null)
                throw new IllegalStateException();

            Node<E> lastNext = lastReturned.next;
            unlink(lastReturned);
            if (next == lastReturned)
                next = lastNext;
            else
                nextIndex--;
            lastReturned = null;
            expectedModCount++;
        }

        public void set(E e) {
            if (lastReturned == null)
                throw new IllegalStateException();
            checkForComodification();
            lastReturned.item = e;
        }

        public void add(E e) {
            checkForComodification();
            lastReturned = null;
            if (next == null)
                linkLast(e);
            else
                linkBefore(e, next);
            nextIndex++;
            expectedModCount++;
        }

        public void forEachRemaining(Consumer<? super E> action) {
            Objects.requireNonNull(action);
            while (modCount == expectedModCount && nextIndex < size) {
                action.accept(next.item);
                lastReturned = next;
                next = next.next;
                nextIndex++;
            }
            checkForComodification();
        }

        final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }

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
     * @since 1.6
     */
    public Iterator<E> descendingIterator() {
        return new DescendingIterator();
    }

    /**
     * 通过 ListItr.previous 提供反向迭代器的适配器。
     */
    private class DescendingIterator implements Iterator<E> {
        private final ListItr itr = new ListItr(size());
        public boolean hasNext() {
            return itr.hasPrevious();
        }
        public E next() {
            return itr.previous();
        }
        public void remove() {
            itr.remove();
        }
    }

    @SuppressWarnings("unchecked")
    private LinkedList<E> superClone() {
        try {
            return (LinkedList<E>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    /**
     * 返回此 {@code LinkedList} 的浅表副本。（元素本身不会被克隆。）
     *
     * @return 此 {@code LinkedList} 实例的浅表副本
     */
    public Object clone() {
        LinkedList<E> clone = superClone();

        // 将克隆放入“原始”状态
        clone.first = clone.last = null;
        clone.size = 0;
        clone.modCount = 0;

        // 用我们的元素初始化克隆
        for (Node<E> x = first; x != null; x = x.next)
            clone.add(x.item);

        return clone;
    }

    /**
     * 按适当顺序（从第一个元素到最后一个元素）返回包含此列表中所有元素的数组。
     *
     * <p>返回的数组将是“安全的”，因为此列表不维护对它的任何引用。
     * （换句话说，此方法必须分配一个新数组）。因此，调用者可以自由修改返回的数组。
     *
     * <p>此方法充当基于数组和基于集合的 API 之间的桥梁。
     *
     * @return 按适当顺序包含此列表中所有元素的数组
     */
    public Object[] toArray() {
        Object[] result = new Object[size];
        int i = 0;
        for (Node<E> x = first; x != null; x = x.next)
            result[i++] = x.item;
        return result;
    }

    /**
     * 按适当顺序（从第一个元素到最后一个元素）返回包含此列表中所有元素的数组；
     * 返回数组的运行时类型是指定数组的运行时类型。如果列表适合指定的数组，则在其中返回。
     * 否则，将使用指定数组的运行时类型和此列表的大小分配一个新数组。
     *
     * <p>如果列表适合指定的数组并有剩余空间（即数组的元素比列表多），
     * 则紧接列表末尾之后的数组元素将设置为 {@code null}。
     * （这仅在调用者知道列表不包含任何 null 元素时才可用于确定列表的长度。）
     *
     * <p>与 {@link #toArray()} 方法一样，此方法充当基于数组和基于集合的 API 之间的桥梁。
     * 此外，此方法允许精确控制输出数组的运行时类型，并且在某些情况下可以用于节省分配成本。
     *
     * <p>假设 {@code x} 是一个已知只包含字符串的列表。以下代码可用于将列表转储到新分配的 {@code String} 数组中：
     *
     * <pre>
     *     String[] y = x.toArray(new String[0]);</pre>
     *
     * 注意，{@code toArray(new Object[0])} 在功能上与 {@code toArray()} 相同。
     *
     * @param a 如果足够大的话，要存储列表元素的数组；否则，将为此分配一个相同运行时类型的新数组。
     * @return 包含列表元素的数组
     * @throws ArrayStoreException 如果指定数组的运行时类型不是此列表中每个元素的运行时类型的超类型
     * @throws NullPointerException 如果指定的数组为 null
     */
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        if (a.length < size)
            a = (T[])java.lang.reflect.Array.newInstance(
                    a.getClass().getComponentType(), size);
        int i = 0;
        Object[] result = a;
        for (Node<E> x = first; x != null; x = x.next)
            result[i++] = x.item;

        if (a.length > size)
            a[size] = null;

        return a;
    }

    private static final long serialVersionUID = 876323262645176354L;

    /**
     * 将此 {@code LinkedList} 实例的状态保存到流中（即序列化它）。
     *
     * @serialData 发出列表的大小（它包含的元素数）（int），然后按适当顺序发出所有元素（每个都是一个 Object）。
     */
    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException {
        // 写出任何隐藏的序列化魔法
        s.defaultWriteObject();

        // 写出大小
        s.writeInt(size);

        // 按适当顺序写出所有元素
        for (Node<E> x = first; x != null; x = x.next)
            s.writeObject(x.item);
    }

    /**
     * 从流中重建此 {@code LinkedList} 实例（即反序列化）。
     */
    @SuppressWarnings("unchecked")
    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException {
        // 读入任何隐藏的序列化魔法
        s.defaultReadObject();

        // 读入大小
        int size = s.readInt();

        // 按适当顺序读入所有元素
        for (int i = 0; i < size; i++)
            linkLast((E)s.readObject());
    }

    /**
     * 在此列表中的元素上创建 <em><a href="Spliterator.html#binding">延迟绑定</a></em> 和 <em>快速失败</em> 的 {@link Spliterator}。
     *
     * <p>{@code Spliterator} 报告 {@link Spliterator#SIZED} 和 {@link Spliterator#ORDERED}。
     * 覆盖实现应记录附加特征值的报告。
     *
     * @implNote
     * {@code Spliterator} 还报告 {@link Spliterator#SUBSIZED} 并实现 {@code trySplit} 以允许有限的并行。
     *
     * @return 此列表中元素的 {@code Spliterator}
     * @since 1.8
     */
    @Override
    public Spliterator<E> spliterator() {
        return new LLSpliterator<E>(this, -1, 0);
    }

    /** Spliterators.IteratorSpliterator 的自定义变体 */
    static final class LLSpliterator<E> implements Spliterator<E> {
        static final int BATCH_UNIT = 1 << 10;  // 批处理数组大小增量
        static final int MAX_BATCH = 1 << 25;   // 最大批处理数组大小
        final LinkedList<E> list; // null 除非被遍历，否则可以接受
        Node<E> current;      // 当前节点；在初始化之前为 null
        int est;              // 大小估计；在首次需要之前为 -1
        int expectedModCount; // 在设置 est 时初始化
        int batch;            // 拆分的批处理大小

        LLSpliterator(LinkedList<E> list, int est, int expectedModCount) {
            this.list = list;
            this.est = est;
            this.expectedModCount = expectedModCount;
        }

        final int getEst() {
            int s; // 强制初始化
            final LinkedList<E> lst;
            if ((s = est) < 0) {
                if ((lst = list) == null)
                    s = est = 0;
                else {
                    expectedModCount = lst.modCount;
                    current = lst.first;
                    s = est = lst.size;
                }
            }
            return s;
        }

        public long estimateSize() { return (long) getEst(); }

        public Spliterator<E> trySplit() {
            Node<E> p;
            int s = getEst();
            if (s > 1 && (p = current) != null) {
                int n = batch + BATCH_UNIT;
                if (n > s)
                    n = s;
                if (n > MAX_BATCH)
                    n = MAX_BATCH;
                Object[] a = new Object[n];
                int j = 0;
                do { a[j++] = p.item; } while ((p = p.next) != null && j < n);
                current = p;
                batch = j;
                est = s - j;
                return Spliterators.spliterator(a, 0, j, Spliterator.ORDERED);
            }
            return null;
        }

        public void forEachRemaining(Consumer<? super E> action) {
            Node<E> p; int n;
            if (action == null) throw new NullPointerException();
            if ((n = getEst()) > 0 && (p = current) != null) {
                current = null;
                est = 0;
                do {
                    E e = p.item;
                    p = p.next;
                    action.accept(e);
                } while (p != null && --n > 0);
            }
            if (list.modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }

        public boolean tryAdvance(Consumer<? super E> action) {
            Node<E> p;
            if (action == null) throw new NullPointerException();
            if (getEst() > 0 && (p = current) != null) {
                --est;
                E e = p.item;
                current = p.next;
                action.accept(e);
                if (list.modCount != expectedModCount)
                    throw new ConcurrentModificationException();
                return true;
            }
            return false;
        }

        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
        }
    }

}