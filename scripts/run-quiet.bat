@echo off
rem ====== Wrapper untuk run.bat tanpa pause ======
call "%~dp0..\run.bat" <nul > "%~dp0..\run.log" 2>&1
echo EXITCODE=%ERRORLEVEL% >> "%~dp0..\run.log"
