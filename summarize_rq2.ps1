# Tổng hợp trung vị theo (algorithm, data, n, k) và kiểm tra output_check khớp giữa các thuật toán.
#   powershell -ExecutionPolicy Bypass -File .\summarize_rq2.ps1
param(
    [string]$In = "results\rq2.csv",
    [string]$Out = "results\summary.csv"
)

function Median($v) {
    $s = @($v | Sort-Object)
    $c = $s.Count
    if ($c -eq 0) { return $null }
    if ($c % 2 -eq 1) { return $s[($c - 1) / 2] }
    return ($s[$c / 2 - 1] + $s[$c / 2]) / 2
}

$rows = Import-Csv $In
$okRows = @($rows | Where-Object { $_.status -eq "true" })

# 1) Kiểm tra chéo: cùng (data,n,seed,k) thì mọi thuật toán phải cho cùng output_check
$bad = 0
$okRows | Group-Object data, n, seed, k | ForEach-Object {
    $distinct = @($_.Group | Select-Object -ExpandProperty output_check -Unique)
    if ($distinct.Count -gt 1) {
        $bad++
        Write-Host "CANH BAO: output_check khong khop o nhom [$($_.Name)]" -ForegroundColor Yellow
    }
}
if ($bad -eq 0) { Write-Host "output_check khop o moi nhom." -ForegroundColor Green }

# 2) Trung vị theo (algorithm, data, n, k)
$summary = $rows | Group-Object algorithm, data, n, k | ForEach-Object {
    $g = $_.Group
    $good = @($g | Where-Object { $_.status -eq "true" })
    $times = @($good | ForEach-Object { [double]$_.elapsed_ms })
    $peaks = @($good | ForEach-Object { [double]$_.heap_peak_bytes })
    $ops = @($good | ForEach-Object { [double]$_.operation_count })
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
} | Sort-Object n, data, k, algorithm

$summary | Export-Csv -Path $Out -NoTypeInformation
$summary | Format-Table -AutoSize
Write-Host "Da ghi: $Out"