package org.doubao.interview.agent.server.collection;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * ArrayList原理演示API控制器
 * 
 * 提供API端点来演示ArrayList的底层原理
 */
@RestController
@RequestMapping("/collection/arraylist-principle")
public class ArrayListPrincipleController {

    /**
     * 演示初始化过程
     */
    @GetMapping("/initialization")
    public List<Object> demonstrateInitialization() {
        List<Object> result = new ArrayList<>();

        // 无参构造：懒加载机制
        ArrayList<String> list1 = new ArrayList<>();
        result.add("无参构造后容量: " + getArrayLength(list1));
        result.add("size: " + list1.size());

        // 第一次添加元素时初始化容量为10
        list1.add("element1");
        result.add("第一次add后容量: " + getArrayLength(list1));
        result.add("size: " + list1.size());

        // 指定初始容量构造
        ArrayList<String> list2 = new ArrayList<>(20);
        result.add("指定容量20构造后容量: " + getArrayLength(list2));
        result.add("size: " + list2.size());

        result.add("初始化过程演示完成");
        return result;
    }

    /**
     * 演示扩容过程
     */
    @GetMapping("/expansion")
    public List<Object> demonstrateExpansion() {
        List<Object> result = new ArrayList<>();

        ArrayList<Integer> list = new ArrayList<>(5); // 初始容量5
        result.add("初始容量: " + getArrayLength(list));

        // 添加元素直到触发扩容
        for (int i = 0; i < 8; i++) {
            list.add(i);
            result.add("添加元素 " + i + ", 当前size: " + list.size() + ", 容量: " + getArrayLength(list));
            
            if (i == 4) {
                result.add(">>> 容量满，下次add将触发扩容 <<<");
            } else if (i == 5) {
                result.add(">>> 发生扩容！容量从5变为10 <<<");
            }
        }

        // 继续添加更多元素，观察1.5倍扩容规律
        list.add(8);
        result.add("添加第9个元素后，容量变为: " + getArrayLength(list) + " (10->15)");

        result.add("扩容过程演示完成");
        return result;
    }

    /**
     * 演示性能特征
     */
    @PostMapping("/performance")
    public List<Object> demonstratePerformance(@RequestParam(defaultValue = "10000") int size) {
        List<Object> result = new ArrayList<>();

        // 准备数据
        ArrayList<Integer> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(i);
        }

        // 测试随机访问性能
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            int idx = i % size;
            int value = list.get(idx); // O(1) 操作
        }
        long getRandomTime = System.currentTimeMillis() - start;
        result.add("随机访问1000次耗时: " + getRandomTime + " ms");

        // 测试尾部添加性能
        start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            list.add(size + i); // O(1) 操作（假设容量足够）
        }
        long addTailTime = System.currentTimeMillis() - start;
        result.add("尾部添加1000次耗时: " + addTailTime + " ms");

        // 测试中间插入性能
        start = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            list.add(size/2, -1); // O(n) 操作，需要移动元素
        }
        long addMiddleTime = System.currentTimeMillis() - start;
        result.add("中间插入100次耗时: " + addMiddleTime + " ms");

        result.add("性能特征演示完成 (size: " + size + ")");
        return result;
    }

    /**
     * 使用我们实现的SimpleArrayList
     */
    @GetMapping("/simple-implementation")
    public List<Object> demonstrateSimpleImplementation() {
        List<Object> result = new ArrayList<>();

        // 测试我们的SimpleArrayList实现
        SimpleArrayList<String> simpleList = new SimpleArrayList<>();
        result.add("SimpleArrayList初始容量: " + getSimpleArrayLength(simpleList));

        simpleList.add("A");
        simpleList.add("B");
        simpleList.add("C");
        result.add("添加ABC后: " + simpleList.size() + " 个元素");

        simpleList.add(1, "X");
        result.add("在索引1插入X后: " + simpleList.size() + " 个元素，内容: [" + 
                  simpleList.get(0) + ", " + simpleList.get(1) + ", " + simpleList.get(2) + ", " + simpleList.get(3) + "]");

        String removed = simpleList.remove(1);
        result.add("删除索引1的元素: " + removed);
        result.add("删除后大小: " + simpleList.size());

        result.add("SimpleArrayList实现演示完成");
        return result;
    }

    /**
     * 通过反射获取ArrayList内部数组长度
     */
    private static int getArrayLength(ArrayList<?> list) {
        try {
            java.lang.reflect.Field field = ArrayList.class.getDeclaredField("elementData");
            field.setAccessible(true);
            Object[] array = (Object[]) field.get(list);
            return array.length;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 通过反射获取SimpleArrayList内部数组长度
     */
    private static int getSimpleArrayLength(SimpleArrayList<?> list) {
        try {
            java.lang.reflect.Field field = SimpleArrayList.class.getDeclaredField("elementData");
            field.setAccessible(true);
            Object[] array = (Object[]) field.get(list);
            return array.length;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}