#!/bin/bash

# 设置编码，防止乱码
export LANG=zh_CN.UTF-8

echo "========================================"
echo "@Autowired vs@Resource 演示验证脚本"
echo "问题 028 - 依赖注入区别"
echo "========================================"
echo ""

# 设置服务地址
BASE_URL="http://localhost:9510/interview-agent/question028"

echo "[1] 健康检查..."
echo ""
curl -s "$BASE_URL/health"
echo ""
echo ""

echo "[2] 使用@Resource(name=\"messageServiceImplA\") 注入 ServiceA..."
echo ""
curl -X POST "$BASE_URL/resource/by-name" \
  -H "Content-Type: application/json" \
  -d '{"message":"Hello from Resource injection","specifyImpl":true,"implName":"messageServiceImplA"}'
echo ""
echo ""

echo "[3] 使用@Resource(name=\"messageServiceImplB\") 注入 ServiceB..."
echo ""
curl -X POST "$BASE_URL/resource/by-name" \
  -H "Content-Type: application/json" \
  -d '{"message":"Hello from Resource injection","specifyImpl":true,"implName":"messageServiceImplB"}'
echo ""
echo ""

echo "[4] 使用@Autowired+@Qualifier(\"messageServiceImplA\") 注入 ServiceA..."
echo ""
curl -X POST "$BASE_URL/autowired/with-qualifier" \
  -H "Content-Type: application/json" \
  -d '{"message":"Hello from Autowired injection","specifyImpl":true,"implName":"messageServiceImplA"}'
echo ""
echo ""

echo "[5] 使用@Autowired+@Qualifier(\"messageServiceImplB\") 注入 ServiceB..."
echo ""
curl -X POST "$BASE_URL/autowired/with-qualifier" \
  -H "Content-Type: application/json" \
  -d '{"message":"Hello from Autowired injection","specifyImpl":true,"implName":"messageServiceImplB"}'
echo ""
echo ""

echo "[6] 获取详细对比说明..."
echo ""
curl -s "$BASE_URL/comparison"
echo ""
echo ""

echo "========================================"
echo "验证完成"
echo "========================================"
