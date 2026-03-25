# 搜索功能前端联调说明

## 1. 文档目标

本文档用于指导前端开发或智能体重构搜索相关页面与交互，适用于以下能力联调：

- 搜索首页
- 搜索建议词面板
- 搜索结果页
- 搜索历史
- 清空历史

文档基于当前后端实现整理，重点说明：

- 接口协议
- 分页规则
- 页面状态机
- 前端推荐交互
- 联调注意事项

## 2. 接口总览

搜索服务统一前缀：

```text
/search
```

当前已开放接口：

- `GET /search/suggestions` 获取建议词
- `GET /search/results` 获取搜索结果
- `GET /search/history` 获取搜索历史
- `POST /search/clear` 清空搜索历史

统一返回结构：

```json
{
  "code": 200,
  "message": "Success",
  "data": {}
}
```

说明：

- `code = 200` 视为成功
- `code != 200` 视为失败
- 前端不要只判断 HTTP 状态码，必须同时判断业务 `code`

## 3. 搜索建议词接口

### 3.1 请求

```http
GET /search/suggestions?keyword=苏
```

请求参数：

- `keyword: string` 必填，用户输入的当前关键词

### 3.2 返回结构

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "quotes": ["苏轼", "苏轼 定风波"],
    "tags": ["诗词", "宋词"],
    "categories": []
  }
}
```

字段说明：

- `quotes`: 搜索词/标题/作者相关建议
- `tags`: 标签建议
- `categories`: 分类建议

### 3.3 前端使用建议

- 输入框有值时再请求
- 建议做 `250ms ~ 400ms` 防抖
- 若连续输入，前端应取消旧请求或只接收最后一次响应
- 建议词面板优先展示顺序：
  - `quotes`
  - `tags`
  - `categories`
- 当三个数组都为空时，展示“暂无建议”

### 3.4 推荐交互

- 点击建议词后：
  - 回填输入框
  - 直接跳转结果页
- Enter 键默认使用当前输入框值发起搜索

## 4. 搜索结果接口

### 4.1 请求

```http
GET /search/results?keyword=苏轼&page=1&size=10&type=quote
```

请求参数：

- `keyword: string` 必填，搜索关键词
- `page: number` 选填，默认 `1`
- `size: number` 选填，默认 `10`
- `type: string` 必填，当前仅支持 `quote`

### 4.2 返回结构

`data` 为 MyBatis Plus 的 `Page` 结构，前端当前需要重点使用以下字段：

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "records": [
      {
        "id": 101,
        "content": "人生若只如初见",
        "author": "纳兰性德",
        "source": "木兰词",
        "categoryName": "诗词",
        "categoryId": 3,
        "tags": [
          { "id": 1, "name": "古诗" }
        ],
        "original": 1,
        "userInfo": null,
        "follow": false
      }
    ],
    "total": 34,
    "size": 10,
    "current": 1,
    "pages": 4
  }
}
```

字段说明：

- `records`: 当前页数据
- `total`: 当前关键词下的总记录数
- `size`: 每页条数
- `current`: 当前页码，从 `1` 开始
- `pages`: 总页数

前端联调时可忽略以下字段：

- `orders`
- `optimizeCountSql`
- `searchCount`
- `countId`
- `maxLimit`

## 5. 分页联调约束

### 5.1 必须遵守的规则

- 页码从 `1` 开始
- 前端“是否还有下一页”以 `current < pages` 判断
- 前端不要用 `records.length === size` 推断是否还有下一页
- 前端不要自己计算总页数，直接使用后端返回的 `pages`

### 5.2 推荐分页策略

推荐使用“页码分页 + 列表追加”：

- 首次搜索：请求 `page=1`
- 上拉加载：请求 `page=current+1`
- 新关键词搜索时：必须清空旧列表并重置页码为 `1`

### 5.3 去重建议

虽然当前后端会尽量避免重复，但前端追加列表时建议按 `id` 做一次去重，避免异常情况下出现重复卡片。

## 6. 搜索历史接口

### 6.1 获取历史

```http
GET /search/history
```

返回示例：

```json
{
  "code": 200,
  "message": "Success",
  "data": [
    {
      "id": 1,
      "userId": 1001,
      "keyword": "苏轼",
      "createdTime": "2026-03-25T10:20:30"
    }
  ]
}
```

说明：

- 当前最多返回最近 `20` 条
- 后端会在 `/search/results` 成功进入搜索时自动记录历史

### 6.2 清空历史

```http
POST /search/clear
```

返回示例：

```json
{
  "code": 200,
  "message": "Success",
  "data": null
}
```

推荐交互：

- 点击“清空历史”前弹确认框
- 成功后直接清空页面历史列表

## 7. 页面重构建议

### 7.1 页面拆分建议

建议拆成以下页面或状态区域：

