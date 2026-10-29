#!/usr/bin/env bash
# run_rq3.sh - ban tuong duong run_rq3.ps1 cho Linux/macOS (cung tham so, cung CSV).
# Cach chay:  mvn clean compile && ./run_rq3.sh [OUT] [SIZES] [REPEATS]
#   vi du thu nhanh: ./run_rq3.sh results/rq3_thu.csv "1000000" 1
set -u
OUT="${1:-results/rq3.csv}"
SIZES="${2:-1000000 10000000}"
REPEATS="${3:-3}"
XMX="${XMX:-8g}"
TIMEOUT=60
CP="target/classes"
COMMIT="$(git rev-parse --short HEAD 2>/dev/null || echo unknown)"
MACHINE="${MACHINE_ID:-$(hostname)}"
mkdir -p "$(dirname "$OUT")"

run_one() { # algo variant data n seed k repeat extra
  local before after
  before=$( [ -f "$OUT" ] && wc -l < "$OUT" || echo 0 )
  timeout $((TIMEOUT + 60)) java -Xmx"$XMX" -cp "$CP" csd201.sort.BenchmarkRunner \
    --algo "$1" --variant "$2" --data "$3" --n "$4" --seed "$5" --k "$6" \
    --rq RQ3 --repeat "$7" --xmx "$XMX" --machine_id "$MACHINE" --git_commit "$COMMIT" \
    --timeout "$TIMEOUT" --out "$OUT" $8 > /dev/null 2>> "${OUT%.csv}_stderr.log"
  local code=$?
  after=$( [ -f "$OUT" ] && wc -l < "$OUT" || echo 0 )
  local st=""
  if [ $code -eq 124 ]; then st="timeout"; elif [ $code -ne 0 ] && [ "$after" -eq "$before" ]; then st="crash"; fi
  if [ -n "$st" ]; then
    echo "$(head -c4 /dev/urandom | od -An -tx1 | tr -d ' \n'),RQ3,$1,$2,$3,$4,k=$6,$5,,1.0,$7,,,,,,,$st,$XMX,$MACHINE,$COMMIT" >> "$OUT"
  fi
  echo "n=$4 $3 k=$6 seed=$5 $1/$2 repeat=$7 -> ${st:-ok}"
}

CONFIGS="quick:base pdq:base pdq:noA pdq:noB lib-Arrays.sort:base"
DATASETS="D1:0:1,2,3 D2:0:1 D3:0:1,2,3 D4:0:1 D5:1000:1,2,3 D5:10:1,2,3 D5:2:1,2,3"
for n in $SIZES; do
  for ds in $DATASETS; do
    IFS=: read -r d k seeds <<< "$ds"
    for seed in ${seeds//,/ }; do
      for c in $CONFIGS; do
        IFS=: read -r algo variant <<< "$c"
        for r in $(seq 1 "$REPEATS"); do
          extra=""; [ "$r" -gt 1 ] && extra="--timing-only"
          run_one "$algo" "$variant" "$d" "$n" "$seed" "$k" "$r" "$extra"
        done
      done
    done
  done
done
if [ "${SKIP_KILLER:-0}" != "1" ]; then
  for n in 10000 100000; do
    run_one quick base KILLER "$n" 1 0 1 ""
    run_one pdq base KILLER "$n" 1 0 1 ""
  done
fi
echo "XONG RQ3 -> $OUT. Tiep theo: python plot_rq3.py"
