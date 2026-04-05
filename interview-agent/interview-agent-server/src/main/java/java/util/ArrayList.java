package java.util;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
// 移除无法解析的 sun.misc.SharedSecrets 导入

/**
 * {@code ArrayList} 是 {@link List} 接口的可调整大小的数组实现。
 * 实现了所有可选的列表操作，并允许所有元素，包括 {@code null}。
 * 除了实现 {@code List} 接口外，此类还提供了一些方法来操作内部存储列表的数组大小。
 * （此类大致相当于 {@code Vector}，但它是不同步的。）
 *
 * <p>{@code size}、{@code isEmpty}、{@code get}、{@code set}、{@code iterator}
 * 和 {@code listIterator} 操作在常数时间内完成。
 * {@code add} 操作在<i>摊销常数时间</i>内运行，也就是说，添加 n 个元素需要 O(n) 时间。
 * 所有其他操作在线性时间内运行（大致来说）。与 {@code LinkedList} 实现相比，常数因子较低。
 *
 * <p>每个 {@code ArrayList} 实例都有一个<i>容量</i>。容量是用于存储列表中元素的数组的大小。
 * 它总是至少与列表大小一样大。随着元素添加到 ArrayList，其容量会自动增长。
 * 增长策略的细节没有具体说明，除了添加元素具有恒定的摊销时间成本。
 *
 * <p>应用程序可以在添加大量元素之前使用 {@link #ensureCapacity} 操作增加 {@code ArrayList} 实例的容量。
 * 这可能会减少增量重新分配的次数。
 *
 * <p><strong>请注意，此实现不是同步的。</strong>
 * 如果多个线程同时访问一个 {@code ArrayList} 实例，并且至少有一个线程在结构上修改了列表，
 * 则<i>必须</i>在外部进行同步。（结构修改是指添加或删除一个或多个元素，或显式调整后备数组大小的任何操作；
 * 仅设置元素的值不是结构修改。）这通常通过同步一些自然封装列表的对象来完成。
 *
 * 如果不存在这样的对象，则应使用 {@link Collections#synchronizedList Collections.synchronizedList}
 * 方法“包装”列表。最好在创建时执行此操作，以防止意外地不同步访问列表：
 * <pre>
 *   List list = Collections.synchronizedList(new ArrayList(...));</pre>
 *
 * <p><a name="fail-fast">
 * 此类的 {@link #iterator() iterator} 和 {@link #listIterator(int) listIterator} 方法返回的迭代器是
 * <em>快速失败</em>的：</a>如果在创建迭代器之后的任何时间对列表进行结构修改，除非通过迭代器自己的
 * {@link ListIterator#remove() remove} 或 {@link ListIterator#add(Object) add} 方法，
 * 否则迭代器将抛出 {@link ConcurrentModificationException}。因此，在面对并发修改时，
 * 迭代器会快速而干净地失败，而不是在未来不确定的时间冒任意、不确定行为的风险。
 *
 * <p>请注意，迭代器的快速失败行为无法得到保证，因为通常来说，在存在不同步的并发修改的情况下，
 * 不可能做出任何硬性保证。快速失败迭代器会尽最大努力抛出 {@code ConcurrentModificationException}。
 * 因此，编写依赖于此异常的程序是错误的：<i>迭代器的快速失败行为应该仅用于检测错误。</i>
 *
 * <p>此类是 <a href="{@docRoot}/../technotes/guides/collections/index.html">Java 集合框架</a> 的成员。
 *
 * @author  Josh Bloch
 * @author  Neal Gafter
 * @see     Collection
 * @see     List
 * @see     LinkedList
 * @see     Vector
 * @since   1.2
 */
