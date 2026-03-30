package org.doubao.interview.agent.server.example.reflection;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 产品实体类 - 演示注解处理
 * <p>
 * 使用自定义注解标注字段，用于反射解析
 * </p>
 *
 * @author interview-agent
 * @date 2026-03-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    
    @FieldMeta(chineseName = "产品 ID", required = true)
    private Long id;
    
    @FieldMeta(chineseName = "产品名称", required = true, minLength = 2, maxLength = 50)
    private String name;
    
    @FieldMeta(chineseName = "产品价格", required = true)
    private Double price;
    
    @FieldMeta(chineseName = "库存数量")
    private Integer stock;
    
    @FieldMeta(chineseName = "产品描述", maxLength = 500)
    private String description;
}
