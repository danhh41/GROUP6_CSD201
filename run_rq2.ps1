# ==============================================================================
# run_rq2.ps1 - Chạy thực nghiệm tự động hóa cho RQ2 (Research Question 2)
# Đề tài 1: Sorting & pdqsort - CSD201 FA26
#
# RQ2: Mức độ có thứ tự (D1-D4) và mức độ trùng khóa (D1, D5: K=1000, 10, 2)
#      làm thay đổi thứ hạng của 4 thuật toán giáo trình thế nào?
#      - Thí nghiệm A: D1, D2, D3, D4
#      - Thí nghiệm B: D1, D5 (K = 1000, 10, 2)
#      - Quy mô: N = 10^6 và N = 10^7
#      - Lặp: 3 seed (1, 2, 3)
# ==============================================================================

param(
    [string]$Java = "",
    [string]$ClassPath = "target\classes",
    [string]$Xmx = "8g",
    [int]$TimeoutSec = 60, # Mục 6.1: Runner chờ tối đa 60 giây
    [string]$Out = "results\rq2.csv",
    [string]$SummaryOut = "results\summary_rq2.csv",
    [int[]]$Sizes = @(1000000, 10000000),
    [int[]]$Seeds = @(1, 2, 3)
)

# 1. Tự động nhận diện JDK 17
if (-not $Java) {
    if (Test-Path "C:\Program Files\Java\jdk-17\bin\java.exe") {
        $Java = "C:\Program Files\Java\jdk-17\bin\java.exe"
    } elseif (Get-Command java -ErrorAction SilentlyContinue) {
        $Java = "java"
    } else {
        $Java = "java.exe"
    }
}

Write-Host "=== BAT DAU CHAY THUC NGHIEM RQ2 ===" -ForegroundColor Cyan
Write-Host "Java Path   : $Java"
Write-Host "ClassPath   : $ClassPath"
Write-Host "Memory Xmx  : $Xmx"
Write-Host "Timeout     : $TimeoutSec s"
Write-Host "Raw CSV     : $Out"
Write-Host "Summary CSV : $SummaryOut"
Write-Host "Sizes N     : $($Sizes -join ', ')"
Write-Host "Seeds       : $($Seeds -join ', ')"
Write-Host "------------------------------------------------------------"

# Bốn thuật toán giáo trình + lib-Arrays.sort đối chứng
$algos = @("quick", "merge", "heap", "radix", "lib-Arrays.sort")

# Thí nghiệm A (D1-D4) & Thí nghiệm B (D1, D5 với K=1000, 10, 2)
$datasets = @(
    @{d = "D1"; k = 0},
    @{d = "D2"; k = 0},
    @{d = "D3"; k = 0},
    @{d = "D4"; k = 0},
    @{d = "D5"; k = 1000},
    @{d = "D5"; k = 10},
    @{d = "D5"; k = 2}
)

$outDir = Split-Path $Out
if ($outDir -and -not (Test-Path $outDir)) {
    New-Item -ItemType Directory -Force -Path $outDir | Out-Null
}

function Count-Rows($f) {
    if (Test-Path $f) { return (Get-Content $f | Measure-Object -Line).Lines } else { return 0 }
}

$totalRuns = $Sizes.Count * $Seeds.Count * $datasets.Count * $algos.Count
$currentRun = 0

foreach ($n in $Sizes) {
    foreach ($seed in $Seeds) {
        foreach ($ds in $datasets) {
            foreach ($algo in $algos) {
                $currentRun++
                $before = Count-Rows $Out
                $argList = "-Xmx$Xmx -cp `"$ClassPath`" csd201.sort.BenchmarkRunner " +
                           "--algo $algo --data $($ds.d) --n $n --seed $seed --k $($ds.k) " +
                           "--rq RQ2 --xmx $Xmx --out `"$Out`""

                $p = Start-Process -FilePath $Java -ArgumentList $argList -PassThru -NoNewWindow
                $finished = $p.WaitForExit($TimeoutSec * 1000)

                $status = $null
                if (-not $finished) {
                    $p.Kill()
                    $status = "timeout"
                } elseif ($p.ExitCode -ne 0 -and (Count-Rows $Out) -eq $before) {
                    $status = "crash"
                }

                if ($status) {
                    $runId = [System.Guid]::NewGuid().ToString().Substring(0, 8)
                    $params = if ($ds.k -gt 0) { "k=$($ds.k)" } else { "k=0" }
                    # Ghi đúng 21 cột Bảng 4 khi bị timeout hoặc crash
                    $row = "$runId,RQ2,$algo,base,$($ds.d),$n,$params,$seed,,1.0,1,,,,,,,$status,$Xmx,$env:COMPUTERNAME,"
                    Add-Content -Path $Out -Value $row
                }

                $msgStatus = if ($status) { $status } else { 'ok' }
                $color = if ($status) { 'Red' } else { 'Green' }
                Write-Host "[$currentRun/$totalRuns] n=$n seed=$seed $($ds.d) k=$($ds.k) $algo -> $msgStatus" -ForegroundColor $color
            }
        }
    }
}

Write-Host "------------------------------------------------------------"
Write-Host "DA HOAN THANH CHAY RQ2 -> Ghi tai: $Out" -ForegroundColor Cyan

# Gọi script tổng hợp
if (Test-Path ".\summarize_rq2.ps1") {
    & .\summarize_rq2.ps1 -In $Out -Out $SummaryOut
}