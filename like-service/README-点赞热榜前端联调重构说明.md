# 点赞热榜前端联调重构说明

## 1. 文档目的

本文档用于指导前端对原“点赞热榜页面”进行联调与重构，重点说明：

- 接口怎么调用
- 返回字段怎么使用
- 榜单 Tab 怎么切换
- 排名、趋势、热度怎么展示
- 空状态、加载态、分页等页面行为怎么处理

适用对象：

- 前端开发
- 联调测试
- 产品验收

---

## 2. 接口信息

### 2.1 接口地址

`GET /like/hot`

后端入口：
[LikeController.java](d:\workspace\doubao\QingShu\like-service\like-service-server\src\main\java\org\doubao\like\service\controller\LikeController.java)

### 2.2 请求参数

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `type` | `string` | 否 | `all` | 榜单类型 |
| `page` | `number` | 否 | `1` | 页码 |
| `limit` | `number` | 否 | `20` | 每页数量 |
| `windowHours` | `number` | 否 | 空 | 仅 `all` 榜可选传 |

### 2.3 支持的 `type`

| type | 名称 | 页面建议名称 |
| --- | --- | --- |
| `all` | 总榜 | 热门榜 |
| `daily` | 日榜 | 24小时榜 |
| `weekly` | 周榜 | 本周榜 |
| `monthly` | 月榜 | 本月榜 |
| `rising` | 飙升榜 | 飙升榜 |

---

## 3. 推荐调用方式

### 3.1 默认首屏

建议默认加载：

```http
GET /like/hot?type=all&page=1&limit=20
```

### 3.2 切换榜单

切换 Tab 时，建议重新请求第一页：

```http
GET /like/hot?type=daily&page=1&limit=20
GET /like/hot?type=weekly&page=1&limit=20
GET /like/hot?type=monthly&page=1&limit=20
GET /like/hot?type=rising&page=1&limit=20
```

### 3.3 自定义总榜窗口

如果页面未来需要做“近 3 天热门”“近 7 天热门”，可以只对 `all` 榜传 `windowHours`：

```http
GET /like/hot?type=all&page=1&limit=20&windowHours=72
```

当前版本不建议前端对 `daily / weekly / monthly / rising` 自定义窗口，避免和后端配置榜单定义不一致。

---

## 4. 响应结构

返回 DTO：
[HotContentResponse.java](d:\workspace\doubao\QingShu\like-service\like-service-api\src\main\java\org\doubao\like\service\dto\response\HotContentResponse.java)

### 4.1 顶层结构示例

```json
{
  "page": 1,
  "limit": 20,
  "total": 128,
  "windowHours": 24,
  "type": "daily",
  "scoreSource": "recent_likes_24h",
  "generatedAt": "2026-03-23T14:30:00",
  "rankMeta": {
    "type": "daily",
    "windowHours": 24,
    "scoreSource": "recent_likes_24h",
    "refreshIntervalMinutes": 10,
    "risingMinCurrentLikes": null
  },
  "hotContents": [
    {
      "contentId": 1001,
      "rank": 1,
      "likeCount": 520,
      "recentLikeCount": 86,
      "heatValue": 1062,
      "rankChange": 2,
      "trend": "UP",
      "quote": {
        "id": 1001,
        "content": "示例文案",
        "authorId": 1
      }
    }
  ]
}
```

### 4.2 页面真正需要重点使用的字段

| 字段 | 用途 |
| --- | --- |
| `type` | 当前是哪张榜 |
| `scoreSource` | 显示榜单说明文案 |
| `rankMeta` | 显示榜单规则和刷新周期 |
| `hotContents[].rank` | 展示名次 |
| `hotContents[].rankChange` | 展示排名升降 |
| `hotContents[].trend` | 展示趋势标签 |
| `hotContents[].likeCount` | 展示总点赞数 |
| `hotContents[].recentLikeCount` | 展示周期点赞数 |
| `hotContents[].quote` | 渲染内容卡片 |

---

## 5. 前端字段映射建议

### 5.1 榜单标题映射

建议前端做一层映射：

```ts
const hotListTitleMap = {
  all: '热门榜',
  daily: '24小时榜',
  weekly: '本周榜',
  monthly: '本月榜',
  rising: '飙升榜',
}
```

### 5.2 榜单副标题映射

建议优先使用后端返回的 `scoreSource` 和 `rankMeta.windowHours` 生成说明。

推荐映射：

```ts
const scoreSourceTextMap = {
  blended_heat: '综合累计热度与近期热度',
  recent_likes_24h: '按近24小时点赞热度排序',
  recent_likes_7d: '按近7天点赞热度排序',
  recent_likes_30d: '按近30天点赞热度排序',
  rising_score: '按增长速度与增长量综合排序',
}
```

### 5.3 趋势标识映射

```ts
const trendTextMap = {
  NEW: '新上榜',
  UP: '上升',
  DOWN: '下降',
  HOT: '持续热门',
  STABLE: '稳定',
}
```

### 5.4 排名变化展示建议

| `rankChange` | 建议展示 |
| --- | --- |
| `null` | `新` / “新上榜” |
| `> 0` | `+N`，红色或强调色，上箭头 |
| `< 0` | `-N`，灰色或冷色，下箭头 |
| `0` | `-` 或 “持平” |

---

## 6. 页面结构建议

建议把页面拆成 4 个区域：

### 6.1 顶部榜单切换区

建议使用横向 Tab：

- 热门榜
- 24小时榜
- 本周榜
- 本月榜
- 飙升榜

交互建议：

- 点击切换后请求新榜单
- 切换时重置滚动和分页
- 当前选中态要明显

