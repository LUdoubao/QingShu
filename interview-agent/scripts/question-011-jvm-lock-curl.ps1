$BaseUrl = if ($env:BASE_URL) { $env:BASE_URL } else { "http://localhost:9510" }
$Api = "$BaseUrl/interview-agent/questions/jvm/q011-lock-mode/run"

Write-Host "[CASE 1] pessimistic lock"
Invoke-RestMethod -Method POST -Uri $Api -ContentType "application/json" -Body '{"lockType":"pessimistic","threads":8,"incrementsPerThread":1000}' | ConvertTo-Json -Depth 5

Write-Host "[CASE 2] optimistic lock (CAS)"
Invoke-RestMethod -Method POST -Uri $Api -ContentType "application/json" -Body '{"lockType":"optimistic","threads":8,"incrementsPerThread":1000}' | ConvertTo-Json -Depth 5
