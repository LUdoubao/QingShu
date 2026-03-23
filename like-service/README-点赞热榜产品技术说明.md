# 点赞热榜产品技术说明

## 1. 文档目的

本文档用于说明 `like-service` 中“点赞热榜”功能的产品目标、接口定义、榜单规则、刷新机制、配置项和核心实现，便于前端、后端、测试和运营统一理解。

---

## 2. 功能定位

点赞热榜不是简单的“总点赞数倒序”，而是一套可运营的榜单能力，目标效果接近抖音、小红书等平台的内容排行榜。

当前能力包括：

- 多榜单类型：总榜、日榜、周榜、月榜、飙升榜
- 排名变化：支持展示上升、下降、新上榜
- 趋势标识：支持前端展示 `UP`、`DOWN`、`NEW`、`HOT`、`STABLE`
- 快照轮换：支持当前榜和上一版榜单对比
- 定时刷新：榜单按配置周期自动刷新
- 启动预热：服务启动后主动预热榜单，避免冷启动空榜
- 配置化控制：刷新周期、榜单窗口、飙升榜门槛均可调整

---

## 3. 接口说明

### 3.1 接口地址

`GET /like/hot`

控制器位置：
[LikeController.java](d:\workspace\doubao\QingShu\like-service\like-service-server\src\main\java\org\doubao\like\service\controller\LikeController.java)

### 3.2 请求参数

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `type` | `String` | 否 | `all` | 榜单类型：`all` / `daily` / `weekly` / `monthly` / `rising` |
| `page` | `int` | 否 | `1` | 页码，从 1 开始 |
| `limit` | `int` | 否 | `20` | 每页条数，最大 100 |
| `windowHours` | `Integer` | 否 | 空 | 仅 `all` 榜支持自定义窗口；其他榜单使用系统配置 |

### 3.3 返回结构

返回 DTO 位置：
[HotContentResponse.java](d:\workspace\doubao\QingShu\like-service\like-service-api\src\main\java\org\doubao\like\service\dto\response\HotContentResponse.java)

顶层字段说明：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `page` | `Integer` | 当前页码 |
| `limit` | `Integer` | 每页条数 |
| `total` | `Long` | 榜单总条数 |
| `windowHours` | `Integer` | 当前榜单统计窗口 |
| `type` | `String` | 当前榜单类型 |
| `scoreSource` | `String` | 分数来源说明 |
| `generatedAt` | `LocalDateTime` | 榜单生成时间 |
| `rankMeta` | `RankMeta` | 榜单元信息 |
| `hotContents` | `List<HotContentItem>` | 榜单内容 |

`rankMeta` 字段说明：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `type` | `String` | 榜单类型 |
| `windowHours` | `Integer` | 统计窗口 |
| `scoreSource` | `String` | 排名分数来源 |
| `refreshIntervalMinutes` | `Integer` | 刷新周期，分钟 |
| `risingMinCurrentLikes` | `Long` | 飙升榜最小当前点赞门槛，仅 `rising` 有值 |

榜单项 `HotContentItem` 字段说明：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `contentId` | `Long` | 内容 ID |
| `rank` | `Integer` | 当前排名 |
| `likeCount` | `Long` | 总点赞数 |
| `recentLikeCount` | `Long` | 当前窗口内点赞数 |
| `heatValue` | `Long` | 服务端计算出的热度值 |
| `rankChange` | `Integer` | 相对上一版榜单的名次变化，正数表示上升 |
| `trend` | `String` | 趋势标识：`NEW` / `UP` / `DOWN` / `HOT` / `STABLE` |
| `quote` | `Map<String, Object>` | 文案内容详情 |

---

## 4. 榜单类型说明

榜单枚举位置：
[HotListType.java](d:\workspace\doubao\QingShu\like-service\like-service-api\src\main\java\org\doubao\like\service\enums\HotListType.java)

### 4.1 `all`

总榜。默认使用累计点赞和近期点赞混合计算热度，更强调“整体热度 + 当前趋势”。

`scoreSource`:

`blended_heat`

### 4.2 `daily`

日榜。统计最近 24 小时内的点赞表现。

`scoreSource`:

`recent_likes_24h`

