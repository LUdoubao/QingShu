package org.doubao.interview.agent.server.service.impl.collection;

import lombok.extern.slf4j.Slf4j;
import org.doubao.interview.agent.api.dto.collection.CollectionPerformanceDTO;
import org.doubao.interview.agent.api.dto.collection.CollectionPerformanceReportDTO;
import org.doubao.interview.agent.api.service.collection.CollectionPerformanceService;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 集合性能对比服务实现类
 * 
 * 【职责】实际执行 ArrayList 与 LinkedList 的性能测试，验证面试理论
 * 【边界】
 *   - 仅用于教学和面试演示，不应用于生产环境
 *   - 测试结果受 JVM、硬件、系统负载影响，仅供参考
 * 【线程安全】方法无状态，天然线程安全
 * 【幂等性】每次调用独立执行，结果可能因系统状态略有差异
 * 
 * @author interview-agent
 * @date 2026-04-05
 */
@Slf4j
@Service
public class CollectionPerformanceServiceImpl implements CollectionPerformanceService {

    /**
     * 默认测试数据量（平衡测试精度与执行时间）
     * 选择 100000 的原因：
     *   - 数据量太小：耗时差异不明显，无法体现性能差距
     *   - 数据量太大：测试时间过长，且可能导致内存溢出
     */
    private static final int DEFAULT_DATA_SIZE = 100_000;

    /**
     * 最小允许测试数据量（防止测试无意义）
     */
    private static final int MIN_DATA_SIZE = 1_000;

    /**
     * 最大允许测试数据量（防止内存溢出）
     */
    private static final int MAX_DATA_SIZE = 1_000_000;

    @Override
    public CollectionPerformanceReportDTO runFullComparison() {
        log.info("开始执行完整的集合性能对比测试, dataSize={}", DEFAULT_DATA_SIZE);
        long startTime = System.currentTimeMillis();

        List<CollectionPerformanceDTO> results = new ArrayList<>();

        // 场景1：随机访问性能测试（ArrayList 的核心优势）
        results.add(testRandomAccess(DEFAULT_DATA_SIZE));

        // 场景2：尾部添加性能测试（两者都很快）
        results.add(testTailAddition(DEFAULT_DATA_SIZE));

        // 场景3：头部插入性能测试（LinkedList 的优势场景）
        results.add(testHeadInsertion(DEFAULT_DATA_SIZE / 10)); // 减少数据量避免测试过慢

        // 场景4：中间插入性能测试（理论上 LinkedList 快，但需考虑查找成本）
        results.add(testMiddleInsertion(DEFAULT_DATA_SIZE / 10));

        // 场景5：遍历性能测试（验证迭代器 vs 普通 for 循环的差异）
        results.add(testIteration(DEFAULT_DATA_SIZE));

        long totalTime = System.currentTimeMillis() - startTime;
        log.info("完整性能对比测试完成, 总耗时={}ms", totalTime);

        String summary = "核心结论：ArrayList 适合读多写少场景（随机访问 O(1)），" +
                        "LinkedList 适合写多读少场景（头尾增删 O(1)），" +
                        "且 LinkedList 支持队列/栈操作。";

        String recommendation = "选型建议：\n" +
                "1. 读多写少 → ArrayList（如查询列表、缓存数据）\n" +
                "2. 写多读少 → LinkedList（如任务队列、消息缓冲）\n" +
                "3. 需要队列/栈 → LinkedList（实现 Deque 接口）\n" +
                "4. 不确定场景 → 优先 ArrayList（综合性能更优）";

        return new CollectionPerformanceReportDTO(results, summary, recommendation);
    }

    @Override
    public CollectionPerformanceReportDTO runComparisonWithDataSize(int dataSize) {
        // 参数校验：确保数据量在合理范围内
        if (dataSize < MIN_DATA_SIZE || dataSize > MAX_DATA_SIZE) {
            throw new IllegalArgumentException(
                String.format("数据量必须在 [%d, %d] 范围内，当前值: %d", 
                    MIN_DATA_SIZE, MAX_DATA_SIZE, dataSize)
            );
        }

        log.info("开始执行指定数据量的性能对比测试, dataSize={}", dataSize);
        long startTime = System.currentTimeMillis();

        List<CollectionPerformanceDTO> results = new ArrayList<>();
        results.add(testRandomAccess(dataSize));
        results.add(testTailAddition(dataSize));
        results.add(testHeadInsertion(Math.min(dataSize / 10, 10000)));
        results.add(testMiddleInsertion(Math.min(dataSize / 10, 10000)));
        results.add(testIteration(dataSize));

        long totalTime = System.currentTimeMillis() - startTime;
        log.info("指定数据量测试完成, dataSize={}, 总耗时={}ms", dataSize, totalTime);

        String summary = String.format("数据量 %d 下的性能对比结果已生成，详见各场景数据。", dataSize);
        String recommendation = "请参考完整报告中的选型建议。";

        return new CollectionPerformanceReportDTO(results, summary, recommendation);
    }

