# Chạy hàng loạt RQ2: nhiều N x nhiều seed x D1-D5. Mỗi tổ hợp = 1 JVM riêng.
# Mặc định: lượt đo thời gian. Thêm -Count để chạy lượt --count (ghi file riêng, ví dụ -Out results\count.csv).
param(
    [string]$Java = "",
    [string]$ClassPath = "target\classes",
    [string]$Xmx = "8g",
    [int]$TimeoutSec = 300,
    [string]$Out = "results\rq2.csv",
    [int[]]$Sizes = @(1000000, 10000000),
    [int[]]$Seeds = @(1, 2, 3),
    [switch]$Count
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

$algos = @("quick", "merge", "heap", "radix", "lib")   # thêm "pdq" khi đã đăng ký trong REGISTRY
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
        if ($Count -and $algo -eq "lib") { continue }   # lib (Arrays.sort) không có hàm đếm

        $before = Count-Rows $Out
        $argList = "-Xmx$Xmx -cp `"$ClassPath`" csd201.sort.BenchmarkRunner " +
                   "--algo $algo --data $($ds.d) --n $n --seed $seed --k $($ds.k) " +
                   "--out `"$Out`" --timeout $TimeoutSec" + $(if ($Count) { " --count" } else { "" })

        $p = Start-Process -FilePath $Java -ArgumentList $argList -PassThru -NoNewWindow
        # Runner tự ghi dòng timeout sau $TimeoutSec giây; đây chỉ là kill dự phòng nếu JVM bị treo
        $finished = $p.WaitForExit(($TimeoutSec + 60) * 1000)

        $status = $null
        if (-not $finished) {
            $p.Kill()
            $status = "timeout"
        } elseif ($p.ExitCode -ne 0 -and (Count-Rows $Out) -eq $before) {
            $status = "crash"
        }

        if ($status) {
            # algorithm,data,n,seed,k,elapsed_ms,heap_before,heap_peak,max_depth,variant,output_check,operation_count,status
            $row = "$algo,$($ds.d),$n,$seed,$($ds.k),-1,-1,-1,-1,default,,-1,$status"
            Add-Content -Path $Out -Value $row
        }
        Write-Host "n=$n seed=$seed $($ds.d) k=$($ds.k) $algo -> $(if ($status) { $status } else { 'done' })"
      }
    }
  }
}
