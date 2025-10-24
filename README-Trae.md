# README-Trae — 输出日志

本文件用于记录 Collections 项目的运行与解析输出，便于复盘与问题定位。

## 项目概览
- 多模块结构：`universe-reader`(下载器)、`particle-parser`(解析器)、`blackhole-storager`(存储器)、`who-wander`(任务启动器)、`single-player`(独立示例)
- 关键技术：`HttpClient5` 下载、`Jsoup` HTML 解析、`org.json` JSON 处理、并发队列+线程池
- 数据流：`SingleUniverse.send(HttpRequest)` → 下载队列 → 生成 `HttpResponse` → `ParticleParser` 从响应队列取出 → `Index` 路由到具体解析器 → `process` 执行解析与派发/存储

## 运行方式
- 构建全项目：
  - `mvn -f d:\Trae area\Collections\pom.xml -q -DskipTests package`
- 运行建议：
  - 先运行 `single-player` 的 `com.hzwyjxy.Main` 验证基础 GET
  - 再运行 `who-wander` 的 `matrix.election.BBCNews` 验证下载与解析联动

## 日志规范
- 日志项包含：`时间`、`入口/模块`、`请求URL`、`Category`、`关键输出`、`耗时/状态码`
- 解析器输出以模块前缀标注，如 `BBCListParser`、`BBCDetailParser`
- 失败重试：当 `checkSuccess=false` 时会复投请求；日志记录重试次数与最后状态

## 采集运行记录

### 2025-10-24 — 单例下载器 + BBC 详情解析（示例）
- 入口：`who-wander` → `matrix.election.BBCNews.main`
- 初始化：`SingleUniverse.create(10)`、`ParticleParser(ElectionIndex)`
- 发送：`GET https://www.bbc.com/news/articles/c23k0d09d4do`
- 解析：`BBCDetailParser`

```
[2025-10-24 10:05:12] [BBCDetailParser] 达到bbc detail
[2025-10-24 10:05:12] 请求URL: https://www.bbc.com/news/articles/c23k0d09d4do
[2025-10-24 10:05:12] 透传消息: {"searchKey":"searchKey"}
[2025-10-24 10:05:12] 正文抽取: <article> ... 文本长度: 12345 chars
[2025-10-24 10:05:12] 状态码: 200, 耗时: 1340ms
```

### 2025-10-24 — BBC 列表解析派发（示例）
- 入口：`who-wander` → `matrix.election.BBCNews.main`
- 发送：`GET https://web-cdn.api.bbci.co.uk/xd/content-collection/...&page=0&size=9`
- 解析：`BBCListParser`，派发详情 URL 至 `SingleUniverse`

```
[2025-10-24 10:08:33] [BBCListParser] 达到bbc
[2025-10-24 10:08:33] 请求URL: https://web-cdn.api.bbci.co.uk/xd/content-collection/...
[2025-10-24 10:08:33] 解析数据条数: 9
[2025-10-24 10:08:33] 派发详情: /news/articles/c23k0d09d4do → https://www.bbc.com/news/articles/c23k0d09d4do
[2025-10-24 10:08:33] 状态码: 200, 耗时: 980ms
```

### 2025-10-24 — single-player 基础 GET（示例）
- 入口：`single-player` → `com.hzwyjxy.Main`
- 发送：`GET https://www.baidu.com`

```
[2025-10-24 10:11:02] [Main] Hello world!
[2025-10-24 10:11:02] 状态码: 200, 耗时: 420ms
[2025-10-24 10:11:02] 页面片段: <!DOCTYPE html><html>...<title>百度一下，你就知道</title>...
```

## 存储与落盘
- 暂存工具：`blackhole-storager/localfile/FileUtils.appendLine(path, line)` 可用于写入 CSV/NDJSON
- 建议：在解析器 `process` 中拼装结构化文本并调用 `appendLine` 统一落盘

## 问题与待办
- 失败重试策略需要区分网络错误/结构变化并限流
- 存储层扩展：Redis/SQL 等
- 代理/鉴权配置：在 `BaseHttpDownloader` 中开放代理与头部策略