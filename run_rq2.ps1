# Chạy hàng loạt RQ2: nhiều N x nhiều seed x D1-D5. Mỗi tổ hợp = 1 JVM riêng.
param(
    [string]$Java = "",
    [string]$ClassPath = "target\classes",
    [string]$Xmx = "8g",
    [int]$TimeoutSec = 300,
    [string]$Out = "results\rq2.csv",
    [int[]]$Sizes = @(1000000, 10000000),
    [int[]]$Seeds = @(1, 2, 3)
)

if (-not $Java) {
    if (Test-Path "C:\Program Files\Java\jdk-17\bin\java.exe") {
        $Java = "C:\Program Files\Java\jdk-17\bin\java.exe"
    } elseif (Get-Command java -ErrorAction SilentlyContinue) {
        $Java = "java"
    } else {
        $Java = "D:\Java\bin\java.exe"
    }
}

$algos = @("quick", "merge", "heap", "radix", "lib")
$datasets = @(
    @{d = "D1"; k = 0}, @{d = "D2"; k = 0}, @{d = "D3"; k = 0}, @{d = "D4"; k = 0},
    @{d = "D5"; k = 1000}, @{d = "D5"; k = 10}, @{d = "D5"; k = 2}
)

New-Item -ItemType Directory -Force -Path (Split-Path $Out) | Out-Null

function Count-Rows($f) {
    if (Test-Path $f) { return (Get-Content $f | Measure-Object -Line).Lines } else { return 0 }
}

foreach ($n in $Sizes) {
  foreach ($seed in $Seeds) {
    foreach ($ds in $datasets) {
      foreach ($algo in $algos) {
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
        Write-Host "n=$n seed=$seed $($ds.d) k=$($ds.k) $algo -> $(if ($status) { $status } else { 'done' })"
      }
    }
  }
}