@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

rem ================================================================
rem   MindRest AI  -  Build + Deploy ke Emulator
rem
rem   Tujuan:
rem     1. Kill stale Gradle daemon (cegah hang/crash)
rem     2. Pastikan emulator hidup + boot selesai
rem     3. Build APK incremental dari source code lokal TERBARU
rem     4. Force-stop app lama (cegah force-close dari running state)
rem     5. Install APK baru + launch
rem
rem   Penting:
rem     - TIDAK pakai --rerun-tasks (overkill, bisa trigger daemon crash)
rem     - TIDAK clean build cache (boros waktu, sumber force-close memory)
rem     - Pakai incremental build: Gradle auto-detect source change
rem     - Pakai :app:assembleDebug (cuma rebuild app, bukan semua module)
rem ================================================================

rem ====== KONFIGURASI ======
set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
set "ANDROID_SDK=%LOCALAPPDATA%\Android\Sdk"
set "ADB=%ANDROID_SDK%\platform-tools\adb.exe"
set "EMU=%ANDROID_SDK%\emulator\emulator.exe"
set "GRADLE=%~dp0gradlew.bat"
set "AVD=Pixel_10"
set "APK=%~dp0app\build\outputs\apk\debug\app-debug.apk"
set "PKG=com.aistudio.mindrest.eedcdb"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo ============================================
echo   MindRest AI  -  Build + Deploy
echo ============================================
echo.

rem -----------------------------------------------------------
rem 0) Kill stale Gradle daemon
rem    Mencegah hang/crash dari daemon lama yang masih jalan
rem -----------------------------------------------------------
echo [*] Membersihkan Gradle daemon stale...
"%GRADLE%" --stop 2>nul
timeout /t 2 /nobreak >nul
echo [v] Done.
echo.

rem -----------------------------------------------------------
rem 1) Pastikan emulator jalan
rem    Max 120 detik boot timeout, supaya tidak loop forever
rem -----------------------------------------------------------
"%ADB%" devices | findstr "emulator-" >nul
if errorlevel 1 (
  echo [*] Emulator belum jalan. Memulai %AVD% ...
  start "" /B "%EMU%" -avd %AVD% -no-boot-anim -netdelay none -netspeed full
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
rem 2) Build APK (incremental dari source code lokal terbaru)
rem    Gradle otomatis deteksi source yang berubah dari file mtime.
rem    Output: app\build\outputs\apk\debug\app-debug.apk
rem -----------------------------------------------------------
echo [*] Build APK incremental :app:assembleDebug ...
echo     ^> Gradle akan skip task yang source-nya tidak berubah
echo     ^> Estimate: 30-90 detik (incremental), 2-4 menit (cold)
cd /d "%~dp0"
"%GRADLE%" :app:assembleDebug --console=plain
if errorlevel 1 (
  echo.
  echo [X] BUILD GAGAL. Periksa error di atas.
  pause
  exit /b 1
)
echo [v] Build sukses.
echo.

rem -----------------------------------------------------------
rem 3) Verify APK exists
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
rem 4) Force-stop app lama sebelum install
rem    Kalau app sedang jalan lalu di-install ulang, kadang
rem    trigger force-close tiba-tiba karena file conflict.
rem -----------------------------------------------------------
echo [*] Force-stop app lama...
"%ADB%" shell am force-stop %PKG%
echo [v] Done.
echo.

rem -----------------------------------------------------------
rem 5) Install APK baru (replace existing)
rem    -r = replace; tanpa uninstall supaya data user (preferences,
rem    session login) tidak hilang.
rem -----------------------------------------------------------
echo [*] Install APK...
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
rem 6) Launch aplikasi
rem -----------------------------------------------------------
echo [*] Membuka aplikasi...
"%ADB%" shell monkey -p %PKG% -c android.intent.category.LAUNCHER 1 >nul
echo.

echo ============================================
echo  SELESAI!
echo ============================================
echo APK : %APK%
echo PKG : %PKG%
echo AVD : %AVD%
echo.
pause
endlocal