### 4.3 `weekly`

周榜。统计最近 7 天的点赞表现。

`scoreSource`:

`recent_likes_7d`

### 4.4 `monthly`

月榜。统计最近 30 天的点赞表现。

`scoreSource`:

`recent_likes_30d`

### 4.5 `rising`

飙升榜。重点反映“增长速度”，而不是单纯总量。

`scoreSource`:

`rising_score`

飙升榜会综合考虑：

- 当前窗口点赞数
- 相比上一窗口的增长量
- 当前窗口相对上一窗口的增长比例

同时增加最小点赞门槛，避免小样本因为倍数高被误判冲榜。

---

## 5. 排名规则

### 5.1 普通热榜排序逻辑

实现位置：
[LikeServiceImpl.java](d:\workspace\doubao\QingShu\like-service\like-service-server\src\main\java\org\doubao\like\service\service\impl\LikeServiceImpl.java)

普通热榜最终会结合：

- `likeCount`：累计点赞数
- `recentLikeCount`：当前窗口点赞数

服务端热度值大致由两部分组成：

- 稳定分：累计点赞带来的长期热度
- 趋势分：近窗口点赞带来的短期爆发力

这样做的好处是：

- 老内容不会因为历史点赞量永久霸榜
- 新内容如果在短时间内快速爆发，也能冲上热榜

### 5.2 飙升榜排序逻辑

实现位置：
[HotContentRankManager.java](d:\workspace\doubao\QingShu\like-service\like-service-server\src\main\java\org\doubao\like\service\service\HotContentRankManager.java)

飙升榜分数核心包含：

- 当前窗口点赞数
- 增长量 `current - previous`
- 增长比例 `current / previous`

并且会过滤掉：

- 当前窗口点赞数小于 `risingMinCurrentLikes` 的内容

默认门槛为：

`10`

目的是防止类似“1 变 5”“2 变 6”这种小样本内容因为增幅大而误上飙升榜。

---

## 6. 排名变化与趋势说明

### 6.1 `rankChange`

`rankChange` 通过比较“当前榜单”和“上一版榜单”中的排名得到。

- 正数：排名上升
- 负数：排名下降
- `0`：排名不变
- `null`：新上榜

### 6.2 `trend`

趋势字段用于前端直接展示状态：

- `NEW`：新上榜
- `UP`：排名上升
- `DOWN`：排名下降
- `HOT`：排名未变但近期热度仍然很强
- `STABLE`：整体较稳定

---

## 7. Redis 设计

Key 工具类位置：
[RedisKeyUtil.java](d:\workspace\doubao\QingShu\like-service\like-service-server\src\main\java\org\doubao\like\service\utils\RedisKeyUtil.java)

每种榜单都有两类 key：

- `hot:contents:{type}:current`
- `hot:contents:{type}:last`

示例：

- `hot:contents:all:current`
- `hot:contents:all:last`
- `hot:contents:daily:current`
- `hot:contents:rising:last`

用途说明：

- `current`：当前生效榜单
- `last`：上一版榜单快照，用于计算 `rankChange`

---

## 8. 榜单刷新机制

### 8.1 定时刷新

刷新任务位置：
[HotContentRankRefreshTask.java](d:\workspace\doubao\like-service\like-service-server\src\main\java\org\doubao\like\service\task\HotContentRankRefreshTask.java)

刷新流程如下：

1. 根据榜单类型计算候选内容与分数
2. 将当前榜单复制为上一版快照
3. 清空旧的当前榜单
4. 写入新的当前榜单

当前会刷新全部榜单：

- `all`
- `daily`
- `weekly`
- `monthly`
- `rising`

### 8.2 启动预热

预热任务位置：
[HotContentRankPreloadRunner.java](d:\workspace\doubao\like-service\like-service-server\src\main\java\org\doubao\like\service\task\HotContentRankPreloadRunner.java)

系统启动后，如果配置开启预热，会主动执行一次全量榜单刷新。

目的：

- 避免服务刚启动时 Redis 为空
- 避免 `/like/hot` 在冷启动时返回空榜

---

## 9. 配置项说明

配置类位置：
[HotListProperties.java](d:\workspace\doubao\QingShu\like-service\like-service-server\src\main\java\org\doubao\like\service\config\HotListProperties.java)