- 搜索首页
  - 搜索框
  - 搜索历史
  - 热门建议词或默认提示
- 搜索建议浮层
  - 输入联想
  - 标签建议
  - 分类建议
- 搜索结果页
  - 顶部搜索框
  - 结果列表
  - 空态
  - 加载中
  - 分页加载

### 7.2 推荐状态机

建议前端显式维护以下状态：

- `idle`: 初始状态
- `typing`: 输入中
- `suggest-loading`: 建议词加载中
- `result-loading`: 首屏结果加载中
- `result-success`: 搜索成功
- `result-empty`: 搜索成功但无结果
- `result-error`: 搜索失败
- `result-loading-more`: 下一页加载中

### 7.3 推荐数据结构

前端页面层建议维护：

```ts
type SearchPageState = {
  keyword: string
  page: number
  size: number
  total: number
  pages: number
  list: SearchResultItem[]
  hasMore: boolean
  loading: boolean
  loadingMore: boolean
  suggestionLoading: boolean
  errorMessage: string
}
```

其中：

- `hasMore = current < pages`
- `page` 表示当前已成功加载到的页码

## 8. 推荐联调流程

### 8.1 首页

1. 页面进入时请求 `/search/history`
2. 无输入时展示搜索历史
3. 点击历史词后直接跳转结果页

### 8.2 输入联想

1. 用户开始输入
2. 前端防抖请求 `/search/suggestions`
3. 渲染建议词面板
4. 点击某项建议，跳转结果页

### 8.3 结果页

1. 进入页面时从路由读取 `keyword`
2. 请求 `/search/results?page=1&size=10&type=quote`
3. 使用 `records` 渲染列表
4. 使用 `total/pages/current` 控制分页和统计
5. 上拉时继续请求下一页

## 9. 空态与异常态建议

### 9.1 建议词为空

展示：

```text
暂无相关建议
```

### 9.2 搜索结果为空

展示：

```text
暂无相关内容，换个关键词试试
```

### 9.3 搜索失败

展示：

```text
搜索失败，请稍后重试
```

并提供：

- 重试按钮
- 返回搜索首页入口

## 10. 联调注意事项

### 10.1 请求频率

- `/search/suggestions` 必须做防抖
- `/search/results` 不要在同一关键词下重复并发发起多页请求

### 10.2 关键词切换

当用户修改关键词后：

- 清空旧列表
- 重置 `page = 1`
- 重置 `total/pages/hasMore`
- 丢弃旧请求回包

### 10.3 分页追加

下一页回包成功后：

- 追加 `records`
- 更新 `current/pages/total`
- 更新 `hasMore`

下一页失败后：

- 保留旧列表
- 仅提示“加载更多失败”
- 允许用户再次触发加载

### 10.4 不要依赖内部字段

前端不要依赖 MyBatis Plus 的内部字段行为，例如：

- `orders`
- `optimizeCountSql`
- `searchCount`

只依赖以下字段即可：

- `records`
- `total`
- `size`
- `current`
- `pages`

## 11. 推荐联调验收清单

- 输入关键词后能正常拉取建议词
- 点击建议词能进入结果页
- 结果页第一页正常显示 `10` 条以内数据
- 第二页可以正常加载，不会出现 `current=2` 但 `pages=1`
- `total/pages` 在翻页时保持稳定
- 新关键词搜索会清空旧结果
- 无结果时展示空态
- 接口失败时展示错误态
- 搜索历史可正常展示
- 清空历史后 UI 立即刷新

## 12. 给智能体的实现建议

若由前端智能体自动重构页面，建议按以下顺序执行：

1. 先抽象搜索 API 层
2. 再实现搜索首页状态管理
3. 再实现建议词浮层
4. 再实现结果页列表与分页
5. 最后补空态、错误态、埋点与样式

推荐智能体实现时遵循：

- 所有接口统一走一个 `searchApi` 模块
- 搜索建议与搜索结果拆分缓存键
- 使用显式状态字段，不要只依赖隐式布尔组合
- 新旧关键词请求必须可中断或可丢弃旧响应

## 13. 文档对应后端位置

接口入口：
[SearchController.java](d:/workspace/doubao/QingShu/search-service/search-service-server/src/main/java/org/doubao/search/service/controller/SearchController.java)

建议词 DTO：
[SearchSuggestionDTO.java](d:/workspace/doubao/QingShu/search-service/search-service-api/src/main/java/org/doubao/search/service/dto/SearchSuggestionDTO.java)

结果 DTO：
[SearchResultDTO.java](d:/workspace/doubao/QingShu/search-service/search-service-api/src/main/java/org/doubao/search/service/dto/SearchResultDTO.java)

统一返回结构：
[Result.java](d:/workspace/doubao/QingShu/mall-common/src/main/java/org/doubao/mall/common/entity/Result.java)
