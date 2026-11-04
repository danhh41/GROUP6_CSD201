# ==============================================================================
# chay_rq3.ps1 - Chay tron goi RQ3 (Cap 3, Nguoi 5), KHONG can Maven.
#   1. Kiem tra dang o dung thu muc repo va dung nhanh
#   2. Tim JDK (java + javac), bien dich src\main\java vao target\classes
#   3. Chay thu 1 lan de chac chan runner chay duoc
#   4. run_rq3.ps1  -> results\rq3.csv
#   5. PdqEvidence ablation, jit -> results\pdq_ablation.csv, results\pdq_jit.csv
#   6. Kiem tra Python + matplotlib + pandas (tu cai neu thieu), chay plot_rq3.py
#
# Cach chay: nhay dup chay_rq3.bat, hoac trong PowerShell:
#   .\chay_rq3.ps1          # chay that, khoang 30-40 phut (chi chay tren may do chinh)
#   .\chay_rq3.ps1 -Thu     # chay thu khoang 1-2 phut, ket qua vao results\thu\ (khong dung cho paper)
#   .\chay_rq3.ps1 -ChiVe   # da co CSV, chi ve hinh va tao bang cho paper
# ==============================================================================

param(
    [switch]$Thu,
    [switch]$ChiVe,
    [string]$Xmx = "8g"
)

$ErrorActionPreference = "Stop"
$root = $PSScriptRoot
if (-not $root) { $root = (Get-Location).Path }
Set-Location $root
$onWindows = ($env:OS -eq "Windows_NT")

function Buoc($msg) { Write-Host ""; Write-Host "==> $msg" -ForegroundColor Cyan }
function Loi($msg) {
    Write-Host ""
    Write-Host "LOI: $msg" -ForegroundColor Red
    exit 1
}
function P([string[]]$parts) {
    $p = $parts[0]
    for ($i = 1; $i -lt $parts.Length; $i++) { $p = Join-Path $p $parts[$i] }
    return $p
}

# ------------------------------------------------------------------------------
Buoc "1/6 Kiem tra thu muc repo"
$srcDir = P @("src", "main", "java", "csd201", "sort")
if (-not (Test-Path (Join-Path $srcDir "PdqSort.java"))) {
    Loi "Khong thay $srcDir\PdqSort.java. Hay dat file nay o thu muc goc repo GROUP6_CSD201."
}
foreach ($f in @("run_rq3.ps1", "plot_rq3.py", (Join-Path $srcDir "PdqEvidence.java"))) {
    if (-not (Test-Path $f)) {
        Loi ("Thieu $f. Ban dang o nhanh khong co phan RQ3. Mo Git Bash o thu muc repo va chay:`n" +
             "     git fetch`n     git checkout pair3/nguoi5-rq3")
    }
}
try {
    $branch = (& git rev-parse --abbrev-ref HEAD 2>$null)
    if ($branch) { Write-Host "Nhanh hien tai: $branch" }
} catch { }
Write-Host "OK: $root"

# ------------------------------------------------------------------------------
Buoc "2/6 Tim JDK va bien dich (khong can Maven)"
$exe = ""
if ($onWindows) { $exe = ".exe" }

function Test-Jdk($binDir) {
    if (-not $binDir) { return $false }
    return (Test-Path (Join-Path $binDir "javac$exe")) -and (Test-Path (Join-Path $binDir "java$exe"))
}

$candidates = New-Object System.Collections.ArrayList
if ($env:JAVA_HOME) { [void]$candidates.Add((Join-Path $env:JAVA_HOME "bin")) }
$cmd = Get-Command "javac$exe" -ErrorAction SilentlyContinue
if ($cmd) { [void]$candidates.Add((Split-Path $cmd.Source)) }
if ($onWindows) {
    $bases = @("C:\Program Files\Java", "C:\Program Files\Eclipse Adoptium", "C:\Program Files\Microsoft",
               "C:\Program Files\Zulu", "C:\Program Files\Amazon Corretto", "C:\Program Files\BellSoft",
               "C:\Program Files (x86)\Java", (Join-Path $env:USERPROFILE ".jdks"))
    foreach ($b in $bases) {
        if (Test-Path $b) {
            Get-ChildItem $b -Directory -ErrorAction SilentlyContinue | Sort-Object Name -Descending | ForEach-Object {
                [void]$candidates.Add((Join-Path $_.FullName "bin"))
            }
        }
    }
}
$jdkBin = $null
foreach ($c in $candidates) { if (Test-Jdk $c) { $jdkBin = $c; break } }
if (-not $jdkBin) {
    Loi ("Khong tim thay JDK (can ca java va javac). Cai JDK 17 (vd. Eclipse Temurin 17) roi chay lai,`n" +
         "     hoac dat bien JAVA_HOME tro toi thu muc JDK.")
}
$java = Join-Path $jdkBin "java$exe"
$javac = Join-Path $jdkBin "javac$exe"
Write-Host "JDK: $jdkBin"