### 6.2 榜单说明区

建议展示：

- 当前榜单标题
- 榜单规则一句话说明
- 最近刷新周期说明

推荐展示来源：

- 标题：前端 type 映射
- 说明：`rankMeta.scoreSource`
- 刷新周期：`rankMeta.refreshIntervalMinutes`
- 飙升榜补充门槛：`rankMeta.risingMinCurrentLikes`

示例：

- `24小时榜 · 按近24小时点赞热度排序`
- `飙升榜 · 按增长速度与增长量综合排序 · 最低上榜点赞数 10`

### 6.3 榜单列表区

每条内容建议展示：

- 排名序号
- 趋势图标或标签
- 文案内容
- 作者信息或引用来源信息
- 总点赞数
- 当前窗口点赞数

推荐展示结构：

```text
[排名] [趋势]
文案内容
作者/来源
总点赞 520 · 本周期点赞 86
```

### 6.4 底部分页或加载更多

接口已经支持 `page + limit`，前端可选两种方式：

- 分页器
- 无限滚动 / 加载更多

如果是信息流风格，建议优先“加载更多”。

---

## 7. UI 展示建议

### 7.1 Top 3 特殊样式

建议给前 3 名做更强视觉：

- `1`：金色高亮
- `2`：银色高亮
- `3`：铜色高亮

### 7.2 飙升榜强调“增长感”

`rising` 榜建议额外强调：

- 趋势箭头
- “增长中”“飙升中”标签
- `recentLikeCount` 数值

### 7.3 热度说明优先展示 `recentLikeCount`

对于 `daily / weekly / monthly / rising`，页面上优先展示“本周期点赞”，再展示总点赞。

对于 `all`，可以优先展示总点赞，再用小字展示“近窗口热度”。

---

## 8. 页面状态处理建议

### 8.1 加载态

建议在切换榜单和首屏加载时显示 Skeleton，不建议只转圈。

### 8.2 空状态

当 `hotContents` 为空时，显示：

- 空列表插画或占位
- 文案建议：“当前榜单暂无内容”

### 8.3 错误态

请求失败时建议展示：

- “加载失败，请重试”
- 重试按钮

### 8.4 无更多数据

当：

`page * limit >= total`

可以显示：

- “没有更多了”

---

## 9. 联调注意事项

### 9.1 不要自己计算排名变化

前端直接使用后端返回的：

- `rank`
- `rankChange`
- `trend`

不要在前端自行二次推断，否则容易与后端快照逻辑不一致。

### 9.2 不要写死榜单规则文案

建议以后端返回的：

- `scoreSource`
- `rankMeta.windowHours`
- `rankMeta.risingMinCurrentLikes`

来生成榜单说明，避免后端调整规则后前端展示仍是旧文案。

### 9.3 `quote` 作为内容卡片主数据源

`quote` 字段里是前端渲染内容详情的主来源。联调时如果发现具体子字段名称和页面现有组件不一致，需要按你们现有 `quote` 卡片组件适配。

### 9.4 `rising` 榜样本过滤是后端完成的

前端不需要再做“最低点赞数过滤”。

---

## 10. 推荐前端实现步骤

### 第一步：替换接口契约

先把原热榜页面的数据源切换到新接口：

`GET /like/hot`

### 第二步：完成榜单 Tab

增加 5 个榜单 Tab：

- `all`
- `daily`
- `weekly`
- `monthly`
- `rising`

### 第三步：重构列表卡片

列表卡片建议新增这些展示位：

- `rank`
- `trend`
- `rankChange`
- `likeCount`
- `recentLikeCount`

### 第四步：增加榜单说明区

使用：

- `type`
- `scoreSource`
- `rankMeta`

生成榜单标题和副标题。

### 第五步：接入分页或加载更多

基于：

- `page`
- `limit`
- `total`

完成翻页逻辑。

---

## 11. 推荐验收点

前端联调完成后，建议至少验证以下场景：

- 默认进入是否展示 `all` 榜
- 切换到 `daily / weekly / monthly / rising` 是否正常
- `rankChange > 0 / < 0 / = 0 / null` 是否都能正确展示
- `rising` 榜是否能显示门槛说明
- 空榜、失败、无更多数据状态是否完整
- 页码切换或加载更多是否正确

---

## 12. 相关后端文件

| 能力 | 文件 |
| --- | --- |
| 接口入口 | [LikeController.java](d:\workspace\doubao\QingShu\like-service\like-service-server\src\main\java\org\doubao\like\service\controller\LikeController.java) |
| 查询组装 | [LikeServiceImpl.java](d:\workspace\doubao\QingShu\like-service\like-service-server\src\main\java\org\doubao\like\service\service\impl\LikeServiceImpl.java) |
| 榜单刷新管理 | [HotContentRankManager.java](d:\workspace\doubao\QingShu\like-service\like-service-server\src\main\java\org\doubao\like\service\service\HotContentRankManager.java) |
| 返回 DTO | [HotContentResponse.java](d:\workspace\doubao\QingShu\like-service\like-service-api\src\main\java\org\doubao\like\service\dto\response\HotContentResponse.java) |
| 类型定义 | [HotListType.java](d:\workspace\doubao\QingShu\like-service\like-service-api\src\main\java\org\doubao\like\service\enums\HotListType.java) |

---

## 13. 一句话结论

前端重构原热榜页面时，可以把它理解成：

“一个支持多榜单切换、趋势展示、分页加载、规则可解释的统一排行榜页面”

核心要点不是只展示“谁点赞多”，而是要把：

- 当前是哪个榜
- 为什么上榜
- 排名是升是降
- 当前周期热度如何

这 4 件事一起表达出来。
