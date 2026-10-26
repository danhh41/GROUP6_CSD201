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
    [int]$TimeoutSec = 300,
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
Write-Host "Raw CSV     : $Out"
Write-Host "Summary CSV : $SummaryOut"
Write-Host "Sizes N     : $($Sizes -join ', ')"
Write-Host "Seeds       : $($Seeds -join ', ')"
Write-Host "------------------------------------------------------------"

# Các thuật toán tham gia RQ1 (4 thuật toán giáo trình + lib làm đối chứng)
$algos = @("quick", "merge", "heap", "radix", "lib")
$datasets = @(@{d = "D1"; k = 0})

# Đảm bảo thư mục results tồn tại
$outDir = Split-Path $Out
if ($outDir -and -not (Test-Path $outDir)) {
    New-Item -ItemType Directory -Force -Path $outDir | Out-Null
}

function Count-Rows($f) {
    if (Test-Path $f) { return (Get-Content $f | Measure-Object -Line).Lines } else { return 0 }
}

function Median($v) {
    $s = @($v | Sort-Object)
    $c = $s.Count
    if ($c -eq 0) { return $null }
    if ($c % 2 -eq 1) { return $s[($c - 1) / 2] }
    return ($s[$c / 2 - 1] + $s[$c / 2]) / 2
}

# 2. Vòng lặp chạy thực nghiệm đo đạc (Mỗi lượt 1 JVM riêng biệt)
$totalRuns = $Sizes.Count * $Seeds.Count * $datasets.Count * $algos.Count
$currentRun = 0

foreach ($n in $Sizes) {
    foreach ($seed in $Seeds) {
        foreach ($ds in $datasets) {
            foreach ($algo in $algos) {
                $currentRun++
                $before = Count-Rows $Out
                $argList = "-Xmx$Xmx -cp `"$ClassPath`" csd201.sort.BenchmarkRunner " +
                           "--algo $algo --data $($ds.d) --n $n --seed $seed --k $($ds.k) --out `"$Out`""

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
                    $row = "$algo,$($ds.d),$n,$seed,$($ds.k),,,,,default,,,$status"
                    Add-Content -Path $Out -Value $row
                }

                $msgStatus = if ($status) { $status } else { 'done' }
                $color = if ($status) { 'Red' } else { 'Green' }
                Write-Host "[$currentRun/$totalRuns] n=$n seed=$seed $($ds.d) $algo -> $msgStatus" -ForegroundColor $color
            }
        }
    }
}

Write-Host "------------------------------------------------------------"
Write-Host "DA HOAN THANH DU LIEU THO -> Ghi tai: $Out" -ForegroundColor Cyan
Write-Host ""

# 3. Tự động tổng hợp trung vị và xuất ra file CSV thứ hai (summary_rq1.csv)
Write-Host "=== TONG HOP KET QUA VA KIEM TRA CHEO OUTPUT_CHECK ===" -ForegroundColor Cyan

if (Test-Path $Out) {
    $rows = Import-Csv $Out
    $okRows = @($rows | Where-Object { $_.status -eq "true" })

    # Kiểm tra chéo: cùng (data, n, seed, k) thì mọi thuật toán phải cho cùng output_check
    $bad = 0
    $okRows | Group-Object data, n, seed, k | ForEach-Object {
        $distinct = @($_.Group | Select-Object -ExpandProperty output_check -Unique)
        if ($distinct.Count -gt 1) {
            $bad++
            Write-Host "CANH BAO: output_check khong khop o nhom [$($_.Name)]" -ForegroundColor Yellow
        }
    }
    if ($bad -eq 0) {
        Write-Host "output_check khop hoan toan giua tat ca thuat toan o moi nhom!" -ForegroundColor Green
    }

    # Tính giá trị trung vị theo (algorithm, data, n, k)
    $summary = $rows | Group-Object algorithm, data, n, k | ForEach-Object {
        $g = $_.Group
        $good = @($g | Where-Object { $_.status -eq "true" })
        $times = @($good | ForEach-Object { [double]$_.elapsed_ms })
        $peaks = @($good | ForEach-Object { [double]$_.heap_peak_bytes })
        $ops   = @($good | ForEach-Object { [double]$_.operation_count })
        [pscustomobject]@{
            algorithm        = $g[0].algorithm
            data             = $g[0].data
            n                = [int]$g[0].n
            k                = [int]$g[0].k
            runs_ok          = $good.Count
            runs_failed      = $g.Count - $good.Count
            median_ms        = Median $times
            min_ms           = if ($times.Count) { ($times | Measure-Object -Minimum).Minimum } else { $null }
            max_ms           = if ($times.Count) { ($times | Measure-Object -Maximum).Maximum } else { $null }
            median_heap_peak = Median $peaks
            median_ops       = Median $ops
        }
    } | Sort-Object n, algorithm

    $summary | Export-Csv -Path $SummaryOut -NoTypeInformation
    $summary | Format-Table -AutoSize
    Write-Host "DA XUAT FILE TONG HOP TRUNG VI -> $SummaryOut" -ForegroundColor Green
} else {
    Write-Host "Loi: Khong tim thay file ket qua thô $Out de tong hop!" -ForegroundColor Red
}
