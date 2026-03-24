# Search Service 重构联调与上线清单

## 1. 部署前准备

1. 在搜索库执行 [search-index-ddl.sql](d:/workspace/doubao/QingShu/search-service/search-service-server/docs/search-index-ddl.sql)。
2. 确认 `search-service-server` 与 `search-service-starter` 已使用最新代码构建。
3. 确认 RabbitMQ 中 `fanout.event.exchange` 可用。
4. 确认 `search-service` 实例可消费 `search.sync.queue`。

## 2. 初始化步骤

1. 启动 `quote-service`、`search-service`。
2. 调用 `POST /search/admin/rebuild` 进行一次全量索引重建。
3. 检查 `search_doc_index`、`search_term_index`、`search_suggest_term` 是否生成数据。

## 3. 联调检查点

1. 新增 quote 后，RabbitMQ 应收到 `QUOTE_EVENT`，且 `extInfo.action=SEARCH_SYNC`。
2. `search.sync.queue` 应消费该消息，并写入 `search_index_sync_task`。
3. 定时任务应自动处理 `search_index_sync_task`，最终将任务状态更新为 `SUCCESS`。
4. 新增或更新后的 quote 应可通过 `/search/results` 搜到。
5. 删除或下架后的 quote 不应再通过本地索引命中。
6. `/search/suggestions` 应优先从 `search_suggest_term` 返回结果，缺失时回退远端建议。

## 4. 自测清单

1. 新增 quote，校验 30 秒内能被搜索到。
2. 更新 quote 文本，校验旧关键词逐步失效、新关键词命中。
3. 删除 quote，校验搜索结果中消失。
4. 下架 quote，校验搜索结果中消失。
5. 草稿保存后不应影响已发布搜索结果；若后续发布，发布后应可命中。
6. 清空用户搜索历史后，`/search/history` 返回为空。
7. 搜索相同关键词多次后，`user_search_history` 与 `search_query_stats` 的计数应递增。

## 5. 回滚手段

1. 关闭搜索侧新索引读取流量，保留远端 `quote-service` 回退。
2. 暂停 `search.sync.queue` 消费或关闭定时任务处理。
3. 保留索引表数据，不影响主业务写链路。
