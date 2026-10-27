@echo off
set JAVA=D:\Java\bin\java
set CP=target\classes
set OUT=results\rq2_v2.csv
for %%A in (quick merge heap radix lib) do (
  for %%N in (1000000 10000000) do (
    for %%S in (1 2 3) do (
      for %%D in (D1 D2 D3 D4) do %JAVA% -cp %CP% csd201.sort.BenchmarkRunner --algo %%A --data %%D --n %%N --seed %%S --k 0 --out %OUT%
      for %%K in (1000 10 2) do %JAVA% -cp %CP% csd201.sort.BenchmarkRunner --algo %%A --data D5 --n %%N --seed %%S --k %%K --out %OUT%
    )
  )
)
echo 