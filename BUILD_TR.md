# APK derleme rehberi (Windows + Android Studio)

Bu zip derlemeye hazırdır. İçinde zaten var: **Java 8 / 17 / 21** (resmi PojavLauncher "gladiolus" sürümünden:
8u442, 17.0.14, 21.0.1) ve **LTW** renderer'ı. Elle bir şey kopyalaman gerekmez.
Derlemeyi ben denemedim. İlk denemede hata çıkarsa "Sorun giderme"ye bak.

## 1. Bir kere kurulacaklar
1. **Android Studio**: developer.android.com/studio
2. **`1_gereksinimleri_kur.bat`** dosyasına çift tıkla (Git ve JDK 8 kurar).
   Git, derleme betiği `git` komutunu çalıştırdığı için gerekir, GitHub hesabı gerekmez.
   JDK 8, `jre_lwjgl3glfw` modülü Java 8 istediği için gerekir.
   `winget` çalışmazsa: git-scm.com/download/win ve adoptium.net (Temurin 8) adreslerinden elle kur.

## 2. Derle
1. Android Studio > **File > Open**: içinde `gradlew` ve `settings.gradle` olan klasörü seç.
2. "Trust project" de. Gradle eşitlemesi başlar. SDK 34 ve **NDK 25.2.9519653** eksikse kurmayı önerir:
   kabul et, lisansları onayla. İlk seferde 10–30 dk ve birkaç GB indirme sürebilir.
   Önermezse: Tools > SDK Manager > SDK Tools > Show Package Details > NDK (Side by side) > 25.2.9519653.
3. Eşitleme bitince menüden **Build > Build Bundle(s) / APK(s) > Build APK(s)** de.
   (Alternatif: `2_apk_derle.bat`. Android Studio'yu en az bir kez açıp eşitleme bittikten sonra çalışır.)
4. APK: `app_pojavlauncher\build\outputs\apk\debug\app_pojavlauncher-debug.apk`
5. APK'yı telefona gönder ve kur. Aynı paket adıyla resmi Pojav kuruluysa imza çakışır:
   önce kaldırman gerekir ve **oyun verilerin silinir**, yedekle.

## Sorun giderme
Android Studio'nun alttaki **Build** sekmesindeki hata metnini (ya da `2_apk_derle.bat` penceresindekini) kopyalayıp gönder.
* `No matching toolchains found ... languageVersion=8`: JDK 8 kurulu değil ya da kurulumdan önce Android Studio açıktı.
  Kur, Android Studio'yu kapatıp aç, tekrar dene.
* `A problem occurred starting process 'command 'git''`: Git kurulu değil ya da PATH'e girmedi. Bilgisayarı yeniden başlat.
* NDK / SDK bulunamadı: yukarıdaki SDK Manager adımı.
* `Android Gradle plugin requires Java 17`: Gradle'ı Java 8 ile çalıştırıyorsun. Derlemeyi Android Studio menüsünden yap.
* Derleme biter ama oyun açılırken "runtime" hatası: logu gönder.

## Notlar
* Java 25 çalışma zamanı pakette yok (launcher tarafı hazır). Ayrıntı: `FPS_MOD_NOTES.md`.
* Yaptığım değişiklikler: `FPS_MOD_NOTES.md`. Yalnızca kaynak değişiklikleri: `pojav-fps-mod.patch`.
