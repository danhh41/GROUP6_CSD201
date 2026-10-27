# ==============================================================================
# run_rq1.ps1 - Chạy thực nghiệm tự động hóa cho RQ1 (Research Question 1)
# Đề tài 1: Sorting & pdqsort - CSD201 FA26
#
# RQ1: Quick Sort, Merge Sort, Heap Sort và Radix Sort khác nhau thế nào về
#      thời gian, bộ nhớ phụ và số thao tác khi N tăng?
#      - Giữ cố định: Dữ liệu D1 (ngẫu nhiên), 3 seed (1, 2, 3)
#      - Yếu tố thay đổi: N = 10^5, 10^6, 5*10^6, 10^7, 2*10^7
#      - Thuật toán: quick, merge, heap, radix (kèm mốc lib-Arrays.sort)
# ==============================================================================

param(
    [string]$Java = "",
    [string]$ClassPath = "target\classes",
    [string]$Xmx = "8g",
    [int]$TimeoutSec = 60, # Mục 6.1: Runner chờ tối đa 60 giây
    [string]$Out = "results\rq1.csv",
    [string]$SummaryOut = "results\summary_rq1.csv",
    [int[]]$Sizes = @(100000, 1000000, 5000000, 10000000, 20000000),
    [int[]]$Seeds = @(1, 2, 3)
)

# 1. Tự động tìm đường dẫn Java JDK 17 nếu người dùng không truyền tham số
if (-not $Java) {
    if (Test-Path "C:\Program Files\Java\jdk-17\bin\java.exe") {
        $Java = "C:\Program Files\Java\jdk-17\bin\java.exe"
    } elseif (Get-Command java -ErrorAction SilentlyContinue) {
        $Java = "java"
    } else {
        $Java = "java.exe"
    }
}

Write-Host "=== BAT DAU CHAY THUC NGHIEM RQ1 ===" -ForegroundColor Cyan
Write-Host "Java Path   : $Java"
Write-Host "ClassPath   : $ClassPath"
Write-Host "Memory Xmx  : $Xmx"
Write-Host "Timeout     : $TimeoutSec s"
Write-Host "Raw CSV     : $Out"
Write-Host "Summary CSV : $SummaryOut"
Write-Host "Sizes N     : $($Sizes -join ', ')"
Write-Host "Seeds       : $($Seeds -join ', ')"
Write-Host "------------------------------------------------------------"

# Các thuật toán tham gia RQ1 (4 thuật toán giáo trình + lib-Arrays.sort làm đối chứng)
$algos = @("quick", "merge", "heap", "radix", "lib-Arrays.sort")
$datasets = @(@{d = "D1"; k = 0})

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
                           "--rq RQ1 --xmx $Xmx --out `"$Out`""

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
                    $row = "$runId,RQ1,$algo,base,$($ds.d),$n,k=0,$seed,,1.0,1,,,,,,,$status,$Xmx,$env:COMPUTERNAME,"
                    Add-Content -Path $Out -Value $row
                }

                $msgStatus = if ($status) { $status } else { 'ok' }
                $color = if ($status) { 'Red' } else { 'Green' }
                Write-Host "[$currentRun/$totalRuns] n=$n seed=$seed $($ds.d) $algo -> $msgStatus" -ForegroundColor $color
            }
        }
    }
}

Write-Host "------------------------------------------------------------"
Write-Host "DA HOAN THANH DU LIEU THO -> Ghi tai: $Out" -ForegroundColor Cyan

# Gọi script tổng hợp
if (Test-Path ".\summarize_rq1.ps1") {
    & .\summarize_rq1.ps1 -In $Out -Out $SummaryOut
}
