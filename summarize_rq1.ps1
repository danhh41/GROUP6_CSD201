# ==============================================================================
# summarize_rq1.ps1 - Tổng hợp trung vị cho RQ1 từ file results/rq1.csv
# ==============================================================================
param(
    [string]$In = "results\rq1.csv",
    [string]$Out = "results\summary_rq1.csv"
)

function Median($v) {
    $s = @($v | Where-Object { $_ -ne $null } | Sort-Object)
    $c = $s.Count
    if ($c -eq 0) { return $null }
    if ($c % 2 -eq 1) { return $s[($c - 1) / 2] }
    return [Math]::Round(($s[$c / 2 - 1] + $s[$c / 2]) / 2.0, 4)
}

if (-not (Test-Path $In)) {
    Write-Host "Loi: File khong ton tai: $In" -ForegroundColor Red
    exit 1
}

$rows = Import-Csv $In

# 1. Kiểm tra chéo output_check
$okRows = @($rows | Where-Object { $_.status -eq "ok" -and $_.output_check -ne "" })
$mismatches = 0
$okRows | Group-Object dataset, n, seed | ForEach-Object {
    $hashes = @($_.Group | Select-Object -ExpandProperty output_check -Unique)
    if ($hashes.Count -gt 1) {
        $mismatches++
        Write-Host "CANH BAO: output_check khong khop tai nhom [$($_.Name)]" -ForegroundColor Yellow
    }
}
if ($mismatches -eq 0) {
    Write-Host "Kiem tra cheo thanh cong: output_check hoan toan khop o moi nhom!" -ForegroundColor Green
}

# 2. Tổng hợp theo (algorithm, dataset, n)
$summary = $rows | Group-Object algorithm, dataset, n | ForEach-Object {
    $g = $_.Group

    $good = @($g | Where-Object { $_.status -eq "ok" })
    $failed = @($g | Where-Object { $_.status -ne "ok" })

    $timingRows = @($good | Where-Object { $_.elapsed_ms -ne $null -and $_.elapsed_ms -ne "" })
    $times = @($timingRows | ForEach-Object { [double]$_.elapsed_ms })
    $peaks = @($timingRows | Where-Object { $_.heap_peak_bytes -ne "" } | ForEach-Object { [double]$_.heap_peak_bytes })
    $auxBytes = @($timingRows | Where-Object { $_.heap_peak_bytes -ne "" -and $_.heap_before_bytes -ne "" } | ForEach-Object {
        [double]$_.heap_peak_bytes - [double]$_.heap_before_bytes
    })

    $countRows = @($good | Where-Object { $_.ops_count -ne $null -and $_.ops_count -ne "" })
    $ops   = @($countRows | ForEach-Object { [double]$_.ops_count })

    $depthRows = @($good | Where-Object { $_.max_depth -ne $null -and $_.max_depth -ne "" -and $_.max_depth -ne "-1" })
    $depths = @($depthRows | ForEach-Object { [int]$_.max_depth })

    $medTime = Median $times
    $nVal = [int]$g[0].n
    # Throughput (triệu phần tử/giây) = (N / 10^6) / (median_ms / 1000) = (N / 1000) / median_ms
    $throughput = if ($medTime -and $medTime -gt 0) { [Math]::Round(($nVal / 1000.0) / $medTime, 2) } else { $null }

    [pscustomobject]@{
        algorithm        = $g[0].algorithm
        dataset          = $g[0].dataset
        n                = $nVal
        runs_ok          = $good.Count
        runs_failed      = $failed.Count
        median_ms        = $medTime
        min_ms           = if ($times.Count) { ($times | Measure-Object -Minimum).Minimum } else { $null }
        max_ms           = if ($times.Count) { ($times | Measure-Object -Maximum).Maximum } else { $null }
        throughput_M_sec = $throughput
        median_heap_peak = Median $peaks
        heap_aux_bytes   = Median $auxBytes
        median_ops       = Median $ops
        max_depth        = if ($depths.Count) { ($depths | Measure-Object -Maximum).Maximum } else { $null }
    }
} | Sort-Object n, algorithm

$summary | Export-Csv -Path $Out -NoTypeInformation
$summary | Format-Table -AutoSize
Write-Host "Da xuat file tong hop ket qua RQ1: $Out" -ForegroundColor Green
