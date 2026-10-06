# ============================================================
# Smart Expense Tracker - End-to-End Endpoint Smoke Test
# Compatible with Windows PowerShell 5.1 and PowerShell 7+
# Run this AFTER starting all 4 services + Docker infra
# Usage: powershell -ExecutionPolicy Bypass -File test-all-endpoints.ps1
# ============================================================

# ---- CONFIG ----
$GatewayUrl   = "http://localhost:8081"
$TestUsername = "smoketest_user"
$TestEmail    = "smoketest@test.com"
$TestPassword = "test1234"

# Dedicated category for the threshold-trigger test, kept separate from
# real data so repeated runs don't pollute your actual budgets/expenses.
$ThresholdCategory = "SmokeTestAlert"
$ThresholdBudgetLimit = 100

$Global:PassCount = 0
$Global:FailCount = 0

function Write-Result {
    param($Name, $Success, $Detail = "")
    if ($Success) {
        Write-Host "[PASS] $Name" -ForegroundColor Green
        $Global:PassCount++
    } else {
        Write-Host "[FAIL] $Name  -->  $Detail" -ForegroundColor Red
        $Global:FailCount++
    }
}

function Get-StatusCode {
    param($ErrorRecord)
    try {
        return [int]$ErrorRecord.Exception.Response.StatusCode
    } catch {
        return -1
    }
}

function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Method,
        [string]$Url,
        [hashtable]$Headers = @{},
        [string]$Body = $null,
        [int]$ExpectedStatus = 200
    )
    try {
        $params = @{
            Method  = $Method
            Uri     = $Url
            Headers = $Headers
        }
        if ($Body) {
            $params["Body"] = $Body
            $params["ContentType"] = "application/json"
        }
        $response = Invoke-RestMethod @params

        if ($ExpectedStatus -eq 200 -or $ExpectedStatus -eq 201) {
            Write-Result $Name $true
            return $response
        } else {
            Write-Result $Name $false "Expected failure status $ExpectedStatus, but call succeeded"
            return $response
        }
    } catch {
        $status = Get-StatusCode $_
        if ($status -eq $ExpectedStatus) {
            Write-Result $Name $true
        } else {
            $msg = $_.Exception.Message
            Write-Result $Name $false "Status $status - $msg"
        }
        return $null
    }
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host " SMART EXPENSE TRACKER - SMOKE TEST" -ForegroundColor Cyan
Write-Host " Target: $GatewayUrl" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# ---- 1. REGISTER (idempotent-ish: ignore failure if user already exists) ----
Write-Host "--- Auth ---" -ForegroundColor Yellow
$registerBody = @{
    username = $TestUsername
    email    = $TestEmail
    password = $TestPassword
} | ConvertTo-Json

try {
    Invoke-RestMethod -Method POST -Uri "$GatewayUrl/api/auth/register" `
        -Body $registerBody -ContentType "application/json" | Out-Null
    Write-Result "Register new test user" $true
} catch {
    $status = Get-StatusCode $_
    if ($status -eq 409) {
        Write-Host "[INFO] Test user already exists, continuing to login" -ForegroundColor DarkYellow
    } else {
        Write-Host "[INFO] Register call failed (status $status): $($_.Exception.Message)" -ForegroundColor DarkYellow
    }
}

# ---- 2. LOGIN ----
$loginBody = @{ username = $TestUsername; password = $TestPassword } | ConvertTo-Json
$loginResponse = $null
try {
    $loginResponse = Invoke-RestMethod -Method POST -Uri "$GatewayUrl/api/auth/login" `
        -Body $loginBody -ContentType "application/json"
    Write-Result "Login" $true
} catch {
    $status = Get-StatusCode $_
    Write-Result "Login" $false "Status $status - $($_.Exception.Message)"
}

if (-not $loginResponse -or -not $loginResponse.token) {
    Write-Host "`nCannot continue without a valid token. Aborting remaining tests." -ForegroundColor Red
    Write-Host "`n========================================"
    Write-Host " RESULT: $Global:PassCount passed, $Global:FailCount failed"
    Write-Host "========================================`n"
    exit 1
}

$token = $loginResponse.token
$authHeader = @{ Authorization = "Bearer $token" }
Write-Host "[INFO] Token acquired successfully (JWT now carries email claim too)`n" -ForegroundColor DarkYellow

# ---- 3. USER-SERVICE ----
Write-Host "--- User Service ---" -ForegroundColor Yellow
Test-Endpoint -Name "GET /api/users/hello" -Method GET -Url "$GatewayUrl/api/users/hello" -Headers $authHeader | Out-Null
Test-Endpoint -Name "GET /api/users/profile" -Method GET -Url "$GatewayUrl/api/users/profile" -Headers $authHeader | Out-Null

# ---- 4. EXPENSE-SERVICE ----
Write-Host "`n--- Expense Service ---" -ForegroundColor Yellow
Test-Endpoint -Name "GET /api/expenses/hello" -Method GET -Url "$GatewayUrl/api/expenses/hello" -Headers $authHeader | Out-Null

$budgetBody = @{ category = "Food"; limitAmount = 1000 } | ConvertTo-Json
Test-Endpoint -Name "POST /api/budgets (create/update)" -Method POST -Url "$GatewayUrl/api/budgets" -Headers $authHeader -Body $budgetBody | Out-Null
Test-Endpoint -Name "GET /api/budgets" -Method GET -Url "$GatewayUrl/api/budgets" -Headers $authHeader | Out-Null

