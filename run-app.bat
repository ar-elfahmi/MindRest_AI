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
REM   3. Waits for full boot
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

setlocal
set JAVA_HOME=C:\Program Files\Android\Android Studio\jbr
set PATH=%JAVA_HOME%\bin;%PATH%
set ADB=C:\Users\lenovo\AppData\Local\Android\Sdk\platform-tools\adb.exe
set EMU=C:\Users\lenovo\AppData\Local\Android\Sdk\emulator\emulator.exe
set AVD=Pixel_3a
set ANDROID_AVD_HOME=C:\Users\lenovo\.android\avd
set PKG=com.aistudio.mindrest.eedcdb

cd /d C:\laragon\www\MindRest_AI

echo.
echo === [1/4] Build APK from current HEAD ===
echo       (run `git checkout main` first if you want latest main)
echo.
call gradlew.bat :app:assembleDebug --console=plain
if errorlevel 1 (
  echo BUILD FAILED.
  exit /b 1
)

echo.
echo === [2/4] Check emulator ===
%ADB% devices | findstr "emulator" >nul
if errorlevel 1 (
  echo       no emulator running - launching Pixel_3a ^(lightweight^)
  echo       flags: -gpu host -memory 1536 -no-boot-anim
  start "" "%EMU%" -avd %AVD% -gpu host -memory 1536 -no-boot-anim
  echo       waiting for device to register...
  %ADB% wait-for-device
  echo       waiting for full boot ^(
  :WAIT_BOOT
  timeout /t 5 >nul
  for /f "delims=" %%i in ('%ADB% shell getprop sys.boot_completed') do set BC=%%i
  set BC=%BC: =%
  if not "%BC%"=="1" goto WAIT_BOOT
  echo       boot complete.
) else (
  echo       emulator already running - reusing.
)

echo.
echo === [3/4] Install APK ===
%ADB% install -r app\build\outputs\apk\debug\app-debug.apk
if errorlevel 1 (
  echo INSTALL FAILED.
  exit /b 1
)

echo.
echo === [4/4] Launch MainActivity ===
%ADB% shell am start -n "%PKG%/com.example.MainActivity"

echo.
echo ============================================================
echo  Done. Emulator window has the app open.
echo  Test keyboard: click a text field, then type on laptop.
echo ============================================================
endlocal
