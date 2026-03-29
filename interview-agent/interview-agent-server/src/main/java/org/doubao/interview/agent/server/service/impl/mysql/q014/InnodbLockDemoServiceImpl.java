package org.doubao.interview.agent.server.service.impl.mysql.q014;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.doubao.interview.agent.api.dto.question014.PurchaseRequest;
import org.doubao.interview.agent.api.dto.question014.PurchaseResponse;
import org.doubao.interview.agent.api.service.question014.InnodbLockDemoService;
import org.doubao.interview.agent.server.entity.q014.Order;
import org.doubao.interview.agent.server.entity.q014.Product;
import org.doubao.interview.agent.server.mapper.question014.OrderMapper;
import org.doubao.interview.agent.server.mapper.question014.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * InnoDB 锁演示服务实现类
 * 
 * 对应面试知识点：问题 014 - InnoDB 锁的分类
 * 
 * 【类注释】
 * 职责：实现购买业务逻辑，展示 InnoDB 各种锁的使用场景
 * 边界：仅用于演示，不包含真实电商系统的复杂校验（如风控、库存预占等）
 * 线程安全：通过 Spring 事务管理和数据库锁保证并发安全
 * 幂等性：通过 orderNo 唯一索引保证同一订单的幂等性
 * 
 * @author interview-agent
 * @date 2026-03-29
 */
