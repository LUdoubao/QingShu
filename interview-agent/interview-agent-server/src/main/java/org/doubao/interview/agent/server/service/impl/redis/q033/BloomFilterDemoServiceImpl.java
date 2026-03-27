package org.doubao.interview.agent.server.service.impl.redis.q033;

import org.doubao.interview.agent.api.dto.redis.q033.BloomFilterCheckRequest;
import org.doubao.interview.agent.api.dto.redis.q033.BloomFilterCheckResponse;
import org.doubao.interview.agent.api.service.redis.q033.BloomFilterDemoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * 问题 033:Bloom Filter 防穿透 Redis 实现。
 * <p>
 * 【核心原理】
 * Bloom Filter（布隆过滤器）是一种空间效率极高的概率型数据结构，用于判断元素是否存在于集合中。
 * <p>
 * 1. 判断结果只有两类：
 *    - 一定不存在（至少一个 bit 为 0）：结论 100% 准确
 *    - 可能存在（所有 bit 都为 1）：存在误判概率（False Positive），但不会漏判
 * <p>
 * 2. 为什么能防缓存穿透？
 *    - 缓存穿透：恶意请求大量不存在的 key，绕过缓存直接打到数据库
 *    - Bloom Filter 可在缓存前挡掉"一定不存在"的请求，显著减轻 DB 压力
 *    - 例如：100 万次请求中，可能 80 万次被 Bloom Filter 直接拦截
 * <p>
 * 3. "可能存在"仍需继续查缓存/数据库：
 *    - Bloom Filter 说"可能存在"时，不代表真的存在
 *    - 需要继续查询缓存/数据库进行最终确认
 *    - 因此会有少量误判回源（False Positive Rate）
 * <p>
 * 4. 普通布隆过滤器的局限性：
 *    - 不支持删除操作：因为多个元素可能映射到相同的 bit 位
 *    - 数据变更处理：通常通过重建整个 Bloom Filter 或使用计数布隆过滤器解决
 * <p>
 * 【应用场景】
 * - 防止缓存穿透：在缓存前拦截不存在的 key
 * - 大数据去重：判断用户是否已签到、文章是否已点赞
 * - 黑名单过滤：快速判断 IP/用户是否在黑名单中
 * <p>
 * 【参数设计】
 * - BIT_SIZE = 1<<20 (约 1M bit = 128KB)：比特数组大小
 * - 哈希函数种子：17, 31, 131（三个不同的质数，降低冲突概率）
 * - 误判率估算：与比特数组大小、哈希函数个数、插入元素数量相关
 */
@Service
public class BloomFilterDemoServiceImpl implements BloomFilterDemoService {

    private static final Logger log = LoggerFactory.getLogger(BloomFilterDemoServiceImpl.class);

    private static final String BLOOM_KEY = "q033:bloom:users";
    private static final int BIT_SIZE = 1 << 20; // 约 1M bit

    private final StringRedisTemplate redisTemplate;

    public BloomFilterDemoServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void init() {
        rebuildInternal();
    }

