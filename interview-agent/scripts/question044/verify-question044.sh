#!/bin/bash

# 设置编码，防止乱码
export LANG=zh_CN.UTF-8

echo "========================================"
echo "Spring @Async 原理与使用注意点演示验证脚本"
echo "问题 044 - 异步任务原理、自调用问题、线程池配置"
echo "========================================"
echo ""

# 设置服务地址
BASE_URL="http://localhost:9510/interview-agent/question044"

echo "[1] 健康检查..."
echo ""
curl -s "$BASE_URL/health"
echo ""
echo ""

echo "[2] 获取@Async 底层源码级原理解析..."
echo ""
curl -s "$BASE_URL/principle"
echo ""
echo ""

echo "[3] 演示基础异步用法（无返回值）..."
echo ""
curl -X POST "$BASE_URL/basic/execute" \
  -H "Content-Type: application/json" \
  -d '{"taskType":"normal","message":"基础异步测试","sleepTime":2000}'
echo ""
echo ""

echo "[4] 演示返回 CompletableFuture 的异步用法..."
echo ""
curl -X POST "$BASE_URL/future/execute" \
  -H "Content-Type: application/json" \
  -d '{"taskType":"future","message":"Future 异步测试","sleepTime":1000}'
echo ""
echo ""

echo "[5] 演示带超时控制的异步用法..."
echo ""
curl -X POST "$BASE_URL/timeout/execute" \
  -H "Content-Type: application/json" \
  -d '{"taskType":"timeout","message":"超时控制测试","sleepTime":1000}'
echo ""
echo ""

echo "[6] 演示自调用失效问题（错误示例）..."
echo ""
curl -X POST "$BASE_URL/pitfall/self-call" \
  -H "Content-Type: application/json" \
  -d '{"taskType":"selfInvoke","message":"自调用测试","sleepTime":1000}'
echo ""
echo ""

echo "[7] 演示正确的自调用解决方案..."
echo ""
curl -X POST "$BASE_URL/solution/self-call" \
  -H "Content-Type: application/json" \
  -d '{"taskType":"selfInvoke","message":"正确自调用测试","sleepTime":1000}'
echo ""
echo ""

echo "========================================"
echo "验证完成"
echo "========================================"
echo ""
echo "【观察日志】"
echo "启动服务后，请观察控制台日志："
echo "- 基础异步：提交后立即返回，任务在独立线程执行"
echo "- Future 异步：返回 CompletableFuture，可以获取结果"
echo "- 超时控制：设置超时时间，防止任务执行过长"
echo "- 自调用问题：❌ 同步执行，@Async 不生效"
echo "- 正确方案：✅ 异步执行，通过注入的 Bean 调用"
echo ""
echo "【关键对比】"
echo "注意对比 [6] 和 [7] 的执行线程："
echo "- [6] 的线程是 main/http-nio（同步）"
echo "- [7] 的线程是 async-task-*（异步）"
echo ""
