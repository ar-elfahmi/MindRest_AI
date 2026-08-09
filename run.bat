@echo off
chcp 65001 >nul
setlocal

rem ====== KONFIGURASI ======
set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
set "ANDROID_SDK=%LOCALAPPDATA%\Android\Sdk"
set "ANDROID_AVD_HOME=C:\laragon\config\.android\avd"
set "ADB=%ANDROID_SDK%\platform-tools\adb.exe"
set "EMU=%ANDROID_SDK%\emulator\emulator.exe"
set "AVD=Pixel_10"
set "APK=%~dp0app\build\outputs\apk\debug\app-debug.apk"
set "PKG=com.aistudio.mindrest.eedcdb"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo ============================================
echo   MindRest AI  -  Run via Emulator (CLI)
echo ============================================
echo.

rem 1) Cek APK
if not exist "%APK%" (
  echo [X] APK belum ada. Jalankan dulu:
  echo     gradlew.bat assembleDebug
  pause
  exit /b 1
)

rem 2) Start emulator (windowed, cold boot) jika belum jalan
"%ADB%" devices | findstr "emulator-" >nul
if errorlevel 1 (
  echo [*] Menjalankan emulator %AVD% ...
  start "MindRest Emulator" "%EMU%" -avd %AVD% -no-snapshot-load -no-boot-anim -netdelay none -netspeed full
) else (
  echo [*] Emulator sudah berjalan, lanjut.
)

rem 3) Tunggu device terdeteksi
echo [*] Menunggu device terdeteksi...
"%ADB%" wait-for-device

rem 4) Tunggu boot selesai
echo [*] Menunggu boot selesai (sys.boot_completed)...
:WAITBOOT
"%ADB%" shell getprop sys.boot_completed 2>nul | findstr "1" >nul
if errorlevel 1 (
  timeout /t 2 /nobreak >nul
  goto WAITBOOT
)
echo [v] Boot selesai.

rem 5) Install / update APK
echo [*] Menginstall APK...
"%ADB%" install -r "%APK%"

rem 6) Buka aplikasi
echo [*] Membuka aplikasi...
"%ADB%" shell monkey -p %PKG% -c android.intent.category.LAUNCHER 1 >nul

echo.
echo ============================================
echo  Selesai! Aplikasi MindRest AI sudah terbuka.
echo  (window ini boleh ditutup, emulator tetap jalan)
echo ============================================
echo.
echo Tips geser window emulator: tekan Alt+Spasi lalu M,
echo lalu gunakan panah / gerakkan mouse untuk memindahkan.
pause
endlocal
