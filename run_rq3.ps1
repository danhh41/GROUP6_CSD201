# ==============================================================================
# run_rq3.ps1 - Chạy thực nghiệm RQ3 (Cặp 3)
# Đề tài 1: Sorting & pdqsort - CSD201 FA26
#
# RQ3: pdqsort cải thiện bao nhiêu so với Quick Sort giáo trình trên dữ liệu RQ2,
#      và mỗi cơ chế (A, B) đóng góp bao nhiêu?
#   - Cấu hình : quick | pdq base (A+B) | pdq noA | pdq noB | lib-Arrays.sort
#   - Dữ liệu  : D1, D2, D3, D4, D5 (K = 1000, 10, 2)  (như RQ2)
#   - Quy mô   : N = 10^6 và N = 10^7
#   - Seed     : 1, 2, 3 cho D1, D3, D5; seed 1 cho D2, D4 (D2, D4 suy ra từ D1)
#   - Lặp      : 3 lượt đo thời gian mỗi cấu hình (Mục 6.6); lượt --count chạy ở lượt 1
#   - Kiểm tra giới hạn (khuyến khích): KILLER (McIlroy), N = 10^4 và 10^5, quick và pdq,
#     repeat = 1. N = 10^6 không chạy vì bộ sinh KILLER tốn O(N^2).
# Mỗi lượt là một tiến trình JVM riêng. Kết quả: results\rq3.csv (21 cột Bảng 4).
#
# Cách chạy (PowerShell, ở thư mục gốc repo, sau khi build):
#   mvn clean compile
#   .\run_rq3.ps1
# Chạy nhanh để thử (dưới 1 phút):
#   .\run_rq3.ps1 -Sizes 1000000 -Repeats 1 -Out results\rq3_thu.csv -SkipKiller
# ==============================================================================

param(
    [string]$Java = "",
    [string]$ClassPath = "target\classes",
    [string]$Xmx = "8g",
    [int]$TimeoutSec = 60,                 # Mục 6.1
    [string]$Out = "results\rq3.csv",
    [int[]]$Sizes = @(1000000, 10000000),
    [int]$Repeats = 3,                     # Mục 6.6
    [switch]$SkipKiller
)

if (-not $Java) {
    if (Test-Path "C:\Program Files\Java\jdk-17\bin\java.exe") {
        $Java = "C:\Program Files\Java\jdk-17\bin\java.exe"
    } elseif (Get-Command java -ErrorAction SilentlyContinue) {
        $Java = "java"
    } else {
        $Java = "java.exe"
    }
}

$gitCommit = (git rev-parse --short HEAD 2>$null)
if (-not $gitCommit) { $gitCommit = "unknown" }
$machineId = $env:COMPUTERNAME

# Thuật toán + biến thể
$configs = @(
    @{algo = "quick";           variant = "base"},
    @{algo = "pdq";             variant = "base"},
    @{algo = "pdq";             variant = "noA"},
    @{algo = "pdq";             variant = "noB"},
    @{algo = "lib-Arrays.sort"; variant = "base"}
)

# Dữ liệu như RQ2; D2, D4 chỉ cần seed 1
$datasets = @(
    @{d = "D1"; k = 0;    seeds = @(1, 2, 3)},
    @{d = "D2"; k = 0;    seeds = @(1)},
    @{d = "D3"; k = 0;    seeds = @(1, 2, 3)},
    @{d = "D4"; k = 0;    seeds = @(1)},
    @{d = "D5"; k = 1000; seeds = @(1, 2, 3)},
    @{d = "D5"; k = 10;   seeds = @(1, 2, 3)},
    @{d = "D5"; k = 2;    seeds = @(1, 2, 3)}
)

$outDir = Split-Path $Out
if ($outDir -and -not (Test-Path $outDir)) {
    New-Item -ItemType Directory -Force -Path $outDir | Out-Null
}

function Count-Rows($f) {
    if (Test-Path $f) { return (Get-Content $f | Measure-Object -Line).Lines } else { return 0 }
}

function Run-One($algo, $variant, $data, $n, $seed, $k, $repeat, $extra) {
    $before = Count-Rows $Out
    $argList = "-Xmx$Xmx -cp `"$ClassPath`" csd201.sort.BenchmarkRunner " +
               "--algo $algo --variant $variant --data $data --n $n --seed $seed --k $k " +
               "--rq RQ3 --repeat $repeat --xmx $Xmx --machine_id $machineId " +
               "--git_commit $gitCommit --timeout $TimeoutSec --out `"$Out`" $extra"

    $p = Start-Process -FilePath $Java -ArgumentList $argList -PassThru -NoNewWindow
    # Runner tự ghi dòng timeout sau $TimeoutSec giây; đây chỉ là kill dự phòng khi JVM treo
    $finished = $p.WaitForExit(($TimeoutSec + 60) * 1000)

    $status = $null
    if (-not $finished) {
        $p.Kill()
        $status = "timeout"
    } elseif ($p.ExitCode -ne 0 -and (Count-Rows $Out) -eq $before) {
        $status = "crash"
    }
    if ($status) {
        $runId = [System.Guid]::NewGuid().ToString().Substring(0, 8)
        $params = "k=$k"
        # 21 cột Bảng 4: run_id,rq,algorithm,variant,dataset,n,params,seed,input_sha256,
        # generator_version,repeat,elapsed_ms,heap_before_bytes,heap_peak_bytes,ops_count,
        # max_depth,output_check,status,xmx,machine_id,git_commit
        $row = "$runId,RQ3,$algo,$variant,$data,$n,$params,$seed,,1.0,$repeat,,,,,,,$status,$Xmx,$machineId,$gitCommit"
        Add-Content -Path $Out -Value $row
    }
    $msg = if ($status) { $status } else { "ok" }
    $color = if ($status) { "Red" } else { "Green" }
    Write-Host "n=$n $data k=$k seed=$seed $algo/$variant repeat=$repeat -> $msg" -ForegroundColor $color
}

Write-Host "=== RQ3: $Out | Xmx=$Xmx | commit=$gitCommit | may=$machineId ===" -ForegroundColor Cyan

foreach ($n in $Sizes) {
    foreach ($ds in $datasets) {
        foreach ($seed in $ds.seeds) {
            foreach ($c in $configs) {
                for ($r = 1; $r -le $Repeats; $r++) {
                    # Lượt 1: đo thời gian + lượt --count riêng; lượt 2, 3: chỉ đo thời gian
                    $extra = if ($r -eq 1) { "" } else { "--timing-only" }
                    Run-One $c.algo $c.variant $ds.d $n $seed $ds.k $r $extra
                }
            }
        }
    }
}

if (-not $SkipKiller) {
    # Kiểm tra giới hạn: repeat = 1 (Mục 6.6)
    foreach ($n in @(10000, 100000)) {
        Run-One "quick" "base" "KILLER" $n 1 0 1 ""
        Run-One "pdq"   "base" "KILLER" $n 1 0 1 ""
    }
}

Write-Host "=== XONG RQ3 -> $Out ===" -ForegroundColor Cyan
Write-Host "Tiep theo: python plot_rq3.py   (tao hinh va bang cho paper)"
