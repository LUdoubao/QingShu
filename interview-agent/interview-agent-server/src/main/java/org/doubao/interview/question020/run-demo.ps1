# ThreadLocal 应用场景演示 - 运行脚本
Write-Host "╔════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║   ThreadLocal 面试题代码              ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

$JAVA_CMD = "java"
$APP_CLASS = "org.doubao.interview.question020.demo.ThreadLocalDemo"
$PRINCIPLE_CLASS = "org.doubao.interview.question020.demo.ThreadLocalPrincipleDemo"
$CLASSPATH = "D:\workspace\doubao\QingShu\interview-agent\interview-agent-server\target\classes"

Write-Host "[步骤 1/2] 编译 Java 代码..." -ForegroundColor Yellow

Set-Location "D:\workspace\doubao\QingShu\interview-agent\interview-agent-server\src\main\java"
javac -encoding UTF-8 `
    -d D:\workspace\doubao\QingShu\interview-agent\interview-agent-server\target\classes `
    org/doubao/interview/question020/concurrent/SimpleThreadLocal.java `
    org/doubao/interview/question020/demo/ThreadLocalDemo.java `
    org/doubao/interview/question020/demo/ThreadLocalPrincipleDemo.java

if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ 编译失败！请检查错误信息。" -ForegroundColor Red
    exit 1
}

Write-Host "✓ 编译成功！" -ForegroundColor Green
Write-Host ""

Write-Host "请选择要运行的演示程序:" -ForegroundColor Yellow
Write-Host "  [1] 应用场景演示（用户上下文、traceId、SimpleDateFormat）"
Write-Host "  [2] 原理深度演示（线程隔离、弱引用、线性探测）"
Write-Host "  [3] 运行全部演示"
Write-Host ""
$choice = Read-Host "请输入选项 (1/2/3)"

if ($choice -eq "1" -or $choice -eq "3") {
    Write-Host ""
    Write-Host "[运行] 应用场景演示程序..." -ForegroundColor Yellow
    Write-Host ""
    & $JAVA_CMD -cp $CLASSPATH $APP_CLASS
    Write-Host ""
}

if ($choice -eq "2" -or $choice -eq "3") {
    Write-Host ""
    Write-Host "[运行] 原理深度演示程序..." -ForegroundColor Yellow
    Write-Host ""
    & $JAVA_CMD -cp $CLASSPATH $PRINCIPLE_CLASS
    Write-Host ""
}

Write-Host "═══════════════════════════════════════" -ForegroundColor Cyan
Write-Host "演示完成！" -ForegroundColor Green
Write-Host "═══════════════════════════════════════" -ForegroundColor Cyan