$expenseBody = @{
    category    = "Food"
    amount      = 50
    description = "Smoke test expense"
    expenseDate = (Get-Date -Format "yyyy-MM-dd")
} | ConvertTo-Json
Test-Endpoint -Name "POST /api/expenses (add)" -Method POST -Url "$GatewayUrl/api/expenses" -Headers $authHeader -Body $expenseBody | Out-Null
Test-Endpoint -Name "GET /api/expenses (list)" -Method GET -Url "$GatewayUrl/api/expenses" -Headers $authHeader | Out-Null

# ---- 5. AI ENDPOINTS ----
Write-Host "`n--- AI Endpoints (Groq) ---" -ForegroundColor Yellow
Test-Endpoint -Name "GET /api/ai/analyze" -Method GET -Url "$GatewayUrl/api/ai/analyze" -Headers $authHeader | Out-Null

$suggestBody = @{ monthlyIncome = 50000 } | ConvertTo-Json
Test-Endpoint -Name "POST /api/ai/suggest-budget" -Method POST -Url "$GatewayUrl/api/ai/suggest-budget" -Headers $authHeader -Body $suggestBody | Out-Null

Test-Endpoint -Name "GET /api/ai/forecast" -Method GET -Url "$GatewayUrl/api/ai/forecast" -Headers $authHeader | Out-Null

$chatBody = @{ message = "How much did I spend on Food?" } | ConvertTo-Json
Test-Endpoint -Name "POST /api/ai/chat" -Method POST -Url "$GatewayUrl/api/ai/chat" -Headers $authHeader -Body $chatBody | Out-Null

# ---- 6. NOTIFICATION-SERVICE (basic reachability) ----
Write-Host "`n--- Notification Service ---" -ForegroundColor Yellow
Test-Endpoint -Name "GET /api/notifications" -Method GET -Url "$GatewayUrl/api/notifications" -Headers $authHeader | Out-Null
Test-Endpoint -Name "GET /api/notifications/unread" -Method GET -Url "$GatewayUrl/api/notifications/unread" -Headers $authHeader | Out-Null

# ---- 7. END-TO-END: BUDGET THRESHOLD -> KAFKA -> NOTIFICATION -> EMAIL ----
# This is the most important test in the whole script: it proves the full
# async pipeline (expense-service publish -> Kafka -> notification-service
# consume -> MongoDB save -> email send) actually works, not just that each
# service responds individually.
Write-Host "`n--- End-to-End Alert Pipeline ---" -ForegroundColor Yellow

$thresholdBudgetBody = @{ category = $ThresholdCategory; limitAmount = $ThresholdBudgetLimit } | ConvertTo-Json
Test-Endpoint -Name "Set dedicated threshold-test budget ($ThresholdCategory, limit $ThresholdBudgetLimit)" `
    -Method POST -Url "$GatewayUrl/api/budgets" -Headers $authHeader -Body $thresholdBudgetBody | Out-Null

# Add an expense at 90% of the limit to guarantee we cross the 80% trigger
$triggerAmount = [math]::Round($ThresholdBudgetLimit * 0.9, 2)
$triggerExpenseBody = @{
    category    = $ThresholdCategory
    amount      = $triggerAmount
    description = "Smoke test - deliberately crossing 80% threshold"
    expenseDate = (Get-Date -Format "yyyy-MM-dd")
} | ConvertTo-Json
Test-Endpoint -Name "Add expense crossing 80% threshold (₹$triggerAmount of ₹$ThresholdBudgetLimit)" `
    -Method POST -Url "$GatewayUrl/api/expenses" -Headers $authHeader -Body $triggerExpenseBody | Out-Null

# Give Kafka + the consumer a moment to process asynchronously before checking Mongo via the API
Write-Host "[INFO] Waiting 3 seconds for the async Kafka -> notification-service pipeline..." -ForegroundColor DarkYellow
Start-Sleep -Seconds 3

$unreadAfterTrigger = Test-Endpoint -Name "GET /api/notifications/unread (after trigger)" `
    -Method GET -Url "$GatewayUrl/api/notifications/unread" -Headers $authHeader

if ($unreadAfterTrigger) {
    $matchingAlert = $unreadAfterTrigger | Where-Object { $_.category -eq $ThresholdCategory }
    if ($matchingAlert) {
        Write-Result "Notification for '$ThresholdCategory' found in unread list" $true
        Write-Host "[INFO] Alert shows $($matchingAlert[0].percentageUsed)% used of ₹$($matchingAlert[0].budgetLimit)" -ForegroundColor DarkYellow
        Write-Host "[INFO] Email delivery cannot be verified by this script - check the inbox manually, and check notification-service's console for 'Budget alert email sent to ...'" -ForegroundColor DarkYellow
    } else {
        Write-Result "Notification for '$ThresholdCategory' found in unread list" $false "No matching notification found - check Kafka consumer logs"
    }
}

# ---- 8. NEGATIVE TEST: no token should be rejected ----
Write-Host "`n--- Security Check ---" -ForegroundColor Yellow
Test-Endpoint -Name "GET /api/expenses without token (should reject)" -Method GET -Url "$GatewayUrl/api/expenses" -ExpectedStatus 403 | Out-Null

# ---- SUMMARY ----
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host " RESULT: $Global:PassCount passed, $Global:FailCount failed" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

if ($Global:FailCount -gt 0) {
    exit 1
} else {
    exit 0
}
