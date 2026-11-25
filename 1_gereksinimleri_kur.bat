@echo off
echo Git ve JDK 8 kuruluyor (Android Studio'yu ayrica kurman gerekir)...
echo.
winget install --id Git.Git -e --source winget
winget install --id EclipseAdoptium.Temurin.8.JDK -e --source winget
echo.
echo Bitti. Bu pencereyi kapat. Android Studio aciksa kapatip yeniden ac.
echo winget bulunamadi hatasi aldiysan: git-scm.com/download/win ve adoptium.net (Temurin 8) adreslerinden elle kur.
pause
