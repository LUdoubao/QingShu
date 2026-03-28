# StampedLock 应用场景演示 - 运行脚本
Write-Host "╔════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║   StampedLock 面试题代码              ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

$JAVA_CMD = "java"
$MAIN_CLASS = "org.doubao.interview.question019.demo.StampedLockDemo"
$CLASSPATH = "D:\workspace\doubao\QingShu\interview-agent\interview-agent-server\target\classes"

Write-Host "[步骤 1/2] 编译 Java 代码..." -ForegroundColor Yellow

Set-Location "D:\workspace\doubao\QingShu\interview-agent\interview-agent-server\src\main\java"
javac -encoding UTF-8 `
    -d D:\workspace\doubao\QingShu\interview-agent\interview-agent-server\target\classes `
    org/doubao/interview/question019/concurrent/SimpleStampedLock.java `
    org/doubao/interview/question019/demo/StampedLockDemo.java

if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ 编译失败！请检查错误信息。" -ForegroundColor Red
    exit 1
}

Write-Host "✓ 编译成功！" -ForegroundColor Green
Write-Host ""

Write-Host "[步骤 2/2] 运行 StampedLock 演示程序..." -ForegroundColor Yellow
Write-Host ""

& $JAVA_CMD -cp $CLASSPATH $MAIN_CLASS

Write-Host ""
Write-Host "═══════════════════════════════════════" -ForegroundColor Cyan
Write-Host "演示完成！" -ForegroundColor Green
Write-Host "═══════════════════════════════════════" -ForegroundColor Cyan
