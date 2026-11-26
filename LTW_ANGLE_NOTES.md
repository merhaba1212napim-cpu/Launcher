# LTW, ANGLE ve Java değişiklikleri

Bu değişiklikler `merhaba1212napim-cpu/Launcher` deposunun üzerine yapıldı. Derlemeyi ben bu ortamda
yapamadım (Android SDK/NDK yok), **telefonda denenmedi.** İlk denemede hata ya da çökme olursa logu gönder.

## 1. Java 8 / 17 / 21 (eksikti, düzeltildi)
* `components/jre-new` klasöründeki Java'nın sürümü **1.8.0_482** idi, yani Java 8'di. Launcher bunu
  Java 17 sanıyordu, bu yüzden 17 gereken sürümler hiç açılmıyordu. Değiştirildi.
* Şimdi paketli olanlar (resmi PojavLauncher `gladiolus` sürümünden, sürümleri doğrulandı):
  * `components/jre` → Java 8 (1.8.0_442)
  * `components/jre-new` → Java 17 (17.0.14)
  * `components/jre-21` → Java 21 (21.0.1)
* Java 25 pakette yok. Minecraft 26.1 ve üzeri için Java 25 gerekir, Ayarlar > Java'dan elle yüklenmeli.

## 2. LTW (renderer) eklendi
* `libs/ltw-release.aar` depoda yoktu, bu yüzden `libltw.so` APK'ya girmiyordu ve LTW hiç listelenmiyordu.
  Eklendi. `build.gradle` zaten `libs/*.aar` dosyasını alıyor, ek bir ayar gerekmez.
* **1.17 ve üzeri otomatik LTW:** Bir sürüm açılırken yerel sürüm dosyası (`versions/<ad>/<ad>.json`)
  okunur. Mod yükleyici kurulumlarında (`inheritsFrom` alanı varsa) asıl oyun sürümüne bakılır.
  1.17 ve üzeriyse LTW seçilir, değilse seçtiğin renderer kullanılır.
* Öncelik sırası: **profilin kendi renderer'ı** > **1.17+ için otomatik LTW** > **genel ayar**.
  Her açılışta yeniden hesaplanır, önceki bir LTW seçimi eski sürümlere sızmaz.
* Ayarlar > Video > "1.17 ve üzeri için LTW otomatik" ile kapatılabilir (varsayılan açık).
* 2021 snapshot'ları ve tanınmayan sürüm adları için karar vermez, kullanıcının seçimi geçerli olur.

## 3. ANGLE (deneysel)
* `jniLibs/arm64-v8a` ve `jniLibs/armeabi-v7a` içine `libEGL_angle.so` ve `libGLESv2_angle.so` eklendi
  (`abnormalmaps/build-angle` sürümü `angle-fd958ac`, Android için derlenmiş dosyalar; ELF ve export'ları kontrol edildi).
* ANGLE tek başına renderer olarak kullanılamaz: Minecraft'ın masaüstü GL çağrılarını (glBegin vb.)
  çalıştıramaz. Bu yüzden LTW'nin GL çevirisi kalıyor, sadece EGL katmanı ANGLE olur.
* Ayarlar > Video > "LTW altında ANGLE EGL (deneysel)" ile açılır, **varsayılan kapalı.**
  Yalnızca LTW seçiliyken, Android 8.0+ cihazlarda ve ANGLE dosyası bulunursa devreye girer.
* Açıkça söylemem gerekir: bu yolu test edemedim. Çökerse ya kapat ya da logu gönder.

## 4. gradlew.bat satır sonları
* Dosya LF (Unix) satır sonuyla kaydedilmişti. Windows `cmd` ve VS Code terminali bu tür `.bat`
  dosyalarında etiket/goto hatası verebilir. CRLF'ye çevrildi.

## 5. VS Code ile derleme
1. JDK 8 kurulu olmalı (`jre_lwjgl3glfw` modülü Java 8 ister). Yoksa `1_gereksinimleri_kur.bat` çalıştır.
2. Android Studio'da bir kez proje açıp Gradle eşitlemesini tamamla (SDK 34 ve NDK 25.2.9519653 kurulsun).
   Bu adım SDK'yı oluşturur, VS Code bu olmadan derleyemez.
3. VS Code'da proje klasörünü aç, terminali PowerShell olarak seç ve şunu çalıştır:
   ```
   .\gradlew.bat :app_pojavlauncher:assembleDebug
   ```
4. APK: `app_pojavlauncher\build\outputs\apk\debug\app_pojavlauncher-debug.apk`

## 6. Dikkat: MinecraftDownloader değişikliği
Depoda `MinecraftDownloader.java` içinde `if(isLocalProfile)` kontrolü `if(false)` ile kapatılmış. Bu, orijinal
kodun "Microsoft hesabıyla giriş yapmalısın" uyarısını devre dışı bırakır. Bu değişiklik bu çalışmada
yapılmadı, depoda zaten vardı. Bilerek yaptıysan sorun yok, ama Minecraft'ın lisans koşullarını göz önünde bulundur.
