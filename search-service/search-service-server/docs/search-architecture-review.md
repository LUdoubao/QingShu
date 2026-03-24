# 搜索体系优化与重构方案

## 1. 文档信息

- 项目：`search-service/search-service-server`
- 主题：搜索功能优化、搜索建议改造、可扩展搜索架构设计
- 约束：当前仅使用 MySQL + Redis，不引入 Elasticsearch 等复杂组件
- 目标：提升搜索质量、建议词质量、性能稳定性，并为未来升级 ES 预留扩展接口

---

## 2. 背景与现状

当前搜索链路主要由 `search-service` 作为聚合入口，真实搜索与建议逻辑主要沉在 `quote-service` 中。

### 2.1 当前代码现状

- `search-service` 主要负责：
  - 接收搜索建议请求
  - 接收搜索结果请求
  - 记录搜索历史
  - 简单 Redis 短缓存
- `quote-service` 主要负责：
  - 通过 `quote.content LIKE '%keyword%'` 进行正文搜索
  - 通过 `tag.name LIKE '%keyword%'` 进行标签搜索
  - 通过 `ORDER BY RAND()` 随机返回 quote 建议
  - 拼装标签建议、quote 建议

### 2.2 当前主要问题

#### 2.2.1 搜索质量问题

- 正文搜索依赖 `%keyword%` 模糊匹配，相关性弱
- 无统一召回策略与排序模型
- 无多路召回能力，难以区分标题、正文、标签、作者、分类的重要性
- 缺少热度、新鲜度、内容质量等排序维度

#### 2.2.2 搜索建议问题

- 建议词来源单一
- quote 建议使用 `ORDER BY RAND()`，结果不稳定且性能差
- 无热词、历史、前缀联想、个性化能力
- 分类建议尚未完成

#### 2.2.3 性能与架构问题

- 搜索直接打业务表，随着数据量增长容易退化
- `%keyword%` 难以利用普通索引
- 搜索历史为 append-only 设计，写入冗余，读取 SQL 偏重
- 搜索逻辑沉在业务服务中，不利于搜索域独立演进

#### 2.2.4 可扩展性问题

- 当前没有统一 `SearchProvider` 抽象
- 无独立搜索索引表
- 无法平滑切换到 ES/OpenSearch 等更强搜索引擎

---

## 3. 建设目标

本次搜索重构目标如下：

1. 在仅使用 MySQL + Redis 的约束下，显著提升搜索效果。
2. 将搜索能力从“业务查库”升级为“索引检索”。
3. 通过召回 + 排序两阶段设计提升结果可控性。
4. 将搜索建议改造为多来源融合的建议体系。
5. 保证现有服务可渐进迁移，不要求一次性推翻重做。
6. 通过抽象接口为未来 ES 升级预留空间。

非目标：

- 本阶段不引入 ES / OpenSearch / Solr
- 本阶段不做复杂机器学习排序
- 本阶段不强依赖复杂中文分词系统

---

## 4. 总体方案

### 4.1 总体架构

建议将搜索体系拆为四层：

1. 接入层 `search-service`
2. 检索层 `search-core`
3. 索引层 `search-index`
4. 同步层 `search-sync`

### 4.2 架构职责划分

#### 4.2.1 接入层

负责：

- API 接入
- 参数校验
- 用户搜索历史记录
- 搜索统计埋点
- 结果聚合
- Redis 缓存控制

#### 4.2.2 检索层

负责：

- Query 预处理
- 多路召回
- 排序打分
- 分页组装

#### 4.2.3 索引层

负责：

- 搜索文档索引构建
- 搜索词项倒排构建
- 建议词索引构建
- 索引重建与增量更新

#### 4.2.4 同步层

负责：

- 监听 quote/tag/category 变更
- 将业务数据同步到搜索索引
- 清理搜索缓存

### 4.3 技术原则

- MySQL 存储可恢复、可重建的索引数据
- Redis 存储热点数据、前缀建议、排行榜、短期结果缓存
- 搜索采用“两阶段”：召回 + 排序
- 所有底层检索能力通过 `SearchProvider` 统一抽象

---

## 5. 数据模型与 DDL 设计

以下 DDL 以 MySQL 8 为基准。

### 5.1 搜索文档主索引表 `search_doc_index`

用途：

- 承载搜索主文档
- 聚合 quote/tag/category/author 信息
- 为排序提供热度和质量字段

