# LinkedList 原理验证脚本 (PowerShell)

$baseUrl = "http://localhost:9510/interview-agent/collection/linkedlist"

Write-Host "`n--- 1. 验证 LinkedList 核心操作 ---" -ForegroundColor Cyan
Invoke-RestMethod -Uri "$baseUrl/core-operations" -Method Get | ConvertTo-Json

Write-Host "`n--- 2. 验证 LinkedList 双端队列特性 ---" -ForegroundColor Cyan
Invoke-RestMethod -Uri "$baseUrl/deque-features" -Method Get | ConvertTo-Json

Write-Host "`n--- 3. 验证 SimpleLinkedList 自定义实现 ---" -ForegroundColor Cyan
Invoke-RestMethod -Uri "$baseUrl/simple-implementation" -Method Get | ConvertTo-Json

Write-Host "`n--- 4. 获取 LinkedList 底层原理总结 ---" -ForegroundColor Cyan
Invoke-RestMethod -Uri "$baseUrl/principle-summary" -Method Get | ConvertTo-Json

Write-Host "`n验证完成！请同时查看服务器控制台日志以获取详细运行过程。" -ForegroundColor Green