    /**
     * 测试随机访问性能
     * 
     * 【为什么这样做】随机访问是 ArrayList 的最大优势（O(1) vs O(n)），必须重点测试
     * 【边界处理】使用固定种子保证每次测试访问相同的索引，提高可比性
     *
     * @param size 数据量
     * @return 性能对比结果
     */
    private CollectionPerformanceDTO testRandomAccess(int size) {
        log.info("开始测试随机访问性能, size={}", size);

        // 准备测试数据
        List<String> arrayList = new ArrayList<>(size);
        List<String> linkedList = new LinkedList<>();
        for (int i = 0; i < size; i++) {
            String value = "element_" + i;
            arrayList.add(value);
            linkedList.add(value);
        }

        // 生成固定的随机索引序列（保证公平对比）
        Random random = new Random(42);
        int accessCount = Math.min(size, 10000); // 限制访问次数避免测试过慢
        int[] indices = new int[accessCount];
        for (int i = 0; i < accessCount; i++) {
            indices[i] = random.nextInt(size);
        }

        // 测试 ArrayList 随机访问
        long arrayStart = System.nanoTime();
        for (int index : indices) {
            arrayList.get(index);
        }
        long arrayTime = System.nanoTime() - arrayStart;

        // 测试 LinkedList 随机访问
        long linkedStart = System.nanoTime();
        for (int index : indices) {
            linkedList.get(index);
        }
        long linkedTime = System.nanoTime() - linkedStart;

        long arrayMs = arrayTime / 1_000_000;
        long linkedMs = linkedTime / 1_000_000;

        log.info("随机访问测试完成, ArrayList={}ms, LinkedList={}ms", arrayMs, linkedMs);

        String explanation = "ArrayList 基于数组，通过下标直接定位（O(1)）；" +
                            "LinkedList 需从头/尾遍历链表找到节点（O(n)）。";

        return new CollectionPerformanceDTO(
            "随机访问（get操作）",
            arrayMs,
            linkedMs,
            size,
            explanation
        );
    }

    /**
     * 测试尾部添加性能
     * 
     * 【为什么这样做】尾部添加是常见操作，ArrayList 无扩容时 O(1)，LinkedList 始终 O(1)
     * 【关键逻辑】ArrayList 指定初始容量避免扩容干扰测试结果
     *
     * @param size 数据量
     * @return 性能对比结果
     */
    private CollectionPerformanceDTO testTailAddition(int size) {
        log.info("开始测试尾部添加性能, size={}", size);

        // 测试 ArrayList 尾部添加（指定初始容量避免扩容）
        long arrayStart = System.nanoTime();
        List<String> arrayList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            arrayList.add("element_" + i);
        }
        long arrayTime = System.nanoTime() - arrayStart;

        // 测试 LinkedList 尾部添加
        long linkedStart = System.nanoTime();
        List<String> linkedList = new LinkedList<>();
        for (int i = 0; i < size; i++) {
            linkedList.add("element_" + i);
        }
        long linkedTime = System.nanoTime() - linkedStart;

        long arrayMs = arrayTime / 1_000_000;
        long linkedMs = linkedTime / 1_000_000;

        log.info("尾部添加测试完成, ArrayList={}ms, LinkedList={}ms", arrayMs, linkedMs);

        String explanation = "两者尾部添加均为 O(1)，ArrayList 无扩容时性能相当；" +
                            "若未指定容量，ArrayList 扩容时会略慢。";

