package org.doubao.recommend.service.common;

/**
 * Redis 键常量定义类
 * 定义推荐系统中使用的所有 Redis 键的前缀和完整键名
 * 统一管理和维护 Redis 键，便于维护和修改
 */
public final class RedisKeys {
    
    /**
     * 私有构造函数，防止实例化
     */
    private RedisKeys() {}

    /**
     * 首页热门内容有序集合
     * 存储首页推荐的热门内容 ID，按热度分数排序
     * 格式：rec:hot:home
     */
    public static final String HOT_HOME_ZSET = "rec:hot:home";
    
    /**
     * 详情页热门内容前缀
     * 用于存储详情页相关内容的热门推荐
     * 格式：rec:hot:detail:{contentId}
     */
    public static final String HOT_DETAIL_PREFIX = "rec:hot:detail:";
    
    /**
     * 用户画像前缀
     * 用于存储用户的兴趣画像数据（标签、作者、话题权重等）
     * 格式：rec:profile:{userIdentity}
     */
    public static final String PROFILE_PREFIX = "rec:profile:";
    
    /**
     * 内容特征前缀
     * 用于存储内容的特征信息（热度、质量、新鲜度等）
     * 格式：rec:feature:{contentId}
     */
    public static final String FEATURE_PREFIX = "rec:feature:";
    
    /**
     * 相似内容前缀
     * 用于存储与某个内容相似的内容 ID 列表
     * 格式：rec:similar:{contentId}
     */
    public static final String SIMILAR_PREFIX = "rec:similar:";
    
    /**
     * 用户已读内容前缀
     * 用于存储用户已经浏览过的内容 ID 集合
     * 格式：rec:seen:{userIdentity}
     */
    public static final String SEEN_PREFIX = "rec:seen:";
    
    /**
     * 预计算首页推荐前缀
     * 用于存储预先计算好的首页推荐结果
     * 格式：rec:pre:home:{userIdentity}
     */
    public static final String PRECOMPUTED_HOME_PREFIX = "rec:pre:home:";
    
    /**
     * 活跃用户有序集合
     * 存储活跃用户的 ID，按活跃度排序
     * 格式：rec:active:users
     */
    public static final String USER_ACTIVE_ZSET = "rec:active:users";
    
    /**
     * 推荐结果缓存前缀
     * 用于缓存最终的推荐结果列表
     * 格式：rec:result:{scene}:{userIdentity}
     */
    public static final String RESULT_CACHE_PREFIX = "rec:result:";
}
