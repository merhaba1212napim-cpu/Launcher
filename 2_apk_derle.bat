@echo off
rem Android Studio'da projeyi bir kez acip Gradle sync'in bitmesini bekledikten SONRA calistir.
cd /d "%~dp0"
set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
if not exist "%JAVA_HOME%\bin\java.exe" (
  echo Android Studio'nun Java'si bulunamadi: %JAVA_HOME%
  echo Android Studio baska bir yere kurulduysa bu dosyadaki JAVA_HOME satirini duzelt,
  echo ya da derlemeyi Android Studio icinden yap: Build ^> Build Bundle^(s^) / APK^(s^) ^> Build APK^(s^)
  pause
  exit /b 1
)
set "PATH=%JAVA_HOME%\bin;%PATH%"
if not defined ANDROID_HOME if exist "%LOCALAPPDATA%\Android\Sdk" set "ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk"
call gradlew.bat :app_pojavlauncher:assembleDebug
if errorlevel 1 (
  echo.
  echo DERLEME BASARISIZ. Yukaridaki ERROR / FAILED satirlarini kopyalayip gonder.
  pause
  exit /b 1
)
echo.
echo APK hazir: %~dp0app_pojavlauncher\build\outputs\apk\debug\app_pojavlauncher-debug.apk
explorer "%~dp0app_pojavlauncher\build\outputs\apk\debug"
pause
