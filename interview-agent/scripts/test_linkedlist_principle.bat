@echo off
set BASE_URL=http://localhost:9510/interview-agent/collection/linkedlist

echo --- 1. 验证 LinkedList 核心操作 ---
curl -X GET "%BASE_URL%/core-operations"
echo.

echo --- 2. 验证 LinkedList 双端队列特性 ---
curl -X GET "%BASE_URL%/deque-features"
echo.

echo --- 3. 验证 SimpleLinkedList 自定义实现 ---
curl -X GET "%BASE_URL%/simple-implementation"
echo.

echo --- 4. 获取 LinkedList 底层原理总结 ---
curl -X GET "%BASE_URL%/principle-summary"
echo.

echo 验证完成！请同时查看服务器控制台日志以获取详细运行过程。
pause