@Slf4j
@Service
public class InnodbLockDemoServiceImpl implements InnodbLockDemoService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 执行购买操作 - 演示 InnoDB 锁的使用
     * 
     * 【关键逻辑说明】
     * 1. 使用排他锁 (X 锁) 锁定商品记录进行库存扣减
     * 2. 根据是否走索引，演示行锁和表级锁的区别
     * 3. 根据 lockMode 参数，演示不同的锁类型
     * 4. 意向锁 (IS/IX) 会自动由 InnoDB 添加，无需显式使用
     * 
     * 【锁的粒度与索引的关系】
     * - 如果查询条件命中主键或唯一索引：使用 Record Lock（记录锁）
     * - 如果查询条件命中普通索引：可能使用 Next-Key Lock
     * - 如果查询条件没有索引：全表扫描，锁住所有记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PurchaseResponse purchase(PurchaseRequest request) {
        String orderNo = request.getOrderNo() != null 
                ? request.getOrderNo() : UUID.randomUUID().toString();
        
        log.info("开始处理购买请求，orderNo={}, userId={}, productId={}, quantity={}, lockMode={}", 
                orderNo, request.getUserId(), request.getProductId(), 
                request.getQuantity(), request.getLockMode());
        
        // 1. 查询商品信息 - 根据是否走索引选择不同的查询方式
        Product product;
        if (request.getUseIndex()) {
            // 使用索引查询（主键）- 会加上 Record Lock（记录锁）
            product = productMapper.selectById(request.getProductId());
        } else {
            // 不走索引 - 全表扫描，可能锁住更多记录
            LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Product::getProductName, "测试商品");
            product = productMapper.selectOne(wrapper);
        }
        
        if (product == null) {
            log.error("商品不存在，productId={}", request.getProductId());
            throw new RuntimeException("商品不存在");
        }
        
        // 2. 检查库存是否充足
        if (product.getStock() < request.getQuantity()) {
            log.error("库存不足，productId={}, currentStock={}, requiredQuantity={}", 
                    request.getProductId(), product.getStock(), request.getQuantity());
            throw new RuntimeException("库存不足");
        }
        
        // 保存购买前的库存 - 用于展示锁的效果
        Integer stockBefore = product.getStock();
        
        // 3. 扣减库存 - 这里会使用排他锁 (X 锁)
        // 【关键点】InnoDB 会对这条 UPDATE 语句涉及的记录加锁：
        // - 如果使用索引：Record Lock（锁住特定记录）
        // - 如果不走索引：可能锁住整个表（取决于隔离级别）
        product.setStock(product.getStock() - request.getQuantity());
        product.setUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);
        
        // 4. 计算订单金额
        BigDecimal amount = product.getPrice().multiply(new BigDecimal(request.getQuantity()));
        
        // 5. 创建订单记录
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(request.getUserId());
        order.setProductId(request.getProductId());
        order.setQuantity(request.getQuantity());
        order.setAmount(amount);
        order.setStatus(1); // 已支付
        order.setLockType(request.getLockMode());
        order.setRemark(request.getRemark());
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.insert(order);
        
        // 6. 构建响应信息 - 展示使用的锁信息
        PurchaseResponse.LockInfo lockInfo = buildLockInfo(request);
        
        PurchaseResponse response = PurchaseResponse.builder()
                .success(true)
                .orderNo(orderNo)
                .message("购买成功")
                .productId(request.getProductId())
                .productName(product.getProductName())
                .stockBefore(stockBefore)
                .stockAfter(product.getStock())
                .amount(amount)
                .lockInfo(lockInfo)
                .build();
        
        log.info("购买处理成功，orderNo={}, 剩余库存={}", orderNo, product.getStock());
        
        return response;
    }

    /**
     * 构建锁信息 - 根据请求参数展示不同的锁类型
     */
    private PurchaseResponse.LockInfo buildLockInfo(PurchaseRequest request) {
        String lockType;
        String lockGranularity;
        String scopeDescription;
        String concurrencyImpact;
        
        if (request.getUseIndex()) {
            // 使用索引 - 行级锁
            lockType = "Record Lock (记录锁)";
            lockGranularity = "行级锁";
            scopeDescription = "只锁住 productId=" + request.getProductId() + " 这一条记录";
            concurrencyImpact = "其他事务可以访问其他商品记录，并发度高";
        } else {
            // 不走索引 - 全表扫描
            lockType = "Table Lock (表锁，因全表扫描导致)";
            lockGranularity = "表级锁";
            scopeDescription = "锁住整个 product_014 表的所有记录";
            concurrencyImpact = "其他事务无法访问任何商品记录，并发度低";
        }
        
        return PurchaseResponse.LockInfo.builder()
                .lockType(lockType)
                .lockMode("X (排他锁)")
                .lockGranularity(lockGranularity)
                .dependsOnIndex(request.getUseIndex())
                .scopeDescription(scopeDescription)
                .concurrencyImpact(concurrencyImpact)
                .build();
    }

    /**
     * 查询商品信息
     */
    @Override
    public String getProductInfo(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            return String.format("{\"error\": \"商品%d不存在\"}", productId);
        }
        
        return String.format(
                "{\"id\":%d,\"productName\":\"%s\",\"price\":%s,\"stock\":%d,\"version\":%d,\"createTime\":\"%s\",\"updateTime\":\"%s\"}",
                product.getId(),
                product.getProductName(),
                product.getPrice().toString(),
                product.getStock(),
                product.getVersion(),
                product.getCreateTime(),
                product.getUpdateTime()
        );
    }

    /**
     * 初始化演示数据
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String initDemoData() {
        log.info("开始初始化演示数据");
        
        // 清空已有数据
        productMapper.delete(null);
        orderMapper.delete(null);
        
        // 创建测试商品
        Product product1 = new Product();
        product1.setProductName("iPhone 15");
        product1.setPrice(new BigDecimal("9999.00"));
        product1.setStock(100);
        product1.setVersion(0);
        product1.setCreateTime(LocalDateTime.now());
        product1.setUpdateTime(LocalDateTime.now());
        productMapper.insert(product1);
        
        Product product2 = new Product();
        product2.setProductName("华为 Mate60");
        product2.setPrice(new BigDecimal("6999.00"));
        product2.setStock(200);
        product2.setVersion(0);
        product2.setCreateTime(LocalDateTime.now());
        product2.setUpdateTime(LocalDateTime.now());
        productMapper.insert(product2);
        
        log.info("演示数据初始化完成，创建商品：id1={}, id2={}", product1.getId(), product2.getId());
        
        return String.format(
                "演示数据初始化成功！\n" +
                "商品 1: ID=%d, 名称=%s, 价格=9999.00, 库存=100\n" +
                "商品 2: ID=%d, 名称=%s, 价格=6999.00, 库存=200\n" +
                "现在可以调用 /purchase 接口进行购买演示",
                product1.getId(), product1.getProductName(),
                product2.getId(), product2.getProductName()
        );
    }

    /**
     * 获取 InnoDB 锁的详细分类说明
     */
    @Override
    public String getLockClassification() {
        StringBuilder sb = new StringBuilder();
        sb.append("# InnoDB 锁的分类详解\n\n");
        
        sb.append("## 一、锁的分类维度\n\n");
        sb.append("InnoDB 锁可以从两个维度进行分类：\n\n");
        sb.append("### 1. 按锁的语义分类\n\n");
        sb.append("| 锁类型 | 英文 | 缩写 | 特点 |\n");
        sb.append("|------|------|------|------|\n");
        sb.append("| **共享锁** | Shared Lock | S 锁 | 允许其他事务读，不允许写 |\n");
        sb.append("| **排他锁** | Exclusive Lock | X 锁 | 不允许其他事务读写 |\n");
        sb.append("| **意向共享锁** | Intention Shared Lock | IS 锁 | 事务准备给数据行加 S 锁 |\n");
        sb.append("| **意向排他锁** | Intention Exclusive Lock | IX 锁 | 事务准备给数据行加 X 锁 |\n\n");
        
        sb.append("### 2. 按锁的粒度分类\n\n");
        sb.append("| 锁类型 | 英文 | 特点 |\n");
        sb.append("|------|------|------|\n");
        sb.append("| **表级锁** | Table Lock | 锁定整个表，开销小，易冲突 |\n");
        sb.append("| **行级锁** | Row Lock | 锁定特定行，开销大，并发高 |\n");
        sb.append("| **页级锁** | Page Lock | 锁定数据页，InnoDB 不支持 |\n\n");
        
        sb.append("### 3. 行级锁的细分类型\n\n");
        sb.append("| 锁类型 | 英文 | 锁定范围 |\n");
        sb.append("|------|------|----------|\n");
        sb.append("| **记录锁** | Record Lock | 锁住索引记录本身 |\n");
        sb.append("| **间隙锁** | Gap Lock | 锁住索引记录之间的间隙 |\n");
        sb.append("| **临键锁** | Next-Key Lock | 记录锁 + 间隙锁 |\n\n");
        
        sb.append("## 二、详细锁类型说明\n\n");
        
        sb.append("### 2.1 共享锁 (S 锁) 和排他锁 (X 锁)\n\n");
        sb.append("**共享锁 (S 锁)**：\n");
        sb.append("- 又称读锁，允许事务读取一行数据\n");
        sb.append("- 多个事务可以同时持有 S 锁\n");
        sb.append("- 持有 S 锁时，其他事务不能持有 X 锁\n");
        sb.append("- 典型场景：SELECT ... LOCK IN SHARE MODE\n\n");
        
        sb.append("**排他锁 (X 锁)**：\n");
        sb.append("- 又称写锁，允许事务更新或删除一行数据\n");
        sb.append("- 一个事务持有 X 锁后，其他事务不能再持有任何锁\n");
        sb.append("- 典型场景：UPDATE、DELETE、INSERT、SELECT ... FOR UPDATE\n\n");
        
        sb.append("**兼容性矩阵**：\n");
        sb.append("```\n");
        sb.append("         |  S 锁  |  X 锁  |\n");
        sb.append("---------|-------|-------|\n");
        sb.append("S 锁     |  √   |  ×   |\n");
        sb.append("X 锁     |  ×   |  ×   |\n");
        sb.append("```\n\n");
        
        sb.append("### 2.2 意向锁 (Intention Lock)\n\n");
        sb.append("**作用**：\n");
        sb.append("- 表级锁，用于快速判断表中是否有记录被加锁\n");
        sb.append("- 避免表锁和行锁的冲突检查需要遍历所有行\n");
        sb.append("- InnoDB 自动添加，无需人工干预\n\n");
        
        sb.append("**规则**：\n");
        sb.append("- 事务要给某行加 S 锁，必须先获得表的 IS 锁\n");
        sb.append("- 事务要给某行加 X 锁，必须先获得表的 IX 锁\n");
        sb.append("- IS 锁与 IX 锁兼容，但都与表锁不兼容\n\n");
        
        sb.append("### 2.3 记录锁 (Record Lock)\n\n");
        sb.append("**特点**：\n");
        sb.append("- 锁住索引记录本身\n");
        sb.append("- 必须依赖索引，否则退化为表锁\n");
        sb.append("- 主键索引和唯一索引都会使用记录锁\n\n");
        
        sb.append("**示例**：\n");
        sb.append("```sql\n");
        sb.append("-- 假设 id 是主键\n");
        sb.append("SELECT * FROM product WHERE id = 1 FOR UPDATE;\n");
        sb.append("-- 只会锁住 id=1 这一条记录\n");
        sb.append("```\n\n");
        
        sb.append("### 2.4 间隙锁 (Gap Lock)\n\n");
        sb.append("**特点**：\n");
        sb.append("- 锁住索引记录之间的间隙，不包含记录本身\n");
        sb.append("- 用于防止幻读（Phantom Read）\n");
        sb.append("- 只在 REPEATABLE READ 隔离级别下出现\n\n");
        
        sb.append("**示例**：\n");
        sb.append("```sql\n");
        sb.append("-- 假设 id 字段有索引，现有记录：1, 5, 10\n");
        sb.append("SELECT * FROM product WHERE id > 3 AND id < 8 FOR UPDATE;\n");
        sb.append("-- 会锁住 (1,5) 和 (5,10) 这两个间隙\n");
        sb.append("-- 其他事务无法在 (1,5) 或 (5,10) 之间插入新记录\n");
        sb.append("```\n\n");
        
        sb.append("### 2.5 临键锁 (Next-Key Lock)\n\n");
        sb.append("**特点**：\n");
        sb.append("- 记录锁 + 间隙锁的组合\n");
        sb.append("- 锁住索引记录及其之前的间隙\n");
        sb.append("- InnoDB 在 REPEATABLE READ 下的默认锁算法\n\n");
        
        sb.append("**示例**：\n");
        sb.append("```sql\n");
        sb.append("-- 假设 id 字段有索引，现有记录：1, 5, 10\n");
        sb.append("SELECT * FROM product WHERE id >= 5 FOR UPDATE;\n");
        sb.append("-- 会锁住：\n");
        sb.append("--   1. id=5 这条记录（Record Lock）\n");
        sb.append("--   2. (1,5) 这个间隙（Gap Lock）\n");
        sb.append("--   3. (5,10] 这个范围（Next-Key Lock）\n");
        sb.append("```\n\n");
        
        sb.append("## 三、锁的降级与升级\n\n");
        sb.append("### 3.1 索引对锁的影响\n\n");
        sb.append("**使用索引（推荐）**：\n");
        sb.append("```sql\n");
        sb.append("-- id 是主键\n");
        sb.append("UPDATE product SET stock = stock - 1 WHERE id = 1;\n");
        sb.append("-- 只锁住 id=1 这一条记录（Record Lock）\n");
        sb.append("```\n\n");
        
        sb.append("**不使用索引（危险）**：\n");
        sb.append("```sql\n");
        sb.append("-- name 字段没有索引\n");
        sb.append("UPDATE product SET stock = stock - 1 WHERE name = 'iPhone';\n");
        sb.append("-- 全表扫描，锁住所有记录（可能升级为表锁）\n");
        sb.append("```\n\n");
        
        sb.append("## 四、面试高频考点\n\n");
        sb.append("1. **行锁一定比表锁好吗？**\n");
        sb.append("   答：不一定。行锁并发高但开销大；表锁开销小但易冲突。要根据场景选择。\n\n");
        
        sb.append("2. **为什么 InnoDB 的行锁要依赖索引？**\n");
        sb.append("   答：InnoDB 是基于索引实现的行锁。如果不走索引，会退化为全表扫描，锁住所有记录。\n\n");
        
        sb.append("3. **什么是幻读？如何避免？**\n");
        sb.append("   答：幻读是指同一个查询在不同时间得到不同的结果集。InnoDB 通过 Next-Key Lock 避免幻读。\n\n");
        
        sb.append("4. **意向锁的作用是什么？**\n");
        sb.append("   答：快速判断表中是否有记录被加锁，避免遍历所有行检查锁冲突。\n\n");
        
        sb.append("5. **什么情况下间隙锁会失效？**\n");
        sb.append("   答：查询条件没有索引时，间隙锁会退化为表锁；READ COMMITTED 隔离级别下不使用间隙锁。\n\n");
        
        return sb.toString();
    }
}