    /**
     * Bloom Filter 查询主流程。
     * <p>
     * 【执行步骤】
     * 1. 参数校验：检查 bizKey 是否合法
     * 2. Bloom Filter 预检查：调用 mightContain 判断是否可能存在
     * 3. 分支处理：
     *    a. 一定不存在：直接返回，不再查询数据库（关键价值）
     *    b. 可能存在：继续查询数据库进行最终验证
     * 4. 根据数据库查询结果返回不同响应：
     *    - 数据库存在：Bloom 判定准确，正常返回
     *    - 数据库不存在：Bloom 误判（False Positive），记录日志
     *
     * @param request 请求参数（包含 bizKey）
     * @return 响应结果（包含 Bloom 判定结果、是否回源、数据库是否存在等）
     */
    @Override
    public BloomFilterCheckResponse check(BloomFilterCheckRequest request) {
        // ========== Step 1: 解析并校验参数 ==========
        String bizKey = request == null ? null : request.getBizKey();
        if (bizKey == null || bizKey.trim().isEmpty()) {
            return build(false, "PARAM_VALIDATION", "bizKey 不能为空", null,
                    false, false, false);
        }
        
        // ========== Step 2: Bloom Filter 预检查 ==========
        // 调用 mightContain 判断元素是否"可能存在"
        boolean mayExist = mightContain(bizKey);
                
        // ========== Step 3: 分支处理 ==========
        if (!mayExist) {
            // ========== 情况 A: Bloom 判定一定不存在 ==========
            // 关键价值：可以直接拦截，不再查询数据库
            // 这是 Bloom Filter 防缓存穿透的核心场景
            return build(true, "BLOOM_BLOCKED", "Bloom 判定一定不存在，已在预过滤层拦截", bizKey,
                    true, false, false);
        }
        
        // ========== 情况 B: Bloom 判定可能存在 ==========
        // 注意：这不代表真的存在，仍需回源验证
        // 这里会有一定概率的误判（False Positive）
        boolean dbExists = simulateDbCheck(bizKey);
                
        if (dbExists) {
            // ========== 情况 B1: 数据库确认存在 ==========
            // Bloom 判定准确，正常返回
            return build(true, "BLOOM_PASS_DB_HIT", "Bloom 判定可能存在，回源后确认存在", bizKey,
                    false, true, true);
        }
                
        // ========== 情况 B2: 数据库确认不存在（误判示例）=========
        // Bloom Filter 的固有缺陷：误判（False Positive）
        // 虽然所有 bit 都为 1，但元素实际不存在
        // 这就是为什么"可能存在"时仍需查询数据库的原因
        return build(true, "BLOOM_FALSE_POSITIVE", "Bloom 判定可能存在，但回源发现不存在（误判示例）", bizKey,
                false, true, true);
    }

    @Override
    public BloomFilterCheckResponse rebuild() {
        rebuildInternal();
        return build(true, "BLOOM_REBUILT", "已重建Bloom Filter（普通布隆删除困难，常通过重建处理）", null,
                false, false, false);
    }

    private void rebuildInternal() {
        redisTemplate.delete(BLOOM_KEY);
        List<String> seeds = Arrays.asList("user-1001", "user-1002", "user-1003", "user-2001", "user-3001");
        for (String key : seeds) {
            add(key);
        }
        log.info("q033 bloom rebuilt, seedCount={}", seeds.size());
    }

    /**
     * 向 Bloom Filter 中添加元素。
     *
     * 【添加原理】
     * 1. 使用 3 个不同的哈希函数（种子分别为 17, 31, 131）计算 3 个位置
     * 2. 将这 3 个位置的 bit 设置为 1
     * 3. 即使发生哈希冲突，也只是将对应 bit 设为 1（不影响其他元素）
     *
     * 【为什么用 3 个哈希函数？】
     * - 1 个哈希函数：误判率高，不可靠
     * - 多个哈希函数：显著降低误判率，但增加计算开销
     * - 经验值：通常 3-7 个哈希函数能在性能和准确率间取得平衡
     *
     * @param value 要添加的元素值
     */
    private void add(String value) {
        // 使用 Spring Data Redis 执行原子操作
        redisTemplate.execute((RedisConnection connection) -> {
            byte[] key = BLOOM_KEY.getBytes(StandardCharsets.UTF_8);
            
            // 计算 3 个不同的哈希位置，并将对应 bit 设置为 1
            // 种子 17: 第一个哈希函数
            connection.setBit(key, index(value, 17), true);
            // 种子 31: 第二个哈希函数
            connection.setBit(key, index(value, 31), true);
            // 种子 131: 第三个哈希函数
            connection.setBit(key, index(value, 131), true);
            
            return null;
        });
    }

