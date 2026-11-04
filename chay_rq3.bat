@echo off
rem Nhay dup de chay RQ3 (khong can Maven). Chay thu: chay_rq3.bat -Thu
cd /d "%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0chay_rq3.ps1" %*
echo.
pause
