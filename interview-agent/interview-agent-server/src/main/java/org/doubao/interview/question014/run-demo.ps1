# AQS 面试题代码示例 - PowerShell 运行脚本
# 编码：UTF-8

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "AQS 面试题代码示例" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 设置工作目录
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

# 创建输出目录
$outputDir = "..\..\..\..\target\classes"
if (-not (Test-Path $outputDir)) {
    Write-Host "[信息] 创建输出目录..." -ForegroundColor Yellow
    New-Item -ItemType Directory -Path $outputDir -Force | Out-Null
}

Write-Host "[步骤 1/3] 编译 Java 源代码..." -ForegroundColor Green
Write-Host ""

# 编译所有 Java 文件
$javacArgs = @(
    "-encoding", "UTF-8",
    "-d", $outputDir,
    "org\doubao\interview\question014\aqs\SimpleAQS.java",
    "org\doubao\interview\question014\aqs\SimpleReentrantLock.java",
    "org\doubao\interview\question014\aqs\LockSupport.java",
    "org\doubao\interview\question014\demo\AQSDemo.java",
    "org\doubao\interview\question014\demo\LockSupportDemo.java"
)

& javac $javacArgs

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "[错误] 编译失败！请检查错误信息。" -ForegroundColor Red
    Write-Host ""
    pause
    exit 1
}

Write-Host ""
Write-Host "[成功] 编译完成！" -ForegroundColor Green
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "[步骤 2/3] 运行 AQS 使用示例..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

& java -cp $outputDir -Dfile.encoding=UTF-8 org.doubao.interview.question014.demo.AQSDemo

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "[错误] 运行 AQSDemo 失败！" -ForegroundColor Red
    Write-Host ""
    pause
    exit 1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "[步骤 3/3] 运行 LockSupport 使用示例..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

& java -cp $outputDir -Dfile.encoding=UTF-8 org.doubao.interview.question014.demo.LockSupportDemo

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "[错误] 运行 LockSupportDemo 失败！" -ForegroundColor Red
    Write-Host ""
    pause
    exit 1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "[完成] 所有示例运行成功！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "代码位置说明：" -ForegroundColor Yellow
Write-Host "- 核心实现：aqs\SimpleAQS.java（AQS 框架实现）" 
Write-Host "- 锁实现：aqs\SimpleReentrantLock.java（可重入锁）"
Write-Host "- 工具类：aqs\LockSupport.java（阻塞/唤醒工具）"
Write-Host "- 使用示例：demo\AQSDemo.java"
Write-Host "- 工具示例：demo\LockSupportDemo.java"
Write-Host ""
Write-Host "所有代码均包含详细中文注释，重点概念有额外说明。" -ForegroundColor Green
Write-Host ""
pause