    /**
     * 判断元素是否可能存在于 Bloom Filter 中。
     *
     * 【判断原理】
     * 1. 使用与 add 方法相同的 3 个哈希函数计算位置
     * 2. 检查这 3 个位置的 bit 是否都为 1
     * 3. 判断逻辑：
     *    - 任意一个 bit 为 0 → 一定不存在（结论 100% 准确）
     *    - 所有 bit 都为 1 → 可能存在（存在误判概率）
     *
     * 【为什么"不存在判定"是确定的？】
     * - 如果元素真的存在，那么添加时一定会把这 3 个 bit 都设为 1
     * - 现在发现有 bit 为 0，说明这个元素从未被添加过
     * - 因此"不存在"的结论是 100% 可靠的
     *
     * 【为什么"存在判定"是不确定的？】
     * - 其他元素的哈希映射可能恰好也设置了这 3 个 bit
     * - 导致虽然当前元素的所有 bit 都是 1，但它实际并不存在
     * - 这就是误判（False Positive）的来源
     *
     * @param value 要检查的元素值
     * @return true=可能存在，false=一定不存在
     */
    private boolean mightContain(String value) {
        // 执行 Bloom Filter 检查
        Boolean result = redisTemplate.execute((RedisConnection connection) -> {
            byte[] key = BLOOM_KEY.getBytes(StandardCharsets.UTF_8);
            
            // 检查第 1 个哈希位置
            boolean b1 = connection.getBit(key, index(value, 17));
            // 检查第 2 个哈希位置
            boolean b2 = connection.getBit(key, index(value, 31));
            // 检查第 3 个哈希位置
            boolean b3 = connection.getBit(key, index(value, 131));
            
            // 当且仅当所有 bit 都为 1 时，才认为"可能存在"
            // 只要有一个 bit 为 0，就可以确定"一定不存在"
            return b1 && b2 && b3;
        });
        
        // Redis 返回 Boolean 对象，需要转换为基本类型
        return Boolean.TRUE.equals(result);
    }

    /**
     * 计算元素的哈希索引位置。
     *
     * 【哈希算法】
     * 1. 使用简单的多项式滚动哈希：hash = hash * seed + char
     * 2. 通过不同的 seed 值（17, 31, 131）生成不同的哈希函数
     * 3. 最后通过位运算 (&) 将哈希值映射到 [0, BIT_SIZE-1] 范围内
     *
     * 【为什么用位运算而不用取模？】
     * - BIT_SIZE = 1<<20 = 2^20，是 2 的幂次
     * - (BIT_SIZE - 1) & hash 等价于 hash % BIT_SIZE
     * - 位运算比取模运算更快（尤其在硬件层面）
     *
     * 【种子的选择】
     * - 17, 31, 131 都是质数
     * - 质数作为种子可以降低哈希冲突的概率
     * - 这些值较小，计算快且不易溢出
     *
     * @param value 元素值
     * @param seed 哈希种子（质数）
     * @return 哈希索引位置 [0, BIT_SIZE-1]
     */
    private long index(String value, int seed) {
        // 初始化哈希值为 0
        int hash = 0;
        
        // 多项式滚动哈希：遍历字符串的每个字符
        // 公式：hash = hash * seed + charCode
        for (int i = 0; i < value.length(); i++) {
            hash = hash * seed + value.charAt(i);
        }
        
        // 通过位运算将哈希值映射到 [0, BIT_SIZE-1] 范围
        // BIT_SIZE - 1 = 2^20 - 1，二进制为 20 个 1
        // & 运算相当于对 BIT_SIZE 取模，但性能更好
        return (BIT_SIZE - 1) & hash;
    }

    private boolean simulateDbCheck(String key) {
        // 模拟真实库只存在部分用户。
        return "user-1001".equals(key) || "user-1002".equals(key) || "user-1003".equals(key)
                || "user-2001".equals(key) || "user-3001".equals(key);
    }

    private BloomFilterCheckResponse build(boolean success, String stage, String message, String bizKey,
                                           boolean definitelyNotExist, boolean maybeExist, boolean passedToDb) {
        BloomFilterCheckResponse r = new BloomFilterCheckResponse();
        r.setSuccess(success);
        r.setStage(stage);
        r.setMessage(message);
        r.setBizKey(bizKey);
        r.setDefinitelyNotExist(definitelyNotExist);
        r.setMaybeExist(maybeExist);
        r.setPassedToDb(passedToDb);
        return r;
    }
}
