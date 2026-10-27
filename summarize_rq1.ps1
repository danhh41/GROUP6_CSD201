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

    $countRows = @($good | Where-Object { $_.ops_count -ne $null -and $_.ops_count -ne "" })
    $ops   = @($countRows | ForEach-Object { [double]$_.ops_count })

    [pscustomobject]@{
        algorithm        = $g[0].algorithm
        dataset          = $g[0].dataset
        n                = [int]$g[0].n
        runs_ok          = $good.Count
        runs_failed      = $failed.Count
        median_ms        = Median $times
        min_ms           = if ($times.Count) { ($times | Measure-Object -Minimum).Minimum } else { $null }
        max_ms           = if ($times.Count) { ($times | Measure-Object -Maximum).Maximum } else { $null }
        median_heap_peak = Median $peaks
        median_ops       = Median $ops
    }
} | Sort-Object n, algorithm

$summary | Export-Csv -Path $Out -NoTypeInformation
$summary | Format-Table -AutoSize
Write-Host "Da xuat file tong hop ket qua RQ1: $Out" -ForegroundColor Green
