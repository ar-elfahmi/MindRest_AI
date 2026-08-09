@echo off
chcp 65001 >nul
setlocal

rem ====== KONFIGURASI ======
set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
set "ANDROID_SDK=%LOCALAPPDATA%\Android\Sdk"
set "ANDROID_AVD_HOME=C:\laragon\config\.android\avd"
set "ADB=%ANDROID_SDK%\platform-tools\adb.exe"
set "EMU=%ANDROID_SDK%\emulator\emulator.exe"
set "GRADLE=%~dp0gradlew.bat"
set "AVD=Pixel_10"
set "APK=%~dp0app\build\outputs\apk\debug\app-debug.apk"
set "PKG=com.aistudio.mindrest.eedcdb"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo ============================================
echo   MindRest AI  -  Deploy (Build + Install)
echo ============================================
echo.

rem 1) Pastikan emulator jalan
"%ADB%" devices | findstr "emulator-" >nul
if errorlevel 1 (
  echo [*] Emulator belum jalan. Memulai %AVD% ...
  start "MindRest Emulator" "%EMU%" -avd %AVD% -no-snapshot-load -no-boot-anim -netdelay none -netspeed full
  echo [*] Menunggu device terdeteksi...
  "%ADB%" wait-for-device
  echo [*] Menunggu boot selesai...
  :WAITBOOT
  "%ADB%" shell getprop sys.boot_completed 2>nul | findstr "1" >nul
  if errorlevel 1 (
    timeout /t 2 /nobreak >nul
    goto WAITBOOT
  )
) else (
  echo [v] Emulator sudah berjalan.
)

rem 2) Build APK baru
echo.
echo [*] Build APK (assembleDebug) ...
echo     ^> ini butuh 30-90 detik tergantung perubahan
cd /d "%~dp0"
call "%GRADLE%" assembleDebug --console=plain
if errorlevel 1 (
  echo.
  echo [X] BUILD GAGAL. Periksa error di atas.
  pause
  exit /b 1
)

rem 3) Install APK
echo.
echo [*] Menginstall APK ke emulator...
"%ADB%" install -r "%APK%"

rem 4) Buka aplikasi
echo [*] Membuka aplikasi...
"%ADB%" shell monkey -p %PKG% -c android.intent.category.LAUNCHER 1 >nul

echo.
echo ============================================
echo  SELESAI! Aplikasi MindRest AI sudah diperbarui.
echo ============================================
echo.
pause
endlocal