```sql
CREATE TABLE `search_doc_index` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `biz_type` VARCHAR(32) NOT NULL COMMENT '业务类型，如 quote',
  `biz_id` BIGINT NOT NULL COMMENT '业务主键ID',
  `title` VARCHAR(255) DEFAULT NULL COMMENT '标题',
  `content` TEXT COMMENT '正文/引文内容',
  `author_name` VARCHAR(128) DEFAULT NULL COMMENT '作者名',
  `source` VARCHAR(255) DEFAULT NULL COMMENT '来源',
  `category_id` BIGINT DEFAULT NULL COMMENT '分类ID',
  `category_name` VARCHAR(128) DEFAULT NULL COMMENT '分类名',
  `tag_ids_json` JSON DEFAULT NULL COMMENT '标签ID列表',
  `tag_names_text` VARCHAR(1000) DEFAULT NULL COMMENT '标签名拼接',
  `search_text` TEXT COMMENT '搜索聚合文本',
  `search_text_normalized` TEXT COMMENT '标准化搜索文本',
  `pinyin_abbr` VARCHAR(255) DEFAULT NULL COMMENT '拼音首字母缩写，可选',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态',
  `is_original` TINYINT NOT NULL DEFAULT 0 COMMENT '是否原创',
  `publish_time` DATETIME DEFAULT NULL COMMENT '发布时间',
  `view_count` BIGINT NOT NULL DEFAULT 0 COMMENT '浏览数',
  `like_count` BIGINT NOT NULL DEFAULT 0 COMMENT '点赞数',
  `comment_count` BIGINT NOT NULL DEFAULT 0 COMMENT '评论数',
  `favorite_count` BIGINT NOT NULL DEFAULT 0 COMMENT '收藏数',
  `hot_score` DECIMAL(12,4) NOT NULL DEFAULT 0 COMMENT '热度分',
  `quality_score` DECIMAL(12,4) NOT NULL DEFAULT 0 COMMENT '质量分',
  `search_version` BIGINT NOT NULL DEFAULT 1 COMMENT '索引版本',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_type_biz_id` (`biz_type`, `biz_id`),
  KEY `idx_status_deleted_publish` (`status`, `is_deleted`, `publish_time` DESC),
  KEY `idx_category_status_deleted` (`category_id`, `status`, `is_deleted`),
  KEY `idx_hot_score` (`hot_score` DESC),
  KEY `idx_updated_time` (`updated_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='搜索文档主索引表';
```

### 5.2 搜索词项倒排表 `search_term_index`

用途：

- 用于 term 召回
- 用于 prefix 召回
- 避免大量 `%keyword%` 全表扫描

```sql
CREATE TABLE `search_term_index` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `biz_type` VARCHAR(32) NOT NULL COMMENT '业务类型',
  `biz_id` BIGINT NOT NULL COMMENT '业务主键ID',
  `term` VARCHAR(128) NOT NULL COMMENT '词项',
  `term_normalized` VARCHAR(128) NOT NULL COMMENT '标准化词项',
  `term_type` VARCHAR(32) NOT NULL COMMENT 'WORD/TAG/CATEGORY/AUTHOR/PREFIX',
  `source_field` VARCHAR(32) NOT NULL COMMENT '来源字段 content/title/tag/category/author',
  `weight` INT NOT NULL DEFAULT 1 COMMENT '词项基础权重',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_term_type_field` (`biz_type`, `biz_id`, `term_normalized`, `term_type`, `source_field`),
  KEY `idx_term_type_biz` (`term_normalized`, `term_type`, `biz_type`),
  KEY `idx_biz_type_biz_id` (`biz_type`, `biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='搜索词项倒排表';
```

### 5.3 搜索建议词表 `search_suggest_term`

用途：

- 存 query suggestion、tag suggestion、category suggestion、author suggestion
- 作为 Redis 前缀联想缓存的数据源

```sql
CREATE TABLE `search_suggest_term` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `term_text` VARCHAR(128) NOT NULL COMMENT '建议词文本',
  `term_normalized` VARCHAR(128) NOT NULL COMMENT '标准化文本',
  `prefix_text` VARCHAR(64) NOT NULL COMMENT '前缀，用于快速归档缓存',
  `term_type` VARCHAR(32) NOT NULL COMMENT 'QUERY/TAG/CATEGORY/AUTHOR/QUOTE',
  `source_id` BIGINT DEFAULT NULL COMMENT '来源ID，可为空',
  `source_biz_type` VARCHAR(32) DEFAULT NULL COMMENT '来源业务类型',
  `hot_score` DECIMAL(12,4) NOT NULL DEFAULT 0 COMMENT '热度分',
  `quality_score` DECIMAL(12,4) NOT NULL DEFAULT 0 COMMENT '质量分',
  `search_count` BIGINT NOT NULL DEFAULT 0 COMMENT '搜索次数',
  `click_count` BIGINT NOT NULL DEFAULT 0 COMMENT '点击次数',
  `result_count` BIGINT NOT NULL DEFAULT 0 COMMENT '命中结果数',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_term_type_source` (`term_normalized`, `term_type`, `source_id`),
  KEY `idx_prefix_type_hot` (`prefix_text`, `term_type`, `hot_score` DESC),
  KEY `idx_term_normalized` (`term_normalized`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='搜索建议词表';
```

### 5.4 搜索统计表 `search_query_stats`

用途：

- 热搜词统计
- 趋势词统计
- 建议词质量评估
- 排序反馈

```sql
CREATE TABLE `search_query_stats` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `query_text` VARCHAR(255) NOT NULL COMMENT '原始搜索词',
  `query_text_normalized` VARCHAR(255) NOT NULL COMMENT '标准化搜索词',
  `search_count` BIGINT NOT NULL DEFAULT 0 COMMENT '搜索次数',
  `result_count_total` BIGINT NOT NULL DEFAULT 0 COMMENT '累计结果数',
  `click_count` BIGINT NOT NULL DEFAULT 0 COMMENT '点击次数',
  `last_result_count` INT NOT NULL DEFAULT 0 COMMENT '最近一次结果数',
  `last_search_time` DATETIME DEFAULT NULL COMMENT '最近搜索时间',
  `trend_score` DECIMAL(12,4) NOT NULL DEFAULT 0 COMMENT '趋势分',
  `hot_score` DECIMAL(12,4) NOT NULL DEFAULT 0 COMMENT '热度分',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_query_normalized` (`query_text_normalized`),
  KEY `idx_hot_score` (`hot_score` DESC),
  KEY `idx_trend_score` (`trend_score` DESC),
  KEY `idx_last_search_time` (`last_search_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='搜索统计表';
```

### 5.5 用户搜索历史表 `user_search_history`

用途：

- 替代现有 append-only 历史写法
- 降低写冗余
- 优化历史查询性能

```sql
CREATE TABLE `user_search_history` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `query_text` VARCHAR(255) NOT NULL COMMENT '原始搜索词',
  `query_text_normalized` VARCHAR(255) NOT NULL COMMENT '标准化搜索词',
  `search_count` INT NOT NULL DEFAULT 1 COMMENT '搜索次数',
  `last_search_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最近搜索时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_query` (`user_id`, `query_text_normalized`),
  KEY `idx_user_last_search_time` (`user_id`, `last_search_time` DESC),
  KEY `idx_user_search_count` (`user_id`, `search_count` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户搜索历史表';
```

推荐写入方式：

```sql
INSERT INTO user_search_history (user_id, query_text, query_text_normalized, search_count, last_search_time, deleted)
VALUES (?, ?, ?, 1, NOW(), 0)
ON DUPLICATE KEY UPDATE
  query_text = VALUES(query_text),
  search_count = search_count + 1,
  last_search_time = NOW(),
  deleted = 0;
```

### 5.6 搜索索引同步任务表 `search_index_sync_task`

用途：

- 管理增量同步任务
- 失败重试
- 支持离线全量重建

```sql
CREATE TABLE `search_index_sync_task` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `biz_type` VARCHAR(32) NOT NULL,
  `biz_id` BIGINT NOT NULL,
  `op_type` VARCHAR(32) NOT NULL COMMENT 'UPSERT/DELETE/REBUILD',
  `task_status` VARCHAR(32) NOT NULL DEFAULT 'INIT' COMMENT 'INIT/PROCESSING/SUCCESS/FAILED',
  `retry_count` INT NOT NULL DEFAULT 0,
  `fail_reason` VARCHAR(500) DEFAULT NULL,
  `next_retry_time` DATETIME DEFAULT NULL,
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status_retry` (`task_status`, `next_retry_time`),
  KEY `idx_biz` (`biz_type`, `biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='搜索索引同步任务表';
```

---

## 6. Redis 设计

### 6.1 Key 规划

```text
search:suggest:prefix:{prefix}
search:suggest:hot:global
search:suggest:hot:day:{yyyyMMdd}
search:history:user:{userId}
search:result:{scene}:{type}:{queryHash}:{page}:{size}:{sort}
search:recall:term:{term}
search:recall:prefix:{prefix}
search:hot:query
search:doc:hot:{bizType}:{bizId}
```

### 6.2 Key 用途

- `search:suggest:prefix:{prefix}`
  - 存储前缀建议缓存
  - 推荐使用 String(JSON) 或 Hash

- `search:suggest:hot:global`
  - 全局热门搜索词
  - 推荐使用 ZSet

- `search:history:user:{userId}`
  - 用户搜索历史
  - 推荐使用 ZSet

- `search:result:*`
  - 搜索结果短缓存
  - TTL 建议 1 到 5 分钟

- `search:recall:*`
  - 高频 term 的候选集缓存
  - TTL 建议较短

---

## 7. 模块与包结构设计

### 7.1 模块拆分建议

```text
search-service
├─ search-service-api
├─ search-service-server
├─ search-core
├─ search-index-api
├─ search-index-mysql-redis
└─ search-sync
```

若短期不希望增加过多 Maven 模块，也可先在 `search-service-server` 内通过分包方式实现。

### 7.2 包结构建议

```text
org.doubao.search
├─ controller
│  ├─ SearchController
│  ├─ SearchSuggestController
│  └─ SearchTrackController
├─ service
│  ├─ SearchService
│  ├─ SuggestService
│  ├─ SearchHistoryService
│  ├─ SearchStatsService
│  └─ SearchIndexService
├─ service.impl
├─ domain
│  ├─ query
│  │  ├─ SearchQuery
│  │  ├─ SuggestQuery
│  │  └─ QueryContext
│  ├─ result
│  │  ├─ SearchPageResult
│  │  ├─ SuggestResult
│  │  └─ RecallDoc
│  ├─ enum
│  └─ model
├─ provider
│  ├─ SearchProvider
│  ├─ SuggestProvider
│  └─ impl
│     └─ MysqlRedisSearchProvider
├─ recall
│  ├─ RecallService
│  ├─ MultiRouteRecallService
│  ├─ strategy
│  │  ├─ ExactMatchRecallStrategy
│  │  ├─ PrefixRecallStrategy
│  │  ├─ TermRecallStrategy
│  │  ├─ TagRecallStrategy
│  │  └─ FallbackLikeRecallStrategy
├─ rank
│  ├─ RankService
│  ├─ RuleBasedRankService
│  └─ scorer
│     ├─ TextMatchScorer
│     ├─ HotScoreScorer
│     ├─ FreshnessScorer
│     └─ PersonalizationScorer
├─ suggest
│  ├─ SuggestAssembler
│  ├─ SuggestSource
│  └─ source
│     ├─ UserHistorySuggestSource
│     ├─ HotQuerySuggestSource
│     ├─ PrefixSuggestSource
│     ├─ TagSuggestSource
│     ├─ CategorySuggestSource
│     └─ QuoteSuggestSource
├─ index
│  ├─ SearchDocumentBuilder
│  ├─ SearchTermBuilder
│  ├─ SuggestTermBuilder
│  └─ SearchIndexSyncManager
├─ mapper
│  ├─ SearchDocIndexMapper
│  ├─ SearchTermIndexMapper
│  ├─ SearchSuggestTermMapper
│  ├─ SearchQueryStatsMapper
│  └─ UserSearchHistoryMapper
├─ entity
├─ dto
├─ converter
├─ cache
│  ├─ SearchCacheKeyBuilder
│  └─ SearchCacheService
├─ job
│  ├─ SearchFullRebuildJob
│  ├─ SearchSuggestWarmupJob
│  └─ SearchStatsFlushJob
└─ listener
   └─ QuoteSearchSyncListener
```

### 7.3 核心抽象接口

```java
public interface SearchProvider {
    SearchPageResult search(SearchQuery query);
    SuggestResult suggest(SuggestQuery query);
}

public interface RecallStrategy {
    List<RecallDoc> recall(QueryContext context);
}

public interface RankService {
    List<RecallDoc> rank(QueryContext context, List<RecallDoc> docs);
}

public interface SearchIndexService {
    void upsertDocument(String bizType, Long bizId);
    void deleteDocument(String bizType, Long bizId);
    void rebuildAll(String bizType);
}
```

---

## 8. 检索流程设计

### 8.1 Query 预处理

输入 query 后统一做：

- trim
- 小写化
- 全半角统一
- 空白字符归一
- 特殊符号清洗
- 可选拼音缩写生成
- term 切分

输出：

- `rawQuery`
- `normalizedQuery`
- `prefix`
- `terms`
- `queryIntent`

### 8.2 召回策略

建议采用多路召回：

1. 精确召回
2. 前缀召回
3. term 倒排召回
4. tag/category/author 召回
5. 弱兜底召回

每路召回建议取 Top 50 到 100，合并去重后保留 200 到 300 个候选用于排序。

### 8.3 排序策略

第一版采用规则排序：

```text
finalScore =
  textMatchScore
  + hotScore
  + freshnessScore
  + qualityScore
  + personalizationScore
```

建议权重：

- 标题精确命中：100
- 标签精确命中：90
- 分类精确命中：80
- 作者精确命中：70
- 内容前缀命中：60
- 内容包含命中：30
- 多 term 全命中：+20
- 热度分：0 到 50
- 新鲜度分：0 到 20
- 质量分：0 到 20
- 个性化分：0 到 10

---

## 9. 搜索建议设计

### 9.1 建议来源

建议词由以下来源融合：

1. 用户历史搜索
2. 全局热搜
3. 前缀联想
4. 实体建议
   - 标签
   - 分类
   - 作者
5. 内容建议

### 9.2 建议策略

- query 长度 1 到 2
  - 优先历史、热词、前缀联想

- query 长度大于等于 3
  - 增加实体建议、内容建议

- query 为空
  - 返回历史 + 热搜 + 默认推荐标签

### 9.3 设计原则

- 取消 `ORDER BY RAND()`
- 优先返回稳定、高质量、高点击率建议
- 支持去重与分组
- Redis 做 prefix 热缓存

---

## 10. 搜索时序图

### 10.1 搜索建议时序图

```text
用户
  -> SearchController /search/suggestions?keyword=xxx
    -> SuggestService
      -> QueryPreprocessor
        -> 生成 normalizedQuery / prefix / terms
      -> Redis 查询 search:suggest:prefix:{prefix}
        -> 命中: 直接返回缓存
        -> 未命中:
          -> UserHistorySuggestSource 查询 Redis/MySQL 用户历史
          -> HotQuerySuggestSource 查询 Redis 热词ZSet
          -> PrefixSuggestSource 查询 MySQL search_suggest_term
          -> TagSuggestSource 查询 MySQL search_suggest_term(term_type=TAG)
          -> CategorySuggestSource 查询 MySQL search_suggest_term(term_type=CATEGORY)
          -> QuoteSuggestSource 查询 search_doc_index / recall cache
          -> SuggestAssembler 合并去重排序截断
          -> 回写 Redis prefix cache
      -> 返回 suggestions
```

### 10.2 搜索结果时序图

```text
用户
  -> SearchController /search/results
    -> SearchService
      -> QueryPreprocessor
        -> 生成 normalizedQuery / terms / intent
      -> SearchHistoryService
        -> upsert 用户历史
      -> SearchStatsService
        -> 记录 query 搜索次数
      -> SearchProvider.search()
        -> MultiRouteRecallService
          -> ExactMatchRecallStrategy
          -> PrefixRecallStrategy
          -> TermRecallStrategy
          -> TagRecallStrategy
          -> FallbackLikeRecallStrategy
        -> 合并候选 docIds
        -> 批量查询 search_doc_index
        -> RankService 规则打分排序
        -> AssembleResult
      -> 结果缓存写 Redis
    -> 返回分页结果
```

### 10.3 索引增量同步时序图

```text
quote-service 发生新增/编辑/审核通过/下架/删除
  -> 发布搜索同步事件 或 写入 search_index_sync_task
    -> SearchIndexSyncManager
      -> 拉取 quote/tag/category/user 信息
      -> SearchDocumentBuilder 构建 search_doc_index
      -> SearchTermBuilder 重建 search_term_index
      -> SuggestTermBuilder 更新 search_suggest_term
      -> 清理相关 Redis 缓存
      -> 更新任务状态 SUCCESS/FAILED
```

---

## 11. 接口返回结构草案

### 11.1 搜索建议接口

接口：

```text
GET /search/suggestions?keyword=xxx
```

返回示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "keyword": "苏",
    "normalizedKeyword": "苏",
    "histories": [
      {
        "text": "苏轼",
        "type": "HISTORY",
        "score": 98.2
      }
    ],
    "hotQueries": [
      {
        "text": "苏轼名句",
        "type": "HOT_QUERY",
        "score": 95.6
      }
    ],
    "querySuggestions": [
      {
        "text": "苏轼",
        "type": "QUERY",
        "score": 93.1
      }
    ],
    "tags": [
      {
        "id": 12,
        "text": "宋词",
        "type": "TAG",
        "score": 88.1
      }
    ],
    "categories": [
      {
        "id": 3,
        "text": "诗词",
        "type": "CATEGORY",
        "score": 81.0
      }
    ],
    "authors": [
      {
        "text": "苏轼",
        "type": "AUTHOR",
        "score": 90.5
      }
    ],
    "quotes": [
      {
        "bizId": 10234,
        "text": "竹杖芒鞋轻胜马，谁怕？",
        "type": "QUOTE",
        "score": 84.7
      }
    ]
  }
}
```

### 11.2 搜索结果接口

接口：

```text
GET /search/results?keyword=xxx&type=quote&page=1&size=10&sort=comprehensive
```

返回示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "keyword": "苏轼",
    "normalizedKeyword": "苏轼",
    "type": "quote",
    "sort": "comprehensive",
    "page": 1,
    "size": 10,
    "total": 238,
    "hasMore": true,
    "searchCostMs": 24,
    "recallInfo": {
      "recallCount": 180,
      "routes": [
        "EXACT",
        "TERM",
        "PREFIX"
      ]
    },
    "records": [
      {
        "id": 10234,
        "content": "竹杖芒鞋轻胜马，谁怕？一蓑烟雨任平生。",
        "author": "苏轼",
        "source": "定风波",
        "categoryId": 3,
        "categoryName": "诗词",
        "tags": [
          {
            "id": 11,
            "name": "豁达"
          }
        ],
        "original": 0,
        "follow": false,
        "matchInfo": {
          "matchedFields": ["author", "content"],
          "score": 168.4,
          "highlights": [
            "author",
            "content"
          ]
        }
      }
    ]
  }
}
```

### 11.3 搜索历史接口

接口：

```text
GET /search/history
```

返回示例：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "text": "苏轼",
      "searchCount": 8,
      "lastSearchTime": "2026-03-23 18:20:11"
    },
    {
      "text": "李白",
      "searchCount": 4,
      "lastSearchTime": "2026-03-22 20:01:00"
    }
  ]
}
```

### 11.4 搜索点击埋点接口

接口：

```text
POST /search/click
```

请求示例：

```json
{
  "query": "苏轼",
  "normalizedQuery": "苏轼",
  "bizType": "quote",
  "bizId": 10234,
  "position": 1,
  "page": 1,
  "sort": "comprehensive"
}
```

---

## 12. 分阶段实施计划

### 12.1 阶段 0：方案评审与准备

周期：2 到 3 天

目标：

- 确认索引字段
- 确认建议词来源
- 确认排序规则
- 确认灰度与回滚策略

输出：

- DDL 定稿
- 接口定义定稿
- 排序规则表
- 缓存 key 定稿

### 12.2 阶段 1：低风险快速优化

周期：5 到 7 天

范围：

- 搜索历史改为 upsert
- 增加热搜统计表与 Redis 热搜榜
- 建议词增加历史、热词、前缀能力
- 去掉 `ORDER BY RAND()`
- 增加搜索结果短缓存
- 分类建议补齐

交付：

- 新版历史
- 新版 suggestions
- 热搜能力
- 结果缓存能力

### 12.3 阶段 2：索引层建设

周期：1.5 到 2 周

范围：

- 新建 `search_doc_index`
- 新建 `search_term_index`
- 新建 `search_suggest_term`
- 实现全量构建任务
- 实现增量同步任务
- 搜索改造为“先召回、后排序”

交付：

- 独立搜索索引
- 索引同步链路
- 候选集召回能力
- 搜索结果规则排序能力

### 12.4 阶段 3：质量提升

周期：1 周

范围：

- 多路召回调优
- 打分规则调优
- 搜索点击埋点反馈
- 热词趋势分
- 用户历史偏好加权

交付：

- 更稳定的综合排序
- 更优的 suggestions
- 个性化雏形

### 12.5 阶段 4：灰度与运维

周期：3 到 5 天

范围：

- 增加索引同步监控
- 增加缓存命中率监控
- 增加搜索耗时、召回数、点击率监控
- 灰度放量
- 回滚预案演练

交付：

- 可观测性
- 灰度策略
- 回滚预案

---

## 13. 灰度、兼容与回滚策略

### 13.1 双链路灰度

建议采用“双写双读可切换”方案：

- 老搜索链路保留
- 新搜索链路以开关形式逐步放量
- 可按环境、用户、流量百分比灰度切换

### 13.2 配置示例

```yaml
search:
  provider: mysql-redis
  suggestion:
    new-pipeline-enabled: true
  recall:
    term-index-enabled: true
    fallback-like-enabled: true
  rank:
    rule-rank-enabled: true
```

### 13.3 回滚方案

- 关闭新 provider 开关
- 回切旧搜索逻辑
- 保留索引表，不影响主业务链路

---

## 14. 风险点与规避方案

### 14.1 term 切分效果一般

风险：

- 第一版轻量分词能力有限

规避：

- 优先支持整词、前缀、标签名、作者名、分类名
- 后续再增加更细粒度 term 构建

### 14.2 prefix 词项膨胀

风险：

- 高基数数据下 prefix 可能爆炸

规避：

- 仅对高频 suggest term 生成 prefix
- 限制 prefix 长度
- 低频长词不生成 prefix term

### 14.3 索引同步延迟

风险：

- 数据新增后不能即时搜索到

规避：

- 接受最终一致
- 增加全量重建能力
- 同步失败支持重试

### 14.4 Redis 热 key

风险：

- 热门 prefix 或热门 query 集中访问

规避：

- prefix cache 使用短 TTL
- 热词榜分桶
- 结果缓存带 query hash

### 14.5 排序不可解释

风险：

- 规则排序不透明

规避：

- 返回内部 `matchInfo`
- 在日志中输出 score 组成
- 便于调参与回归测试

---

## 15. 最终建议

本方案建议最终落地为：

- MySQL 存储搜索主索引表、词项倒排表、建议词表、查询统计表、用户历史表
- Redis 存储前缀联想、热词榜、用户历史缓存、短结果缓存、候选集缓存
- 搜索采用“两阶段”：多路召回 + 规则排序
- 搜索建议采用“历史 + 热词 + 前缀 + 实体 + 内容”的融合管线
- 通过 `SearchProvider` 统一抽象底层实现，预留未来 ES 升级通道
- 搜索能力逐步从 `quote-service` 回归 `search-service`

该方案的优点：

- 不引入复杂组件，符合现阶段约束
- 相比当前方案能显著提升搜索质量与性能稳定性
- 架构职责清晰，利于长期演进
- 支持渐进实施和灰度回滚
- 为未来 ES 切换预留了标准化接口

---

# 评审 PPT / 文档目录式提纲

## 1. 背景与问题

1. 当前搜索链路概览
2. 当前实现存在的问题
3. 为什么现在要重构

## 2. 建设目标

1. 本次重构目标
2. 非目标说明
3. 约束条件说明

## 3. 总体架构方案

1. 新搜索架构分层
2. `search-service` 与 `quote-service` 职责划分
3. MySQL 与 Redis 分工
4. 未来扩展到 ES 的预留点

## 4. 数据模型设计

1. 搜索文档索引表
2. 搜索词项倒排表
3. 建议词表
4. 搜索统计表
5. 用户搜索历史表
6. 索引同步任务表

## 5. 核心链路设计

1. 搜索建议链路
2. 搜索结果链路
3. 索引同步链路
4. 缓存链路

## 6. 检索与排序策略

1. Query 预处理
2. 多路召回
3. 规则排序
4. 建议词融合策略

## 7. 接口设计

1. suggestions 接口
2. results 接口
3. history 接口
4. click 埋点接口

## 8. 分阶段实施计划

1. 阶段 0：评审与准备
2. 阶段 1：低风险快速优化
3. 阶段 2：索引层建设
4. 阶段 3：排序与建议优化
5. 阶段 4：灰度与运维

## 9. 风险与回滚

1. 风险项识别
2. 风险规避措施
3. 灰度策略
4. 回滚方案

## 10. 预期收益

1. 搜索质量收益
2. 性能收益
3. 架构收益
4. 长期扩展收益

---

# 核心类图 + 关键 SQL 草案 + 伪代码

## 1. 核心类图

```text
+-------------------+
|  SearchController |
+-------------------+
          |
          v
+-------------------+        +----------------------+
|   SearchService   |------->|  SearchHistoryService|
+-------------------+        +----------------------+
          |
          v
+-------------------+        +----------------------+
|   SearchProvider  |<-------| MysqlRedisSearchProv |
+-------------------+        +----------------------+
          |
          +------------------------------+
          |                              |
          v                              v
+---------------------+        +---------------------+
| MultiRouteRecallSvc |        |   SuggestService    |
+---------------------+        +---------------------+
          |                              |
          v                              v
+---------------------+        +---------------------+
|   RecallStrategy    |        |    SuggestSource    |
+---------------------+        +---------------------+
| ExactMatchRecall    |        | UserHistorySource   |
| PrefixRecall        |        | HotQuerySource      |
| TermRecall          |        | PrefixSuggestSource |
| TagRecall           |        | TagSuggestSource    |
| FallbackLikeRecall  |        | CategorySource      |
+---------------------+        | QuoteSuggestSource  |
          |                    +---------------------+
          v
+---------------------+
|     RankService     |
+---------------------+
| RuleBasedRankService|
+---------------------+
          |
          v
+---------------------+
| SearchDocIndexMapper|
+---------------------+
|SearchTermIndexMapper|
|SearchSuggestMapper  |
|SearchStatsMapper    |
+---------------------+
```

## 2. 关键 SQL 草案

### 2.1 用户历史 upsert

```sql
INSERT INTO user_search_history (
  user_id,
  query_text,
  query_text_normalized,
  search_count,
  last_search_time,
  deleted
)
VALUES (?, ?, ?, 1, NOW(), 0)
ON DUPLICATE KEY UPDATE
  query_text = VALUES(query_text),
  search_count = search_count + 1,
  last_search_time = NOW(),
  deleted = 0;
```

### 2.2 查询用户历史

```sql
SELECT
  query_text,
  search_count,
  last_search_time
FROM user_search_history
WHERE user_id = ?
  AND deleted = 0
ORDER BY last_search_time DESC, search_count DESC
LIMIT 20;
```

### 2.3 查询前缀建议

```sql
SELECT
  term_text,
  term_type,
  source_id,
  hot_score,
  quality_score
FROM search_suggest_term
WHERE prefix_text = ?
  AND status = 1
ORDER BY hot_score DESC, quality_score DESC, updated_time DESC
LIMIT 20;
```

### 2.4 term 倒排召回

```sql
SELECT
  biz_id,
  SUM(weight) AS recall_score
FROM search_term_index
WHERE biz_type = 'quote'
  AND term_normalized IN (?, ?, ?)
GROUP BY biz_id
ORDER BY recall_score DESC
LIMIT 100;
```

### 2.5 prefix 召回

```sql
SELECT
  biz_id,
  SUM(weight) AS recall_score
FROM search_term_index
WHERE biz_type = 'quote'
  AND term_type = 'PREFIX'
  AND term_normalized = ?
GROUP BY biz_id
ORDER BY recall_score DESC
LIMIT 100;
```

### 2.6 精确 tag/category/author 召回

```sql
SELECT
  biz_id,
  SUM(weight) AS recall_score
FROM search_term_index
WHERE biz_type = 'quote'
  AND (
    (term_type = 'TAG' AND term_normalized = ?)
    OR (term_type = 'CATEGORY' AND term_normalized = ?)
    OR (term_type = 'AUTHOR' AND term_normalized = ?)
  )
GROUP BY biz_id
ORDER BY recall_score DESC
LIMIT 100;
```

### 2.7 批量回表取搜索文档

```sql
SELECT
  biz_id,
  title,
  content,
  author_name,
  source,
  category_id,
  category_name,
  tag_ids_json,
  tag_names_text,
  is_original,
  view_count,
  like_count,
  comment_count,
  favorite_count,
  hot_score,
  quality_score,
  publish_time
FROM search_doc_index
WHERE biz_type = 'quote'
  AND biz_id IN (?,?,?,?,?)
  AND status = 1
  AND is_deleted = 0;
```

### 2.8 弱兜底召回

仅在召回不足时使用：

```sql
SELECT
  biz_id
FROM search_doc_index
WHERE biz_type = 'quote'
  AND status = 1
  AND is_deleted = 0
  AND search_text_normalized LIKE CONCAT('%', ?, '%')
ORDER BY hot_score DESC, publish_time DESC
LIMIT 50;
```

### 2.9 搜索统计 upsert

```sql
INSERT INTO search_query_stats (
  query_text,
  query_text_normalized,
  search_count,
  result_count_total,
  click_count,
  last_result_count,
  last_search_time,
  trend_score,
  hot_score
)
VALUES (?, ?, 1, ?, 0, ?, NOW(), 0, 0)
ON DUPLICATE KEY UPDATE
  query_text = VALUES(query_text),
  search_count = search_count + 1,
  result_count_total = result_count_total + VALUES(result_count_total),
  last_result_count = VALUES(last_result_count),
  last_search_time = NOW();
```

### 2.10 搜索点击更新

```sql
UPDATE search_query_stats
SET click_count = click_count + 1,
    updated_time = NOW()
WHERE query_text_normalized = ?;
```

## 3. 核心伪代码

### 3.1 搜索结果主流程

```java
public SearchPageResult search(SearchQuery query) {
    QueryContext context = queryPreprocessor.process(query);

    searchHistoryService.record(context.getUserId(), context.getRawQuery(), context.getNormalizedQuery());

    String cacheKey = cacheKeyBuilder.buildResultKey(context);
    SearchPageResult cached = searchCacheService.get(cacheKey);
    if (cached != null) {
        return cached;
    }

    List<RecallDoc> recallDocs = multiRouteRecallService.recall(context);
    List<Long> docIds = recallDocs.stream()
            .map(RecallDoc::getBizId)
            .distinct()
            .limit(300)
            .collect(Collectors.toList());

    List<SearchDocIndex> docs = searchDocIndexMapper.selectByBizIds("quote", docIds);

    List<RecallDoc> ranked = rankService.rank(context, mergeDocInfo(recallDocs, docs));

    SearchPageResult result = pageAssembler.build(context, ranked);

    searchStatsService.recordSearch(context, result.getTotal());
    searchCacheService.set(cacheKey, result, Duration.ofMinutes(3));

    return result;
}
```

### 3.2 多路召回流程

```java
public List<RecallDoc> recall(QueryContext context) {
    List<RecallDoc> merged = new ArrayList<>();

    merged.addAll(exactMatchRecallStrategy.recall(context));
    merged.addAll(prefixRecallStrategy.recall(context));
    merged.addAll(termRecallStrategy.recall(context));
    merged.addAll(tagRecallStrategy.recall(context));

    if (merged.size() < 50) {
        merged.addAll(fallbackLikeRecallStrategy.recall(context));
    }

    return deduplicateAndMergeScore(merged);
}
```

### 3.3 规则排序流程

```java
public List<RecallDoc> rank(QueryContext context, List<RecallDoc> docs) {
    for (RecallDoc doc : docs) {
        double textScore = textMatchScorer.score(context, doc);
        double hotScore = hotScoreScorer.score(doc);
        double freshnessScore = freshnessScorer.score(doc);
        double qualityScore = qualityScorer.score(doc);
        double personalizationScore = personalizationScorer.score(context, doc);

        doc.setFinalScore(textScore + hotScore + freshnessScore + qualityScore + personalizationScore);
    }

    return docs.stream()
            .sorted(Comparator.comparing(RecallDoc::getFinalScore).reversed())
            .collect(Collectors.toList());
}
```

### 3.4 搜索建议主流程

```java
public SuggestResult suggest(SuggestQuery query) {
    QueryContext context = queryPreprocessor.process(query);

    String cacheKey = cacheKeyBuilder.buildSuggestKey(context.getPrefix());
    SuggestResult cached = searchCacheService.get(cacheKey);
    if (cached != null) {
        return cached;
    }

    List<SuggestionItem> items = new ArrayList<>();
    items.addAll(userHistorySuggestSource.load(context));
    items.addAll(hotQuerySuggestSource.load(context));
    items.addAll(prefixSuggestSource.load(context));
    items.addAll(tagSuggestSource.load(context));
    items.addAll(categorySuggestSource.load(context));
    items.addAll(quoteSuggestSource.load(context));

    SuggestResult result = suggestAssembler.assemble(context, items);
    searchCacheService.set(cacheKey, result, Duration.ofMinutes(5));
    return result;
}
```

### 3.5 索引同步流程

```java
public void upsertDocument(String bizType, Long bizId) {
    QuoteAggregate aggregate = quoteSearchDataLoader.loadQuoteAggregate(bizId);
    if (aggregate == null || aggregate.isDeleted()) {
        deleteDocument(bizType, bizId);
        return;
    }

    SearchDocIndex doc = searchDocumentBuilder.build(aggregate);
    searchDocIndexMapper.upsert(doc);

    searchTermIndexMapper.deleteByBiz(bizType, bizId);
    List<SearchTermIndex> terms = searchTermBuilder.build(doc);
    searchTermIndexMapper.batchInsert(terms);

    suggestTermBuilder.refreshByDocument(doc);

    cacheInvalidator.invalidateByBiz(bizType, bizId);
}
```

### 3.6 热词更新流程

```java
public void recordSearch(QueryContext context, long resultCount) {
    searchQueryStatsMapper.upsert(
        context.getRawQuery(),
        context.getNormalizedQuery(),
        resultCount
    );

    redisTemplate.opsForZSet().incrementScore(
        "search:suggest:hot:global",
        context.getNormalizedQuery(),
        1.0
    );
}
```

---

## 16. 结论

该方案在不引入 ES 的前提下，通过“独立索引 + 多路召回 + 规则排序 + 多来源建议 + Redis 缓存”的方式，能够显著提升当前搜索功能质量，并为未来升级到更强搜索引擎预留清晰的演进路径。

建议按阶段逐步推进：

1. 先做历史、建议、热词和缓存优化
2. 再建设搜索索引与同步链路
3. 最后做排序、建议和个性化调优

整体风险可控、实施成本适中、收益明确，适合作为当前阶段搜索体系重构方案进入评审。