默认配置位置：
[bootstrap.yml](d:\workspace\doubao\QingShu\like-service\like-service-starter\src\main\resources\bootstrap.yml)

当前支持的配置如下：

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `like.hot-list.enabled` | `true` | 是否启用热榜能力 |
| `like.hot-list.preload-on-startup` | `true` | 服务启动时是否预热榜单 |
| `like.hot-list.refresh-interval-minutes` | `10` | 榜单刷新周期 |
| `like.hot-list.refresh-limit` | `200` | 刷新时榜单最大候选数量 |
| `like.hot-list.all-default-window-hours` | `72` | 总榜默认窗口 |
| `like.hot-list.daily-window-hours` | `24` | 日榜窗口 |
| `like.hot-list.weekly-window-hours` | `168` | 周榜窗口 |
| `like.hot-list.monthly-window-hours` | `720` | 月榜窗口 |
| `like.hot-list.rising-window-hours` | `24` | 飙升榜当前窗口 |
| `like.hot-list.rising-previous-window-hours` | `24` | 飙升榜前序窗口 |
| `like.hot-list.rising-min-current-likes` | `10` | 飙升榜最小当前点赞门槛 |

---

## 10. 数据来源与数据库统计

Mapper 位置：

- [LikeRecordMapper.java](d:\workspace\doubao\QingShu\like-service\like-service-server\src\main\java\org\doubao\like\service\mapper\LikeRecordMapper.java)
- [LikeRecordMapper.xml](d:\workspace\doubao\QingShu\like-service\like-service-server\src\main\resources\mapper\LikeRecordMapper.xml)

榜单统计主要依赖 `like_records` 表，并且只统计：

`liked = 1`

时间窗口相关统计主要使用：

- `updated_time`

支持的统计能力包括：

- 指定内容的总点赞数
- 指定内容的近窗口点赞数
- 指定时间段内的点赞数
- 指定窗口下的 TopN 内容

---

## 11. 核心代码位置

| 能力 | 文件 |
| --- | --- |
| 接口入口 | [LikeController.java](d:\workspace\doubao\QingShu\like-service\like-service-server\src\main\java\org\doubao\like\service\controller\LikeController.java) |
| 榜单查询组装 | [LikeServiceImpl.java](d:\workspace\doubao\QingShu\like-service\like-service-server\src\main\java\org\doubao\like\service\service\impl\LikeServiceImpl.java) |
| 榜单刷新与候选池管理 | [HotContentRankManager.java](d:\workspace\doubao\QingShu\like-service\like-service-server\src\main\java\org\doubao\like\service\service\HotContentRankManager.java) |
| 定时刷新 | [HotContentRankRefreshTask.java](d:\workspace\doubao\like-service\like-service-server\src\main\java\org\doubao\like\service\task\HotContentRankRefreshTask.java) |
| 启动预热 | [HotContentRankPreloadRunner.java](d:\workspace\doubao\like-service\like-service-server\src\main\java\org\doubao\like\service\task\HotContentRankPreloadRunner.java) |
| 配置项 | [HotListProperties.java](d:\workspace\doubao\QingShu\like-service\like-service-server\src\main\java\org\doubao\like\service\config\HotListProperties.java) |
| Redis Key | [RedisKeyUtil.java](d:\workspace\doubao\QingShu\like-service\like-service-server\src\main\java\org\doubao\like\service\utils\RedisKeyUtil.java) |
| 统计 SQL | [LikeRecordMapper.xml](d:\workspace\doubao\QingShu\like-service\like-service-server\src\main\resources\mapper\LikeRecordMapper.xml) |

---

## 12. 当前能力总结

当前点赞热榜已经具备以下产品和技术能力：

- 支持 `all / daily / weekly / monthly / rising`
- 支持排名变化与趋势展示
- 支持多榜单 Redis 快照轮换
- 支持配置化刷新与启动预热
- 支持飙升榜防小样本误判
- 支持前端通过 `scoreSource` 和 `rankMeta` 明确理解榜单含义

这意味着该功能已经从“基础点赞排行”升级为“可解释、可运营、可调优”的正式热榜系统。
