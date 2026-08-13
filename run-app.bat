@echo off
REM ============================================================
REM run-app.bat — Start Pixel_3a emulator + build + install app
REM ============================================================
REM Usage: double-click, or run from a terminal:
REM   cd C:\laragon\www\MindRest_AI
REM   run-app.bat
REM
REM What it does:
REM   1. Builds :app:assembleDebug from your CURRENT branch/HEAD
REM      (Tip: run `git checkout main` first if you want latest main)
REM   2. Launches the Pixel_3a emulator (lightweight, ~1.5GB)
REM      if not already running; reuses it if it is
REM   3. Waits for full boot (max ~120 detik)
REM   4. Installs the fresh APK
REM   5. Launches MainActivity
REM
REM Emulator settings (Pixel_3a AVD):
REM   - hw.keyboard = yes   (host laptop keyboard works in text fields)
REM   - hw.gpu    = host    (hardware GPU rendering)
REM   - hw.ramSize = 1536M  (light)
REM   - hw.cpu.ncore = 2
REM   - audioInput = no
REM ============================================================

chcp 65001 >nul
setlocal enabledelayedexpansion

rem ====== KONFIGURASI ======
set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
set "ANDROID_SDK=%LOCALAPPDATA%\Android\Sdk"
set "ADB=%ANDROID_SDK%\platform-tools\adb.exe"
set "EMU=%ANDROID_SDK%\emulator\emulator.exe"
set "GRADLE=%~dp0gradlew.bat"
set "AVD=Pixel_3a"
set "APK=%~dp0app\build\outputs\apk\debug\app-debug.apk"
set "PKG=com.aistudio.mindrest.eedcdb"
set "PATH=%JAVA_HOME%\bin;%PATH%"

cd /d "%~dp0"

echo ============================================
echo   MindRest AI  -  Build + Run (current HEAD)
echo ============================================
echo.

rem -----------------------------------------------------------
rem 1) Build APK incremental
rem -----------------------------------------------------------
echo [*] Build APK dari HEAD saat ini...
"%GRADLE%" :app:assembleDebug --console=plain
if errorlevel 1 (
  echo.
  echo [X] BUILD GAGAL.
  pause
  exit /b 1
)

if not exist "%APK%" (
  echo [X] APK tidak ditemukan: %APK%
  pause
  exit /b 1
)
echo [v] Build sukses, APK ready.
echo.

rem -----------------------------------------------------------
rem 2) Pastikan emulator jalan (subroutine :WaitForBoot)
rem -----------------------------------------------------------
"%ADB%" devices | findstr "emulator-" >nul
if errorlevel 1 (
  echo [*] Emulator belum jalan. Memulai %AVD% ...
  start "" "%EMU%" -avd %AVD% -gpu host -no-boot-anim -no-snapshot
  call :WaitForBoot
) else (
  echo [v] Emulator sudah berjalan.
)
echo.

rem -----------------------------------------------------------
rem 3) Install APK
rem -----------------------------------------------------------
echo [*] Install APK baru...
"%ADB%" install -r "%APK%"
if errorlevel 1 (
  echo [X] INSTALL GAGAL.
  pause
  exit /b 1
)
echo [v] Install sukses.
echo.

rem -----------------------------------------------------------
rem 4) Launch aplikasi
rem -----------------------------------------------------------
echo [*] Membuka aplikasi...
"%ADB%" shell am start -n "%PKG%/com.example.MainActivity"
echo.

echo ============================================
echo  DONE. Emulator window has the app open.
echo  Test keyboard: click a text field, then type on laptop.
echo ============================================
echo.
pause
endlocal
exit /b 0

rem ============================================================
rem Subroutine : WaitForBoot
rem   Tunggu device register, lalu tunggu sys.boot_completed=1.
rem   Timeout 120 detik, lalu abort.
rem ============================================================
:WaitForBoot
echo [*] Menunggu device register ...
"%ADB%" wait-for-device
if errorlevel 1 (
  echo [X] Gagal register device.
  exit /b 1
)

set /a BOOT_COUNT=0
:WAITLOOP
set /a BOOT_COUNT+=1
"%ADB%" shell getprop sys.boot_completed 2>nul | findstr "1" >nul
if errorlevel 1 (
  if !BOOT_COUNT! GEQ 60 (
    echo [X] Boot timeout setelah 120 detik.
    echo     Coba start manual atau pakai AVD lebih ringan.
    pause
    exit /b 1
  )
  timeout /t 2 /nobreak >nul
  goto WAITLOOP
)
echo [v] Emulator boot selesai.
exit /b 0