$classes = P @("target", "classes")
New-Item -ItemType Directory -Force -Path $classes | Out-Null
$sources = @(Get-ChildItem $srcDir -Filter *.java | ForEach-Object { $_.FullName })
$ErrorActionPreference = "Continue"
& $javac -nowarn -encoding UTF-8 -d $classes $sources
$ErrorActionPreference = "Stop"
if ($LASTEXITCODE -ne 0) { Loi "Bien dich that bai (xem thong bao javac o tren)." }
Write-Host "OK: da bien dich $($sources.Count) file vao $classes"

# ------------------------------------------------------------------------------
if ($Thu) {
    $outDir = P @("results", "thu")
} else {
    $outDir = "results"
}
New-Item -ItemType Directory -Force -Path $outDir | Out-Null
$rq3Csv = Join-Path $outDir "rq3.csv"
$ablCsv = Join-Path $outDir "pdq_ablation.csv"
$jitCsv = Join-Path $outDir "pdq_jit.csv"

if (-not $ChiVe) {
    Buoc "3/6 Chay thu runner 1 lan"
    $probe = Join-Path ([System.IO.Path]::GetTempPath()) ("rq3_probe_" + [guid]::NewGuid().ToString("N") + ".csv")
    $oldEap = $ErrorActionPreference; $ErrorActionPreference = "Continue"
    $probeOut = & $java "-Xmx1g" -cp $classes csd201.sort.BenchmarkRunner --algo pdq --variant noA --data D2 `
                 --n 100000 --seed 1 --rq RQ3 --out $probe 2>&1
    $ErrorActionPreference = $oldEap
    $okProbe = (Test-Path $probe) -and ((Get-Content $probe -Raw) -match ",ok,")
    if (-not $okProbe) {
        $probeOut | ForEach-Object { Write-Host $_ }
        Loi "Runner chay khong duoc (thong bao o tren). Chup man hinh gui lai."
    }
    Remove-Item $probe -ErrorAction SilentlyContinue
    Write-Host "OK: runner chay duoc"

    Buoc "4/6 Do RQ3 -> $rq3Csv"
    if (Test-Path $rq3Csv) {
        $bak = "$rq3Csv.bak_" + (Get-Date -Format "yyyyMMdd_HHmmss")
        Move-Item $rq3Csv $bak
        Write-Host "File cu duoc doi ten thanh $bak (tranh ghi chong 2 lan do)."
    }
    $runArgs = @{ Java = $java; ClassPath = $classes; Out = $rq3Csv; Xmx = $Xmx }
    if ($Thu) {
        $runArgs.Sizes = @(100000); $runArgs.Repeats = 1; $runArgs.SkipKiller = $true
        Write-Host "Che do THU: N = 10^5, 1 lan, bo KILLER. Ket qua KHONG dung cho paper." -ForegroundColor Yellow
    } else {
        Write-Host "Mat khoang 30-40 phut. Dong trinh duyet, game... cho den khi xong." -ForegroundColor Yellow
    }
    $ErrorActionPreference = "Continue"
    & (Join-Path $root "run_rq3.ps1") @runArgs
    $ErrorActionPreference = "Stop"
    if (-not (Test-Path $rq3Csv)) { Loi "run_rq3.ps1 khong tao duoc $rq3Csv." }

    Buoc "5/6 Thi nghiem tat co che va JIT"
    foreach ($pair in @(@("ablation", $ablCsv), @("jit", $jitCsv))) {
        if (Test-Path $pair[1]) { Remove-Item $pair[1] }
        $ErrorActionPreference = "Continue"
        & $java "-Xmx$Xmx" -cp $classes csd201.sort.PdqEvidence $pair[0] $pair[1]
        $ErrorActionPreference = "Stop"
        if ($LASTEXITCODE -ne 0) { Loi "PdqEvidence $($pair[0]) that bai." }
        Write-Host "OK: $($pair[1])"
    }
}

# ------------------------------------------------------------------------------
Buoc "6/6 Ve hinh va tao bang cho paper"
foreach ($f in @($rq3Csv, $ablCsv, $jitCsv)) {
    if (-not (Test-Path $f)) { Loi "Chua co $f. Chay lai khong kem -ChiVe." }
}
$py = $null
$pyArgs = @()
foreach ($try in @(@("py", "-3"), @("python"), @("python3"))) {
    $c = Get-Command $try[0] -ErrorAction SilentlyContinue
    if (-not $c) { continue }
    $extra = @(); if ($try.Length -gt 1) { $extra = @($try[1]) }
    $oldEap = $ErrorActionPreference; $ErrorActionPreference = "Continue"
    $v = & $c.Source @extra --version 2>&1
    $ErrorActionPreference = $oldEap
    if ($LASTEXITCODE -eq 0 -and "$v" -match "Python 3") { $py = $c.Source; $pyArgs = $extra; break }
}
if (-not $py -and $onWindows) {
    # Python da cai nhung khong nam trong PATH: tim o cac thu muc cai dat thuong gap
    $pyDirs = @()
    if ($env:LOCALAPPDATA) { $pyDirs += (Join-Path $env:LOCALAPPDATA "Programs\Python") }
    $pyDirs += @("C:\Program Files", "C:\Program Files (x86)", "C:\")
    $found = @()
    foreach ($d in $pyDirs) {
        if (Test-Path $d) {
            $found += @(Get-ChildItem $d -Directory -Filter "Python3*" -ErrorAction SilentlyContinue |
                        ForEach-Object { Join-Path $_.FullName "python.exe" })
        }
    }
    if ($env:USERPROFILE) {
        foreach ($n in @("anaconda3", "miniconda3", "Anaconda3", "Miniconda3")) {
            $found += (Join-Path $env:USERPROFILE "$n\python.exe")
        }
    }
    if ($env:ProgramData) { $found += (Join-Path $env:ProgramData "anaconda3\python.exe") }
    foreach ($f in ($found | Sort-Object -Descending)) {
        if (Test-Path $f) { $py = $f; $pyArgs = @(); break }
    }
}
if (-not $py) {
    $again = ".\chay_rq3.bat -ChiVe"
    if ($Thu) { $again = ".\chay_rq3.bat -Thu -ChiVe" }
    Loi ("Khong tim thay Python 3. Cai Python 3 tu https://www.python.org/downloads/`n" +
         "     (o man hinh cai dat nho tick 'Add python.exe to PATH'), dong cua so nay roi chay:`n" +
         "     $again`n" +
         "     Cac buoc do da xong, lenh tren chi ve hinh, khong do lai.")
}
Write-Host "Python: $py $pyArgs"
$oldEap = $ErrorActionPreference; $ErrorActionPreference = "Continue"
& $py @pyArgs -c "import matplotlib, pandas" 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Host "Dang cai matplotlib va pandas..."
    & $py @pyArgs -m pip install --user matplotlib pandas
}
$ErrorActionPreference = $oldEap

