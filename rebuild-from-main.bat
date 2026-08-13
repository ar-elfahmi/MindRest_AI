@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

rem ================================================================
rem   MindRest AI  -  Rebuild APK From Zero (latest main)
rem
rem   Tujuan:
rem     1. Sync ke main terbaru (git fetch + checkout + pull --rebase)
rem     2. Wipe build cache (gradlew clean)
rem     3. Build APK dari 0 dengan --rerun-tasks --no-build-cache
rem     4. Force-stop app + install ulang + launch
rem
rem   CATATAN:
rem     - Script ini AKAN checkout paksa ke main.
rem       Kalau Anda di branch lain dengan uncommitted work,
rem       commit dulu atau stash sebelum run.
rem     - Cold rebuild butuh ~3-5 menit (vs incremental 30-90 detik).
rem     - Menggantikan: build.bat, deploy.bat, run.bat, scripts/run-quiet.bat
rem ================================================================

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

echo ============================================
echo   MindRest AI  -  Rebuild From Main (Zero)
echo ============================================
echo.

rem -----------------------------------------------------------
rem 0) Safety: cek working tree tracked bersih
rem    Untracked files OK (artefak kerja, .env, dst.)
rem    Kalau tracked ada modifikasi, abort supaya tidak hilang
rem -----------------------------------------------------------
echo [*] Checking working tree (tracked files)...
git diff --quiet HEAD
if errorlevel 1 (
  echo [X] Ada modifikasi tracked yang belum di-commit.
  echo     Commit atau stash dulu, lalu run ulang.
  echo.
  git status --short
  pause
  exit /b 1
)
echo [v] Working tree bersih (tracked).
echo.

rem -----------------------------------------------------------
rem 1) Sync ke main terbaru
rem -----------------------------------------------------------
echo [*] Fetch origin main ...
git fetch origin main
if errorlevel 1 (
  echo [X] Gagal fetch dari origin.
  pause
  exit /b 1
)

echo [*] Checkout main ...
git checkout main
if errorlevel 1 (
  echo [X] Gagal checkout main.
  pause
  exit /b 1
)

echo [*] Pull --rebase origin main ...
git pull --rebase origin main
if errorlevel 1 (
  echo [X] Gagal pull --rebase. Mungkin ada konflik.
  echo     Resolve manual lalu run ulang.
  pause
  exit /b 1
)

for /f "delims=" %%H in ('git rev-parse --short HEAD') do set "HEAD_HASH=%%H"
for /f "delims=" %%S in ('git log -1 --format^=%%s') do set "HEAD_SUBJ=%%S"
echo [v] Sekarang di main @ %HEAD_HASH% - %HEAD_SUBJ%
echo.

rem -----------------------------------------------------------
rem 2) Kill stale Gradle daemon
rem -----------------------------------------------------------
echo [*] Membersihkan Gradle daemon stale...
"%GRADLE%" --stop 2>nul
timeout /t 2 /nobreak >nul
echo [v] Done.
echo.

rem -----------------------------------------------------------
rem 3) Pastikan emulator jalan
rem    Max 120 detik boot timeout
rem -----------------------------------------------------------
"%ADB%" devices | findstr "emulator-" >nul
if errorlevel 1 (
  echo [*] Emulator belum jalan. Memulai %AVD% ...
  start "" /B "%EMU%" -avd %AVD% -gpu host -memory 1536 -no-boot-anim -netdelay none -netspeed full
  echo [*] Menunggu device ready (maks 120 detik)...
  "%ADB%" wait-for-device
  set /a BOOT_COUNT=0
  :WAITBOOT
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
    goto WAITBOOT
  )
  echo [v] Emulator sudah boot.
) else (
  echo [v] Emulator sudah berjalan.
)
echo.

rem -----------------------------------------------------------
rem 4) Clean build cache (rebuild dari 0)
rem -----------------------------------------------------------
echo [*] Clean build cache ...
"%GRADLE%" clean --console=plain
if errorlevel 1 (
  echo [X] Gagal clean.
  pause
  exit /b 1
)
echo [v] Clean selesai.
echo.

rem -----------------------------------------------------------
rem 5) Build APK dari 0 dengan --rerun-tasks --no-build-cache
rem    Estimate: 3-5 menit (cold rebuild)
rem -----------------------------------------------------------
echo [*] Build APK dari 0 (:app:assembleDebug --rerun-tasks --no-build-cache) ...
echo     ^> Estimate: 3-5 menit (cold rebuild)
cd /d "%~dp0"
"%GRADLE%" :app:assembleDebug --rerun-tasks --no-build-cache --console=plain
if errorlevel 1 (
  echo.
  echo [X] BUILD GAGAL. Periksa error di atas.
  pause
  exit /b 1
)
echo [v] Build sukses.
echo.

rem -----------------------------------------------------------
rem 6) Verify APK exists
rem    Kadang Gradle return success tapi APK tidak ter-generate
rem -----------------------------------------------------------
if not exist "%APK%" (
  echo [X] APK tidak ditemukan: %APK%
  echo     Build "sukses" tapi file APK tidak ada. Investigasi diperlukan.
  pause
  exit /b 1
)
echo [v] APK ready: %APK%
echo.

rem -----------------------------------------------------------
rem 7) Force-stop app lama sebelum install
rem    Kalau app sedang jalan lalu di-install ulang, kadang
rem    trigger force-close tiba-tiba karena file conflict.
rem -----------------------------------------------------------
echo [*] Force-stop app lama...
"%ADB%" shell am force-stop %PKG%
echo [v] Done.
echo.

rem -----------------------------------------------------------
rem 8) Install APK baru (replace existing)
rem    -r = replace; tanpa uninstall supaya data user
rem    (preferences, session login) tidak hilang.
rem -----------------------------------------------------------
echo [*] Install APK baru...
"%ADB%" install -r "%APK%"
if errorlevel 1 (
  echo [X] INSTALL GAGAL.
  echo     Coba: uninstall manual (%ADB% uninstall %PKG%) lalu run lagi.
  pause
  exit /b 1
)
echo [v] Install sukses.
echo.

rem -----------------------------------------------------------
rem 9) Launch aplikasi
rem -----------------------------------------------------------
echo [*] Membuka aplikasi...
"%ADB%" shell monkey -p %PKG% -c android.intent.category.LAUNCHER 1 >nul
echo.

echo ============================================
echo  REBUILD SELESAI!
echo ============================================
echo  Branch : main @ %HEAD_HASH%
echo  Subject: %HEAD_SUBJ%
echo  APK    : %APK%
echo  Package: %PKG%
echo  AVD    : %AVD%
echo.
pause
endlocal
