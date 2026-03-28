#!/bin/bash
# AQS 面试题代码示例 - 编译和运行脚本
# 编码：UTF-8

echo "========================================"
echo "AQS 面试题代码示例"
echo "========================================"
echo ""

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# 创建输出目录
if [ ! -d "../../../target/classes" ]; then
    echo "[信息] 创建输出目录..."
    mkdir -p ../../../target/classes
fi

echo "[步骤 1/3] 编译 Java 源代码..."
echo ""

# 编译所有 Java 文件（包含详细中文注释）
javac -encoding UTF-8 -d ../../../target/classes \
    org/doubao/interview/question014/aqs/SimpleAQS.java \
    org/doubao/interview/question014/aqs/SimpleReentrantLock.java \
    org/doubao/interview/question014/aqs/LockSupport.java \
    org/doubao/interview/question014/demo/AQSDemo.java \
    org/doubao/interview/question014/demo/LockSupportDemo.java

if [ $? -ne 0 ]; then
    echo ""
    echo "[错误] 编译失败！请检查错误信息。"
    echo ""
    exit 1
fi

echo ""
echo "[成功] 编译完成！"
echo ""
echo "========================================"
echo "[步骤 2/3] 运行 AQS 使用示例..."
echo "========================================"
echo ""

java -cp ../../../target/classes -Dfile.encoding=UTF-8 org.doubao.interview.question014.demo.AQSDemo

if [ $? -ne 0 ]; then
    echo ""
    echo "[错误] 运行 AQSDemo 失败！"
    echo ""
    exit 1
fi

echo ""
echo "========================================"
echo "[步骤 3/3] 运行 LockSupport 使用示例..."
echo "========================================"
echo ""

java -cp ../../../target/classes -Dfile.encoding=UTF-8 org.doubao.interview.question014.demo.LockSupportDemo

if [ $? -ne 0 ]; then
    echo ""
    echo "[错误] 运行 LockSupportDemo 失败！"
    echo ""
    exit 1
fi

echo ""
echo "========================================"
echo "[完成] 所有示例运行成功！"
echo "========================================"
echo ""
echo "代码位置说明："
echo "- 核心实现：aqs/SimpleAQS.java（AQS 框架实现）"
echo "- 锁实现：aqs/SimpleReentrantLock.java（可重入锁）"
echo "- 工具类：aqs/LockSupport.java（阻塞/唤醒工具）"
echo "- 使用示例：demo/AQSDemo.java"
echo "- 工具示例：demo/LockSupportDemo.java"
echo ""
echo "所有代码均包含详细中文注释，重点概念有额外说明。"
echo ""