$plotArgs = @("plot_rq3.py", "--rq3", $rq3Csv, "--ablation", $ablCsv, "--jit", $jitCsv)
if ($Thu) {
    $plotArgs += @("--fig-dir", (Join-Path $outDir "figures"), "--paper-dir", (Join-Path $outDir "paper"))
}
$ErrorActionPreference = "Continue"
& $py @pyArgs @plotArgs
$ErrorActionPreference = "Stop"
if ($LASTEXITCODE -ne 0) { Loi "plot_rq3.py that bai (xem thong bao o tren)." }

Write-Host ""
Write-Host "=============================== XONG ===============================" -ForegroundColor Green
if ($Thu) {
    Write-Host "Ban THU nam trong $outDir. Khong commit thu muc nay."
    Write-Host "Canh bao 'KIEM TRA LOI VAN' o che do thu la binh thuong (N nho, khong chay KILLER)."
    Write-Host "Tren may do chinh, chay lai khong co -Thu: .\chay_rq3.ps1"
} else {
    Write-Host "CSV : $rq3Csv, $ablCsv, $jitCsv"
    Write-Host "Hinh: results\figures\fig4_rq3_runtime.png, fig5_pdq_core_ablation.png"
    Write-Host "Paper: paper\rq3_numbers.tex, paper\tables\rq3_*.tex"
    Write-Host "Doc muc 'KIEM TRA LOI VAN' o tren. Co canh bao thi gui lai truoc khi commit."
}
