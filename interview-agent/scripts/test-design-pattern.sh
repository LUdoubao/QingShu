#!/bin/bash
# ===================================================================
# Spring 设计模式演示 - 验证脚本（Linux/Mac 版本）
# 
# 用途：验证所有设计模式的演示接口
# 环境要求：
#   1. Java 运行环境
#   2. curl 命令可用
# 
# 使用方法：
#   1. 启动应用后运行此脚本
#   2. 查看输出结果
# ===================================================================

set -e

echo "========================================"
echo "Spring 设计模式演示 - 开始验证"
echo "========================================"
echo ""

# 设置服务地址
BASE_URL="http://localhost:9510/design-pattern"

# 检查服务是否可用
echo "[检查] 测试服务连通性..."
if ! curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/health" | grep -q "200"; then
    echo "[错误] 无法连接到服务，请确认服务已启动：$BASE_URL"
    exit 1
fi
echo "[成功] 服务连接正常"
echo ""

# 1. 工厂模式 + 单例模式
echo "========================================"
echo "[测试 1/10] 工厂模式 + 单例模式"
echo "========================================"
curl -s -X GET "$BASE_URL/factory-singleton" | python3 -m json.tool || curl -s -X GET "$BASE_URL/factory-singleton"
echo ""
echo ""

# 2. 原型模式
echo "========================================"
echo "[测试 2/10] 原型模式"
echo "========================================"
curl -s -X GET "$BASE_URL/prototype" | python3 -m json.tool || curl -s -X GET "$BASE_URL/prototype"
echo ""
echo ""

# 3. 建造者模式
echo "========================================"
echo "[测试 3/10] 建造者模式"
echo "========================================"
curl -s -X GET "$BASE_URL/builder" | python3 -m json.tool || curl -s -X GET "$BASE_URL/builder"
echo ""
echo ""

# 4. 代理模式
echo "========================================"
echo "[测试 4/10] 代理模式"
echo "========================================"
curl -s -X GET "$BASE_URL/proxy" | python3 -m json.tool || curl -s -X GET "$BASE_URL/proxy"
echo ""
echo ""

# 5. 装饰器模式
echo "========================================"
echo "[测试 5/10] 装饰器模式"
echo "========================================"
curl -s -X GET "$BASE_URL/decorator" | python3 -m json.tool || curl -s -X GET "$BASE_URL/decorator"
echo ""
echo ""

# 6. 策略模式
echo "========================================"
echo "[测试 6/10] 策略模式"
echo "========================================"
curl -s -X GET "$BASE_URL/strategy" | python3 -m json.tool || curl -s -X GET "$BASE_URL/strategy"
echo ""
echo ""

# 7. 模板方法模式
echo "========================================"
echo "[测试 7/10] 模板方法模式"
echo "========================================"
curl -s -X GET "$BASE_URL/template-method" | python3 -m json.tool || curl -s -X GET "$BASE_URL/template-method"
echo ""
echo ""

# 8. 观察者模式
echo "========================================"
echo "[测试 8/10] 观察者模式"
echo "========================================"
curl -s -X GET "$BASE_URL/observer" | python3 -m json.tool || curl -s -X GET "$BASE_URL/observer"
echo ""
echo ""

# 9. 责任链模式
echo "========================================"
echo "[测试 9/10] 责任链模式"
echo "========================================"
curl -s -X GET "$BASE_URL/chain-of-responsibility" | python3 -m json.tool || curl -s -X GET "$BASE_URL/chain-of-responsibility"
echo ""
echo ""

# 10. 委派模式
echo "========================================"
echo "[测试 10/10] 委派模式"
echo "========================================"
curl -s -X GET "$BASE_URL/delegation" | python3 -m json.tool || curl -s -X GET "$BASE_URL/delegation"
echo ""
echo ""

echo "========================================"
echo "所有测试完成！"
echo "========================================"