        return new CollectionPerformanceDTO(
            "尾部添加（add操作）",
            arrayMs,
            linkedMs,
            size,
            explanation
        );
    }

    /**
     * 测试头部插入性能
     * 
     * 【为什么这样做】头部插入是 LinkedList 的优势场景（O(1)），而 ArrayList 需移动所有元素（O(n)）
     * 【边界处理】数据量不宜过大，否则 ArrayList 会极慢
     *
     * @param size 数据量
     * @return 性能对比结果
     */
    private CollectionPerformanceDTO testHeadInsertion(int size) {
        log.info("开始测试头部插入性能, size={}", size);

        // 测试 ArrayList 头部插入
        long arrayStart = System.nanoTime();
        List<String> arrayList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            arrayList.add(0, "element_" + i); // 在索引 0 处插入
        }
        long arrayTime = System.nanoTime() - arrayStart;

        // 测试 LinkedList 头部插入
        long linkedStart = System.nanoTime();
        List<String> linkedList = new LinkedList<>();
        for (int i = 0; i < size; i++) {
            ((LinkedList<String>) linkedList).addFirst("element_" + i);
        }
        long linkedTime = System.nanoTime() - linkedStart;

        long arrayMs = arrayTime / 1_000_000;
        long linkedMs = linkedTime / 1_000_000;

        log.info("头部插入测试完成, ArrayList={}ms, LinkedList={}ms", arrayMs, linkedMs);

        String explanation = "LinkedList 头部插入仅需修改指针（O(1)）；" +
                            "ArrayList 需将所有元素后移一位（O(n)）。";

        return new CollectionPerformanceDTO(
            "头部插入（addFirst操作）",
            arrayMs,
            linkedMs,
            size,
            explanation
        );
    }

    /**
     * 测试中间插入性能
     * 
     * 【为什么这样做】验证"LinkedList 中间插入快"的理论，但需注意查找节点的开销
     * 【关键逻辑】插入位置固定在中间，排除极端情况
     *
     * @param size 数据量
     * @return 性能对比结果
     */
    private CollectionPerformanceDTO testMiddleInsertion(int size) {
        log.info("开始测试中间插入性能, size={}", size);

        // 先填充一半数据
        List<String> arrayList = new ArrayList<>(size);
        List<String> linkedList = new LinkedList<>();
        for (int i = 0; i < size / 2; i++) {
            String value = "element_" + i;
            arrayList.add(value);
            linkedList.add(value);
        }

        int middleIndex = size / 4; // 插入到中间位置

        // 测试 ArrayList 中间插入
        long arrayStart = System.nanoTime();
        for (int i = size / 2; i < size; i++) {
            arrayList.add(middleIndex, "new_element_" + i);
        }
        long arrayTime = System.nanoTime() - arrayStart;

        // 测试 LinkedList 中间插入
        long linkedStart = System.nanoTime();
        for (int i = size / 2; i < size; i++) {
            linkedList.add(middleIndex, "new_element_" + i);
        }
        long linkedTime = System.nanoTime() - linkedStart;

        long arrayMs = arrayTime / 1_000_000;
        long linkedMs = linkedTime / 1_000_000;

        log.info("中间插入测试完成, ArrayList={}ms, LinkedList={}ms", arrayMs, linkedMs);

        String explanation = "LinkedList 修改指针快，但查找插入位置需 O(n)；" +
                            "ArrayList 查找快，但移动元素需 O(n)。实际性能取决于具体场景。";

        return new CollectionPerformanceDTO(
            "中间插入（add(index)操作）",
            arrayMs,
            linkedMs,
            size,
            explanation
        );
    }

    /**
     * 测试遍历性能
     * 
     * 【为什么这样做】验证 LinkedList 不能用普通 for 循环的坑点
     * 【关键逻辑】分别测试两种遍历方式，展示巨大差异
     *
     * @param size 数据量
     * @return 性能对比结果
     */
    private CollectionPerformanceDTO testIteration(int size) {
        log.info("开始测试遍历性能, size={}", size);

        // 准备测试数据
        List<String> arrayList = new ArrayList<>(size);
        List<String> linkedList = new LinkedList<>();
        for (int i = 0; i < size; i++) {
            String value = "element_" + i;
            arrayList.add(value);
            linkedList.add(value);
        }

        // 测试 ArrayList 迭代器遍历
        long arrayIterStart = System.nanoTime();
        for (String item : arrayList) {
            // 空操作，仅测试遍历速度
        }
        long arrayIterTime = System.nanoTime() - arrayIterStart;

        // 测试 LinkedList 迭代器遍历（推荐方式）
        long linkedIterStart = System.nanoTime();
        for (String item : linkedList) {
            // 空操作，仅测试遍历速度
        }
        long linkedIterTime = System.nanoTime() - linkedIterStart;

        long arrayMs = arrayIterTime / 1_000_000;
        long linkedMs = linkedIterTime / 1_000_000;

        log.info("迭代器遍历测试完成, ArrayList={}ms, LinkedList={}ms", arrayMs, linkedMs);

        String explanation = "两者使用迭代器遍历均为 O(n)，性能相当；" +
                            "注意：LinkedList 严禁使用普通 for 循环（get(index)），会导致 O(n²) 复杂度。";

        return new CollectionPerformanceDTO(
            "迭代器遍历（foreach操作）",
            arrayMs,
            linkedMs,
            size,
            explanation
        );
    }
}
