# ==============================================================================
# summarize_rq2.ps1 - Tổng hợp kết quả RQ2 theo Bảng 4 Quy định chung CSD201 FA26
# ==============================================================================
param(
    [string]$In = "results\rq2.csv",
    [string]$Out = "results\summary_rq2.csv"
)

function Median($v) {
    $s = @($v | Where-Object { $_ -ne $null } | Sort-Object)
    $c = $s.Count
    if ($c -eq 0) { return $null }
    if ($c % 2 -eq 1) { return $s[($c - 1) / 2] }
    return [Math]::Round(($s[$c / 2 - 1] + $s[$c / 2]) / 2.0, 4)
}

if (-not (Test-Path $In)) {
    Write-Host "Loi: Khong tim thay file $In de tong hop!" -ForegroundColor Red
    exit 1
}

$rows = Import-Csv $In

# 1. Kiểm tra chéo output_check: trong cùng một nhóm (dataset, n, seed, params),
#    mọi thuật toán có kết quả 'ok' bắt buộc phải cho cùng một giá trị output_check.
$okRows = @($rows | Where-Object { $_.status -eq "ok" -and $_.output_check -ne "" })
$mismatches = 0
$okRows | Group-Object dataset, n, seed, params | ForEach-Object {
    $hashes = @($_.Group | Select-Object -ExpandProperty output_check -Unique)
    if ($hashes.Count -gt 1) {
        $mismatches++
        Write-Host "CANH BAO: output_check khong khop tai nhom [$($_.Name)]" -ForegroundColor Yellow
    }
}

if ($mismatches -eq 0) {
    Write-Host "Kiem tra cheo thanh cong: output_check hoan toan khop giua tat ca cac thuat toan!" -ForegroundColor Green
}

# 2. Tổng hợp theo (algorithm, dataset, n, params)
$summary = $rows | Group-Object algorithm, dataset, n, params | ForEach-Object {
    $g = $_.Group

    # Lọc các lượt chạy thành công (status == 'ok')
    $good = @($g | Where-Object { $_.status -eq "ok" })
    $failed = @($g | Where-Object { $_.status -ne "ok" })

    # Lọc dữ liệu đo thời gian (dòng có elapsed_ms)
    $timingRows = @($good | Where-Object { $_.elapsed_ms -ne $null -and $_.elapsed_ms -ne "" })
    $times = @($timingRows | ForEach-Object { [double]$_.elapsed_ms })
    $peaks = @($timingRows | Where-Object { $_.heap_peak_bytes -ne "" } | ForEach-Object { [double]$_.heap_peak_bytes })

    # Lọc dữ liệu đếm thao tác (dòng có ops_count)
    $countRows = @($good | Where-Object { $_.ops_count -ne $null -and $_.ops_count -ne "" })
    $ops = @($countRows | ForEach-Object { [double]$_.ops_count })

    [pscustomobject]@{
        algorithm        = $g[0].algorithm
        dataset          = $g[0].dataset
        n                = [int]$g[0].n
        params           = $g[0].params
        runs_ok          = $good.Count
        runs_failed      = $failed.Count
        median_ms        = Median $times
        min_ms           = if ($times.Count) { ($times | Measure-Object -Minimum).Minimum } else { $null }
        max_ms           = if ($times.Count) { ($times | Measure-Object -Maximum).Maximum } else { $null }
        median_heap_peak = Median $peaks
        median_ops       = Median $ops
    }
} | Sort-Object n, dataset, params, algorithm

$summary | Export-Csv -Path $Out -NoTypeInformation
$summary | Format-Table -AutoSize
Write-Host "Da xuat file tong hop ket qua RQ2: $Out" -ForegroundColor Green