public class ArrayList<E> extends AbstractList<E>
        implements List<E>, RandomAccess, Cloneable, java.io.Serializable {
    private static final long serialVersionUID = 8683452581122892189L;

    /**
     * 默认初始容量。
     */
    private static final int DEFAULT_CAPACITY = 10;

    /**
     * 用于空实例的共享空数组实例。
     */
    private static final Object[] EMPTY_ELEMENTDATA = {};

    /**
     * 用于默认大小的空实例的共享空数组实例。
     * 我们将其与 EMPTY_ELEMENTDATA 区分开来，以了解添加第一个元素时需要扩容多少。
     */
    private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};

    /**
     * 存储 ArrayList 元素的数组缓冲区。
     * ArrayList 的容量是该数组缓冲区的长度。任何带有
     * elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA 的空 ArrayList
     * 将在添加第一个元素时扩容到 DEFAULT_CAPACITY。
     *
     * 非私有以简化嵌套类的访问。
     */
    transient Object[] elementData;

    /**
     * ArrayList 的大小（它包含的元素数量）。
     *
     * @serial
     */
    private int size;

    /**
     * 构造一个具有指定初始容量的空列表。
     *
     * @param  initialCapacity  列表的初始容量
     * @throws IllegalArgumentException 如果指定的初始容量为负数
     */
    public ArrayList(int initialCapacity) {
        if (initialCapacity > 0) {
            this.elementData = new Object[initialCapacity];
        } else if (initialCapacity == 0) {
            this.elementData = EMPTY_ELEMENTDATA;
        } else {
            throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
        }
    }

    /**
     * 构造一个初始容量为 10 的空列表。
     */
    public ArrayList() {
        this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
    }

    /**
     * 按照指定集合的迭代器返回的顺序构造一个包含指定集合元素的列表。
     *
     * @param c 其元素将放入此列表的集合
     * @throws NullPointerException 如果指定的集合为 null
     */
    public ArrayList(Collection<? extends E> c) {
        Object[] a = c.toArray();
        if ((size = a.length) != 0) {
            if (c.getClass() == ArrayList.class) {
                elementData = a; // 如果是 ArrayList 类型，直接使用其内部数组
            } else {
                // 其他集合类型，复制为 Object[] 类型
                elementData = Arrays.copyOf(a, size, Object[].class);
            }
        } else {
            // 空集合，替换为空数组
            elementData = EMPTY_ELEMENTDATA;
        }
    }

    /**
     * 将此 ArrayList 实例的容量修剪为列表的当前大小。
     * 应用程序可以使用此操作来最小化 ArrayList 实例的存储空间。
     */
    public void trimToSize() {
        modCount++; // 记录结构性修改次数
        if (size < elementData.length) {
            elementData = (size == 0)
                    ? EMPTY_ELEMENTDATA
                    : Arrays.copyOf(elementData, size);
        }
    }

    /**
     * 如有必要，增加此 ArrayList 实例的容量，以确保它至少可以容纳由最小容量参数指定的元素数。
     *
     * @param   minCapacity   所需的最小容量
     */
    public void ensureCapacity(int minCapacity) {
        // 如果 elementData 不是默认的空表，则 minExpand 为 0；否则为 DEFAULT_CAPACITY
        int minExpand = (elementData != DEFAULTCAPACITY_EMPTY_ELEMENTDATA)
                ? 0
                : DEFAULT_CAPACITY;
        if (minCapacity > minExpand) {
            ensureExplicitCapacity(minCapacity);
        }
    }

    /**
     * 计算容量：如果使用默认空表，则取 max(DEFAULT_CAPACITY, minCapacity)，否则直接返回 minCapacity。
     */
    private static int calculateCapacity(Object[] elementData, int minCapacity) {
        if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
            return Math.max(DEFAULT_CAPACITY, minCapacity);
        }
        return minCapacity;
    }

    /**
     * 内部确保容量方法，不对外暴露。
     * 调用 calculateCapacity 得到真正需要的容量，然后调用 ensureExplicitCapacity。
     */
    private void ensureCapacityInternal(int minCapacity) {
        ensureExplicitCapacity(calculateCapacity(elementData, minCapacity));
    }

    /**
     * 显式容量确保，如果所需容量大于当前数组长度，则扩容。
     */
    private void ensureExplicitCapacity(int minCapacity) {
        modCount++; // 结构性修改计数增加

        // 如果所需最小容量大于当前数组长度，则扩容
        if (minCapacity - elementData.length > 0)
            grow(minCapacity);
    }

    /**
     * 要分配的数组的最大大小。一些 VM 会在数组中保留一些头字。
     * 尝试分配更大的数组可能会导致 OutOfMemoryError：请求的数组大小超过 VM 限制。
     */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    /**
     * 增加容量，以确保它至少可以容纳由最小容量参数指定的元素数。
     *
     * @param minCapacity 所需的最小容量
     */
    private void grow(int minCapacity) {
        // 注意：这里可能溢出
        int oldCapacity = elementData.length;
        // 新容量 = 旧容量 + 旧容量/2（即增长 50%）
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
        // minCapacity 通常接近 size，因此这是一个优化
        elementData = Arrays.copyOf(elementData, newCapacity);
    }

    /**
     * 为非常大的容量处理溢出情况。
     */
    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // 溢出
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ? Integer.MAX_VALUE : MAX_ARRAY_SIZE;
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
     * 如果此列表不包含任何元素，则返回 true。
     *
     * @return 如果此列表不包含任何元素，则返回 true
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * 如果此列表包含指定元素，则返回 true。
     * 更正式地说，当且仅当此列表包含至少一个满足 (o==null ? e==null : o.equals(e)) 的元素 e 时返回 true。
     *
     * @param o 要测试在此列表中是否存在的元素
     * @return 如果此列表包含指定元素，则返回 true
     */
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

    /**
     * 返回此列表中指定元素的第一次出现的索引，如果此列表不包含该元素，则返回 -1。
     * 更正式地说，返回最低索引 i，使得 (o==null ? get(i)==null : o.equals(get(i)))，
     * 如果没有这样的索引，则返回 -1。
     */
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
     * 返回此列表中指定元素的最后一次出现的索引，如果此列表不包含该元素，则返回 -1。
     * 更正式地说，返回最高索引 i，使得 (o==null ? get(i)==null : o.equals(get(i)))，
     * 如果没有这样的索引，则返回 -1。
     */
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
     * 返回此 ArrayList 实例的浅表副本。（元素本身不会被复制。）
     *
     * @return 此 ArrayList 实例的克隆
     */
    public Object clone() {
        try {
            ArrayList<?> v = (ArrayList<?>) super.clone();
            v.elementData = Arrays.copyOf(elementData, size);
            v.modCount = 0;
            return v;
        } catch (CloneNotSupportedException e) {
            // 这不应该发生，因为我们实现了 Cloneable
            throw new InternalError(e);
        }
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
        return Arrays.copyOf(elementData, size);
    }

    /**
     * 按适当顺序（从第一个元素到最后一个元素）返回包含此列表中所有元素的数组；
     * 返回数组的运行时类型是指定数组的运行时类型。如果列表适合指定的数组，则在其中返回。
     * 否则，将使用指定数组的运行时类型和此列表的大小分配一个新数组。
     *
     * <p>如果列表适合指定的数组并有剩余空间（即数组的元素比列表多），
     * 则紧接列表末尾之后的数组元素将设置为 null。
     * （这仅在调用者知道列表不包含任何 null 元素时才可用于确定列表的长度。）
     *
     * @param a 如果足够大的话，要存储列表元素的数组；否则，将为此分配一个相同运行时类型的新数组。
     * @return 包含列表元素的数组
     * @throws ArrayStoreException 如果指定数组的运行时类型不是此列表中每个元素的运行时类型的超类型
     * @throws NullPointerException 如果指定的数组为 null
     */
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        if (a.length < size)
            // 创建一个新的数组，运行时类型为 a 的类型，但内容为本列表元素
            return (T[]) Arrays.copyOf(elementData, size, a.getClass());
        System.arraycopy(elementData, 0, a, 0, size);
        if (a.length > size)
            a[size] = null;
        return a;
    }

    // 位置访问操作

    @SuppressWarnings("unchecked")
    E elementData(int index) {
        return (E) elementData[index];
    }

    /**
     * 返回此列表中指定位置的元素。
     *
     * @param  index 要返回的元素的索引
     * @return 此列表中指定位置的元素
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public E get(int index) {
        rangeCheck(index);
        return elementData(index);
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
        rangeCheck(index);
        E oldValue = elementData(index);
        elementData[index] = element;
        return oldValue;
    }

    /**
     * 将指定元素追加到此列表的末尾。
     *
     * @param e 要追加到此列表的元素
     * @return true（根据 {@link Collection#add} 的规定）
     */
    public boolean add(E e) {
        ensureCapacityInternal(size + 1);  // 增加 modCount
        elementData[size++] = e;
        return true;
    }

    /**
     * 在此列表中的指定位置插入指定元素。将当前位于该位置的元素（如果有）和任何后续元素向右移动（将其索引增加 1）。
     *
     * @param index 要插入指定元素的索引
     * @param element 要插入的元素
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public void add(int index, E element) {
        rangeCheckForAdd(index);
        ensureCapacityInternal(size + 1);  // 增加 modCount
        System.arraycopy(elementData, index, elementData, index + 1,
                size - index);
        elementData[index] = element;
        size++;
    }

    /**
     * 删除此列表中指定位置的元素。将所有后续元素向左移动（将其索引减 1）。
     *
     * @param index 要删除的元素的索引
     * @return 从列表中删除的元素
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public E remove(int index) {
        rangeCheck(index);
        modCount++;
        E oldValue = elementData(index);
        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(elementData, index + 1, elementData, index,
                    numMoved);
        elementData[--size] = null; // 清除引用，让 GC 工作
        return oldValue;
    }

    /**
     * 如果存在，则从此列表中删除指定元素的第一个出现。如果列表不包含该元素，则不变。
     * 更正式地说，删除具有最低索引 i 的元素，使得 (o==null ? get(i)==null : o.equals(get(i)))
     * （如果存在这样的元素）。如果此列表包含指定元素（或者等效地，如果此列表因调用而更改），则返回 true。
     *
     * @param o 如果存在，要从此列表中删除的元素
     * @return 如果此列表包含指定元素，则返回 true
     */
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

    /*
     * 私有删除方法，跳过边界检查，不返回删除的值。
     */
    private void fastRemove(int index) {
        modCount++;
        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(elementData, index + 1, elementData, index,
                    numMoved);
        elementData[--size] = null; // 清除引用，让 GC 工作
    }

    /**
     * 从此列表中删除所有元素。此调用返回后，列表将为空。
     */
    public void clear() {
        modCount++;
        // 清除引用，让 GC 工作
        for (int i = 0; i < size; i++)
            elementData[i] = null;
        size = 0;
    }

    /**
     * 按指定集合的迭代器返回的顺序将指定集合中的所有元素追加到此列表的末尾。
     * 如果在操作进行中修改了指定的集合，则此操作的行为未定义。（这意味着如果指定的集合是此列表并且此列表非空，则此调用的行为未定义。）
     *
     * @param c 包含要添加到此列表的元素的集合
     * @return 如果此列表因调用而更改，则返回 true
     * @throws NullPointerException 如果指定的集合为 null
     */
    public boolean addAll(Collection<? extends E> c) {
        Object[] a = c.toArray();
        int numNew = a.length;
        ensureCapacityInternal(size + numNew);  // 增加 modCount
        System.arraycopy(a, 0, elementData, size, numNew);
        size += numNew;
        return numNew != 0;
    }

    /**
     * 从指定位置开始，将指定集合中的所有元素插入此列表。将当前位于该位置的元素（如果有）和任何后续元素向右移动（增加其索引）。
     * 新元素将按照指定集合的迭代器返回的顺序出现在列表中。
     *
     * @param index 从指定集合插入第一个元素的索引
     * @param c 包含要添加到此列表的元素的集合
     * @return 如果此列表因调用而更改，则返回 true
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @throws NullPointerException 如果指定的集合为 null
     */
    public boolean addAll(int index, Collection<? extends E> c) {
        rangeCheckForAdd(index);
        Object[] a = c.toArray();
        int numNew = a.length;
        ensureCapacityInternal(size + numNew);  // 增加 modCount
        int numMoved = size - index;
        if (numMoved > 0)
            System.arraycopy(elementData, index, elementData, index + numNew,
                    numMoved);
        System.arraycopy(a, 0, elementData, index, numNew);
        size += numNew;
        return numNew != 0;
    }

    /**
     * 从此列表中删除索引在 {@code fromIndex}（包含）和 {@code toIndex}（不包含）之间的所有元素。
     * 将所有后续元素向左移动（减少其索引）。此调用将列表缩短 {@code (toIndex - fromIndex)} 个元素。
     * （如果 {@code toIndex == fromIndex}，则此操作无效。）
     *
     * @throws IndexOutOfBoundsException 如果 {@code fromIndex} 或 {@code toIndex} 超出范围
     *         ({@code fromIndex < 0 ||
     *          fromIndex >= size() ||
     *          toIndex > size() ||
     *          toIndex < fromIndex})
     */
    protected void removeRange(int fromIndex, int toIndex) {
        modCount++;
        int numMoved = size - toIndex;
        System.arraycopy(elementData, toIndex, elementData, fromIndex,
                numMoved);
        // 清除引用，让 GC 工作
        int newSize = size - (toIndex - fromIndex);
        for (int i = newSize; i < size; i++) {
            elementData[i] = null;
        }
        size = newSize;
    }

    /**
     * 检查给定索引是否在范围内。如果不在范围内，则抛出适当的运行时异常。
     * 此方法*不*检查索引是否为负数：它总是在数组访问之前立即使用，如果索引为负数，则会抛出 ArrayIndexOutOfBoundsException。
     */
    private void rangeCheck(int index) {
        if (index >= size)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    /**
     * add 和 addAll 使用的 rangeCheck 版本。
     */
    private void rangeCheckForAdd(int index) {
        if (index > size || index < 0)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    /**
     * 构造 IndexOutOfBoundsException 的详细消息。
     * 在可能的错误处理代码重构中，这种“提取”方式在服务器和客户端 VM 中表现最佳。
     */
    private String outOfBoundsMsg(int index) {
        return "Index: " + index + ", Size: " + size;
    }

    /**
     * 从此列表中删除所有包含在指定集合中的元素。
     *
     * @param c 包含要从列表中删除的元素的集合
     * @return 如果此列表因调用而更改，则返回 true
     * @throws ClassCastException 如果此列表的元素类与指定集合不兼容
     * @throws NullPointerException 如果此列表包含 null 元素且指定集合不允许 null 元素，
     *         或者如果指定的集合为 null
     * @see Collection#contains(Object)
     */
    public boolean removeAll(Collection<?> c) {
        Objects.requireNonNull(c);
        return batchRemove(c, false);
    }

    /**
     * 仅保留此列表中包含在指定集合中的元素。换句话说，从此列表中删除所有未包含在指定集合中的元素。
     *
     * @param c 包含要保留在此列表中的元素的集合
     * @return 如果此列表因调用而更改，则返回 true
     * @throws ClassCastException 如果此列表的元素类与指定集合不兼容
     * @throws NullPointerException 如果此列表包含 null 元素且指定集合不允许 null 元素，
     *         或者如果指定的集合为 null
     * @see Collection#contains(Object)
     */
    public boolean retainAll(Collection<?> c) {
        Objects.requireNonNull(c);
        return batchRemove(c, true);
    }

    /**
     * 批量删除或保留操作的通用实现。
     * @param c 集合
     * @param complement true 表示保留（retain），false 表示删除（remove）
     */
    private boolean batchRemove(Collection<?> c, boolean complement) {
        final Object[] elementData = this.elementData;
        int r = 0, w = 0;
        boolean modified = false;
        try {
            for (; r < size; r++)
                if (c.contains(elementData[r]) == complement)
                    elementData[w++] = elementData[r];
        } finally {
            // 即使 c.contains() 抛出异常，也要保持与 AbstractCollection 的行为兼容
            if (r != size) {
                System.arraycopy(elementData, r,
                        elementData, w,
                        size - r);
                w += size - r;
            }
            if (w != size) {
                // 清除引用，让 GC 工作
                for (int i = w; i < size; i++)
                    elementData[i] = null;
                modCount += size - w;
                size = w;
                modified = true;
            }
        }
        return modified;
    }

    /**
     * 将 {@code ArrayList} 实例的状态保存到流中（即序列化它）。
     *
     * @serialData 发出支持 {@code ArrayList} 实例的数组长度（int），然后按适当顺序发出所有元素（每个都是一个 {@code Object}）。
     */
    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException {
        // 写出元素计数和任何隐藏内容
        int expectedModCount = modCount;
        s.defaultWriteObject();

        // 写出大小作为容量，以与 clone() 的行为兼容
        s.writeInt(size);

        // 按适当顺序写出所有元素
        for (int i = 0; i < size; i++) {
            s.writeObject(elementData[i]);
        }

        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
    }

    /**
     * 从流中重建 {@code ArrayList} 实例（即反序列化）。
     */
    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException {
        elementData = EMPTY_ELEMENTDATA;

        // 读入大小和任何隐藏内容
        s.defaultReadObject();

        // 读入容量（忽略）
        s.readInt();

        if (size > 0) {
            // 像 clone() 一样，根据大小而不是容量分配数组
            int capacity = calculateCapacity(elementData, size);
            // 移除 SharedSecrets 调用，直接跳过数组长度检查
            // 如果后续需要安全性检查，可在此手动验证 capacity 是否合理
            ensureCapacityInternal(size);

            Object[] a = elementData;
            // 按适当顺序读入所有元素
            for (int i = 0; i < size; i++) {
                a[i] = s.readObject();
            }
        }
    }

    /**
     * 从列表中的指定位置开始，返回此列表中元素的列表迭代器（按适当顺序）。
     * 指定的索引指示 {@link ListIterator#next next} 的初始调用将返回的第一个元素。
     * {@link ListIterator#previous previous} 的初始调用将返回具有指定索引减 1 的元素。
     *
     * <p>返回的列表迭代器是 <a href="#fail-fast"><i>快速失败</i></a> 的。
     *
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public ListIterator<E> listIterator(int index) {
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException("Index: " + index);
        return new ListItr(index);
    }

    /**
     * 返回此列表中元素的列表迭代器（按适当顺序）。
     *
     * <p>返回的列表迭代器是 <a href="#fail-fast"><i>快速失败</i></a> 的。
     *
     * @see #listIterator(int)
     */
    public ListIterator<E> listIterator() {
        return new ListItr(0);
    }

    /**
     * 按适当顺序返回此列表中元素的迭代器。
     *
     * <p>返回的迭代器是 <a href="#fail-fast"><i>快速失败</i></a> 的。
     *
     * @return 按适当顺序对此列表中元素的迭代器
     */
    public Iterator<E> iterator() {
        return new Itr();
    }

    /**
     * AbstractList.Itr 的优化版本。
     */
    private class Itr implements Iterator<E> {
        int cursor;       // 要返回的下一个元素的索引
        int lastRet = -1; // 最后返回的元素的索引；如果没有则为 -1
        int expectedModCount = modCount; // 期望的修改计数，用于快速失败检查

        Itr() {}

        public boolean hasNext() {
            return cursor != size;
        }

        @SuppressWarnings("unchecked")
        public E next() {
            checkForComodification();
            int i = cursor;
            if (i >= size)
                throw new NoSuchElementException();
            Object[] elementData = ArrayList.this.elementData;
            if (i >= elementData.length)
                throw new ConcurrentModificationException();
            cursor = i + 1;
            return (E) elementData[lastRet = i];
        }

        public void remove() {
            if (lastRet < 0)
                throw new IllegalStateException();
            checkForComodification();

            try {
                ArrayList.this.remove(lastRet);
                cursor = lastRet;
                lastRet = -1;
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public void forEachRemaining(Consumer<? super E> consumer) {
            Objects.requireNonNull(consumer);
            final int size = ArrayList.this.size;
            int i = cursor;
            if (i >= size) {
                return;
            }
            final Object[] elementData = ArrayList.this.elementData;
            if (i >= elementData.length) {
                throw new ConcurrentModificationException();
            }
            while (i != size && modCount == expectedModCount) {
                consumer.accept((E) elementData[i++]);
            }
            // 在迭代结束时更新一次，以减少堆写入流量
            cursor = i;
            lastRet = i - 1;
            checkForComodification();
        }

        final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }

    /**
     * AbstractList.ListItr 的优化版本。
     */
    private class ListItr extends Itr implements ListIterator<E> {
        ListItr(int index) {
            super();
            cursor = index;
        }

        public boolean hasPrevious() {
            return cursor != 0;
        }

        public int nextIndex() {
            return cursor;
        }

        public int previousIndex() {
            return cursor - 1;
        }

        @SuppressWarnings("unchecked")
        public E previous() {
            checkForComodification();
            int i = cursor - 1;
            if (i < 0)
                throw new NoSuchElementException();
            Object[] elementData = ArrayList.this.elementData;
            if (i >= elementData.length)
                throw new ConcurrentModificationException();
            cursor = i;
            return (E) elementData[lastRet = i];
        }

        public void set(E e) {
            if (lastRet < 0)
                throw new IllegalStateException();
            checkForComodification();

            try {
                ArrayList.this.set(lastRet, e);
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

        public void add(E e) {
            checkForComodification();

            try {
                int i = cursor;
                ArrayList.this.add(i, e);
                cursor = i + 1;
                lastRet = -1;
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }
    }

    /**
     * 返回此列表中介于 {@code fromIndex}（包含）和 {@code toIndex}（不包含）之间的部分的视图。
     * （如果 {@code fromIndex} 和 {@code toIndex} 相等，则返回的列表为空。）
     * 返回的列表由此列表支持，因此返回列表中的非结构更改会反映在此列表中，反之亦然。
     * 返回的列表支持所有可选的列表操作。
     *
     * <p>此方法消除了对显式范围操作的需要（通常为数组存在的此类操作）。
     * 任何期望列表的操作都可以通过传递子列表视图而不是整个列表来用作范围操作。
     * 例如，以下惯用法从列表中删除一系列元素：
     * <pre>
     *      list.subList(from, to).clear();
     * </pre>
     * 可以为 {@link #indexOf(Object)} 和 {@link #lastIndexOf(Object)} 构造类似的惯用法，
     * 并且 {@link Collections} 类中的所有算法都可以应用于子列表。
     *
     * <p>如果通过返回列表以外的任何方式对后备列表（即此列表）进行<i>结构修改</i>，则此方法返回的列表的语义将变得未定义。
     * （结构修改是那些改变此列表大小的修改，或者以其他方式扰乱它，以至于进行中的迭代可能会产生不正确的结果。）
     *
     * @throws IndexOutOfBoundsException {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public List<E> subList(int fromIndex, int toIndex) {
        subListRangeCheck(fromIndex, toIndex, size);
        return new SubList(this, 0, fromIndex, toIndex);
    }

    static void subListRangeCheck(int fromIndex, int toIndex, int size) {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
        if (toIndex > size)
            throw new IndexOutOfBoundsException("toIndex = " + toIndex);
        if (fromIndex > toIndex)
            throw new IllegalArgumentException("fromIndex(" + fromIndex +
                    ") > toIndex(" + toIndex + ")");
    }

    /**
     * 子列表实现，支持对父列表的部分视图操作。
     */
    private class SubList extends AbstractList<E> implements RandomAccess {
        private final AbstractList<E> parent;
        private final int parentOffset;
        private final int offset;
        int size;

        SubList(AbstractList<E> parent,
                int offset, int fromIndex, int toIndex) {
            this.parent = parent;
            this.parentOffset = fromIndex;
            this.offset = offset + fromIndex;
            this.size = toIndex - fromIndex;
            this.modCount = ArrayList.this.modCount;
        }

        public E set(int index, E e) {
            rangeCheck(index);
            checkForComodification();
            E oldValue = ArrayList.this.elementData(offset + index);
            ArrayList.this.elementData[offset + index] = e;
            return oldValue;
        }

        public E get(int index) {
            rangeCheck(index);
            checkForComodification();
            return ArrayList.this.elementData(offset + index);
        }

        public int size() {
            checkForComodification();
            return this.size;
        }

        public void add(int index, E e) {
            rangeCheckForAdd(index);
            checkForComodification();
            parent.add(parentOffset + index, e);
            this.modCount = parent.modCount;
            this.size++;
        }

        public E remove(int index) {
            rangeCheck(index);
            checkForComodification();
            E result = parent.remove(parentOffset + index);
            this.modCount = parent.modCount;
            this.size--;
            return result;
        }

        protected void removeRange(int fromIndex, int toIndex) {
            checkForComodification();
            parent.removeRange(parentOffset + fromIndex,
                    parentOffset + toIndex);
            this.modCount = parent.modCount;
            this.size -= toIndex - fromIndex;
        }

        public boolean addAll(Collection<? extends E> c) {
            return addAll(this.size, c);
        }

        public boolean addAll(int index, Collection<? extends E> c) {
            rangeCheckForAdd(index);
            int cSize = c.size();
            if (cSize == 0)
                return false;

            checkForComodification();
            parent.addAll(parentOffset + index, c);
            this.modCount = parent.modCount;
            this.size += cSize;
            return true;
        }

        public Iterator<E> iterator() {
            return listIterator();
        }

        public ListIterator<E> listIterator(final int index) {
            checkForComodification();
            rangeCheckForAdd(index);
            final int offset = this.offset;

            return new ListIterator<E>() {
                int cursor = index;
                int lastRet = -1;
                int expectedModCount = ArrayList.this.modCount;

                public boolean hasNext() {
                    return cursor != SubList.this.size;
                }

                @SuppressWarnings("unchecked")
                public E next() {
                    checkForComodification();
                    int i = cursor;
                    if (i >= SubList.this.size)
                        throw new NoSuchElementException();
                    Object[] elementData = ArrayList.this.elementData;
                    if (offset + i >= elementData.length)
                        throw new ConcurrentModificationException();
                    cursor = i + 1;
                    return (E) elementData[offset + (lastRet = i)];
                }

                public boolean hasPrevious() {
                    return cursor != 0;
                }

                @SuppressWarnings("unchecked")
                public E previous() {
                    checkForComodification();
                    int i = cursor - 1;
                    if (i < 0)
                        throw new NoSuchElementException();
                    Object[] elementData = ArrayList.this.elementData;
                    if (offset + i >= elementData.length)
                        throw new ConcurrentModificationException();
                    cursor = i;
                    return (E) elementData[offset + (lastRet = i)];
                }

                @SuppressWarnings("unchecked")
                public void forEachRemaining(Consumer<? super E> consumer) {
                    Objects.requireNonNull(consumer);
                    final int size = SubList.this.size;
                    int i = cursor;
                    if (i >= size) {
                        return;
                    }
                    final Object[] elementData = ArrayList.this.elementData;
                    if (offset + i >= elementData.length) {
                        throw new ConcurrentModificationException();
                    }
                    while (i != size && modCount == expectedModCount) {
                        consumer.accept((E) elementData[offset + (i++)]);
                    }
                    // 在迭代结束时更新一次
                    lastRet = cursor = i;
                    checkForComodification();
                }

                public int nextIndex() {
                    return cursor;
                }

                public int previousIndex() {
                    return cursor - 1;
                }

                public void remove() {
                    if (lastRet < 0)
                        throw new IllegalStateException();
                    checkForComodification();

                    try {
                        SubList.this.remove(lastRet);
                        cursor = lastRet;
                        lastRet = -1;
                        expectedModCount = ArrayList.this.modCount;
                    } catch (IndexOutOfBoundsException ex) {
                        throw new ConcurrentModificationException();
                    }
                }

                public void set(E e) {
                    if (lastRet < 0)
                        throw new IllegalStateException();
                    checkForComodification();

                    try {
                        ArrayList.this.set(offset + lastRet, e);
                    } catch (IndexOutOfBoundsException ex) {
                        throw new ConcurrentModificationException();
                    }
                }

                public void add(E e) {
                    checkForComodification();

                    try {
                        int i = cursor;
                        SubList.this.add(i, e);
                        cursor = i + 1;
                        lastRet = -1;
                        expectedModCount = ArrayList.this.modCount;
                    } catch (IndexOutOfBoundsException ex) {
                        throw new ConcurrentModificationException();
                    }
                }

                final void checkForComodification() {
                    if (expectedModCount != ArrayList.this.modCount)
                        throw new ConcurrentModificationException();
                }
            };
        }

        public List<E> subList(int fromIndex, int toIndex) {
            subListRangeCheck(fromIndex, toIndex, size);
            return new SubList(this, offset, fromIndex, toIndex);
        }

        private void rangeCheck(int index) {
            if (index < 0 || index >= this.size)
                throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }

        private void rangeCheckForAdd(int index) {
            if (index < 0 || index > this.size)
                throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }

        private String outOfBoundsMsg(int index) {
            return "Index: " + index + ", Size: " + this.size;
        }

        private void checkForComodification() {
            if (ArrayList.this.modCount != this.modCount)
                throw new ConcurrentModificationException();
        }

        public Spliterator<E> spliterator() {
            checkForComodification();
            return new ArrayListSpliterator<E>(ArrayList.this, offset,
                    offset + this.size, this.modCount);
        }
    }

    /**
     * 对列表中的每个元素执行给定操作，直到处理完所有元素或操作抛出异常。
     * 操作按迭代顺序执行（如果指定了顺序）。操作抛出的异常会转发给调用者。
     *
     * @param action 要为每个元素执行的操作
     * @throws NullPointerException 如果指定操作为 null
     * @throws ConcurrentModificationException 如果在迭代过程中检测到修改
     * @since 1.8
     */
    @Override
    public void forEach(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        final int expectedModCount = modCount;
        @SuppressWarnings("unchecked")
        final E[] elementData = (E[]) this.elementData;
        final int size = this.size;
        for (int i = 0; modCount == expectedModCount && i < size; i++) {
            action.accept(elementData[i]);
        }
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
    }

    /**
     * 在此列表中的元素上创建 <em><a href="Spliterator.html#binding">延迟绑定</a></em> 和 <em>快速失败</em> 的 {@link Spliterator}。
     *
     * <p>{@code Spliterator} 报告 {@link Spliterator#SIZED}、{@link Spliterator#SUBSIZED} 和 {@link Spliterator#ORDERED}。
     * 覆盖实现应记录附加特征值的报告。
     *
     * @return 此列表中的元素的 {@code Spliterator}
     * @since 1.8
     */
    @Override
    public Spliterator<E> spliterator() {
        return new ArrayListSpliterator<>(this, 0, -1, 0);
    }

    /**
     * 基于索引的分割成两半的、延迟初始化的 Spliterator。
     */
    static final class ArrayListSpliterator<E> implements Spliterator<E> {

        /*
         * 如果 ArrayList 是不可变的，或者结构上不可变（没有添加、删除等），
         * 我们可以使用 Arrays.spliterator 实现其分割器。
         * 相反，我们会在遍历期间尽可能多地检测干扰，而不会牺牲太多性能。
         * 我们主要依赖 modCount。这些不能保证检测到并发冲突，
         * 并且有时对线程内干扰过于保守，但足以检测到足够的问题，在实践中是有价值的。
         * 为此，我们（1）延迟初始化 fence 和 expectedModCount，直到我们需要提交正在检查的状态的最新点；
         * 从而提高精度。（这不适用于 SubList，它们使用当前的非延迟值创建分割器）。
         * （2）我们仅在 forEach（最关心性能的方法）结束时执行一次 ConcurrentModificationException 检查。
         * 使用 forEach 时（与迭代器相反），我们通常只能在操作之后检测到干扰，而不能在之前。
         * 进一步的 CME 触发检查适用于对假设的所有其他可能违反情况，例如给定其 size() 的 null 或太小的 elementData 数组，
         * 这只能由于干扰而发生。这允许 forEach 的内部循环无需任何进一步检查即可运行，并简化了 lambda 解析。
         * 虽然这需要许多检查，但请注意，在常见情况下 list.stream().forEach(a)，
         * 除了 forEach 本身内部之外，其他任何地方都不会发生检查或其他计算。
         * 其他较少使用的方法无法利用这些简化。
         */

        private final ArrayList<E> list;
        private int index; // 当前索引，在 advance/split 时修改
        private int fence; // -1 直到使用；然后是最后一个索引加 1
        private int expectedModCount; // 设置 fence 时初始化

        /** 创建覆盖给定范围的新分割器 */
        ArrayListSpliterator(ArrayList<E> list, int origin, int fence,
                             int expectedModCount) {
            this.list = list; // 如果未遍历，可以为 null
            this.index = origin;
            this.fence = fence;
            this.expectedModCount = expectedModCount;
        }

        private int getFence() { // 首次使用时将 fence 初始化为 size
            int hi; // forEach 方法中出现一个专门的变体
            ArrayList<E> lst;
            if ((hi = fence) < 0) {
                if ((lst = list) == null)
                    hi = fence = 0;
                else {
                    expectedModCount = lst.modCount;
                    hi = fence = lst.size;
                }
            }
            return hi;
        }

        public ArrayListSpliterator<E> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid) ? null : // 除非太小，否则将范围分成两半
                    new ArrayListSpliterator<E>(list, lo, index = mid,
                            expectedModCount);
        }

        public boolean tryAdvance(Consumer<? super E> action) {
            if (action == null)
                throw new NullPointerException();
            int hi = getFence(), i = index;
            if (i < hi) {
                index = i + 1;
                @SuppressWarnings("unchecked") E e = (E) list.elementData[i];
                action.accept(e);
                if (list.modCount != expectedModCount)
                    throw new ConcurrentModificationException();
                return true;
            }
            return false;
        }

        public void forEachRemaining(Consumer<? super E> action) {
            int i, hi, mc; // 从循环中提升访问和检查
            ArrayList<E> lst;
            Object[] a;
            if (action == null)
                throw new NullPointerException();
            if ((lst = list) != null && (a = lst.elementData) != null) {
                if ((hi = fence) < 0) {
                    mc = lst.modCount;
                    hi = lst.size;
                } else
                    mc = expectedModCount;
                if ((i = index) >= 0 && (index = hi) <= a.length) {
                    for (; i < hi; ++i) {
                        @SuppressWarnings("unchecked") E e = (E) a[i];
                        action.accept(e);
                    }
                    if (lst.modCount == mc)
                        return;
                }
            }
            throw new ConcurrentModificationException();
        }

        public long estimateSize() {
            return (long) (getFence() - index);
        }

        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
        }
    }

    /**
     * 删除此集合中满足给定谓词的所有元素。
     * 在迭代期间或由谓词抛出的任何异常都将转发给调用者。
     *
     * @param filter 一个谓词，对于要删除的元素返回 true
     * @return 如果删除了任何元素，则返回 true
     * @throws NullPointerException 如果指定的过滤器为 null
     * @throws ConcurrentModificationException 如果在迭代过程中检测到修改
     * @since 1.8
     */
    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        Objects.requireNonNull(filter);
        // 找出哪些元素要被删除
        // 在此阶段从过滤器谓词抛出的任何异常将使集合保持不变
        int removeCount = 0;
        final BitSet removeSet = new BitSet(size);
        final int expectedModCount = modCount;
        final int size = this.size;
        for (int i = 0; modCount == expectedModCount && i < size; i++) {
            @SuppressWarnings("unchecked")
            final E element = (E) elementData[i];
            if (filter.test(element)) {
                removeSet.set(i);
                removeCount++;
            }
        }
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }

        // 将存活的元素向左移动，填补被删除元素留下的空隙
        final boolean anyToRemove = removeCount > 0;
        if (anyToRemove) {
            final int newSize = size - removeCount;
            for (int i = 0, j = 0; (i < size) && (j < newSize); i++, j++) {
                i = removeSet.nextClearBit(i);
                elementData[j] = elementData[i];
            }
            for (int k = newSize; k < size; k++) {
                elementData[k] = null;  // 让 GC 工作
            }
            this.size = newSize;
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            modCount++;
        }

        return anyToRemove;
    }

    /**
     * 将该列表中的每个元素替换为对该元素应用运算符的结果。
     * 操作抛出的异常将转发给调用者。
     *
     * @param operator 要应用于每个元素的运算符
     * @throws NullPointerException 如果指定的运算符为 null
     * @throws ConcurrentModificationException 如果检测到修改
     * @since 1.8
     */
    @Override
    @SuppressWarnings("unchecked")
    public void replaceAll(UnaryOperator<E> operator) {
        Objects.requireNonNull(operator);
        final int expectedModCount = modCount;
        final int size = this.size;
        for (int i = 0; modCount == expectedModCount && i < size; i++) {
            elementData[i] = operator.apply((E) elementData[i]);
        }
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
        modCount++;
    }

    /**
     * 使用提供的 {@link Comparator} 对该列表进行排序。
     * 排序是稳定的：此方法不会为相等的元素重新排序。
     *
     * @param c 用于比较列表元素的 {@code Comparator}。{@code null} 值表示应使用元素的 {@link Comparable 自然顺序}
     * @throws ClassCastException 如果列表包含使用指定比较器无法相互比较的元素
     * @throws ConcurrentModificationException 如果在排序过程中检测到修改
     * @since 1.8
     */
    @Override
    @SuppressWarnings("unchecked")
    public void sort(Comparator<? super E> c) {
        final int expectedModCount = modCount;
        Arrays.sort((E[]) elementData, 0, size, c);
        if (modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
        modCount++;
    }
}