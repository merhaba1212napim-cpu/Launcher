# Enhanced Launcher: ne yapacaksın

Derlemeyi ben bu ortamda yapamıyorum (Android SDK ve Google sunucularına erişim yok), bu yüzden derleme sende.
Aşağıdaki adımlar sırayla. Bir adımda takılırsan VS Code terminalindeki hata metnini bana gönder.

## 1. Uygulama adı (zaten yapıldı)
* Görünen ad artık **Enhanced Launcher**, 48 dilin hepsinde. Paket adı (`net.kdt.pojavlaunch`) bilerek değiştirilmedi.
* Yazımı "Enchanced" yerine "Enhanced" yaptım (doğru yazım bu). Mutlaka "Enchanced" istiyorsan
  `app_pojavlauncher/src/main/res/values*/strings.xml` içinde `Enhanced` kelimesini değiştirmen yeterli.

## 2. İkon (senin görselin)
1. Kare, en az 1024x1024 bir PNG hazırla (ör. Canva'da). Arka planı şeffaf olabilir.
2. Proje klasöründe PowerShell aç ve şunu çalıştır (bir kere):
   ```
   pip install pillow
   python ikon_degistir.py C:\Users\uzulm\Desktop\ikonun.png
   ```
   Dosya yolunu kendi görselinin yoluyla değiştir. Script tüm boyutları ve adaptif ikonu otomatik üretir.
3. Alternatif (Android Studio): `res` klasörüne sağ tık > New > Image Asset, görselini seç, kaydet.
Görsel 1024'ten küçükse script reddeder, daha büyük bir görsel kullan.

## 3. Derle (VS Code)
1. Bir kere: `1_gereksinimleri_kur.bat` çalıştır (Git ve JDK 8).
2. Bir kere: Android Studio'da projeyi aç, Gradle eşitlemesinin bitmesini bekle. SDK ve NDK bu adımda kurulur.
3. VS Code'da proje klasörünü aç. Terminal > New Terminal (PowerShell) ve şunu yaz:
   ```
   .\gradlew.bat :app_pojavlauncher:assembleDebug
   ```
4. Sonuç: `app_pojavlauncher\build\outputs\apk\debug\app_pojavlauncher-debug.apk`

Debug sürümü paket adına `.debug` ekler (`net.kdt.pojavlaunch.debug`), yani orijinal PojavLauncher'ın yanına
kurulur, onu kaldırmana gerek yoktur.

## 4. Demo modu sorunu (düzeltildi)
* Önceki değişiklikte Microsoft giriş kontrolü kapatılmıştı, bu yüzden yerel (giriş yapılmamış) hesapla oyun
  demo modunda açılıyordu. Orijinal kontrol geri getirildi, `MinecraftDownloader.java` artık orijinaliyle birebir aynı.
* Sonuç: Oyunu tam açmak için launcher'da **Microsoft hesabıyla giriş** yapman gerekir. Giriş yapılmadan
  oyun dosyası indirilmez (bu orijinal davranış).

## Ne değişti (özet)
* Uygulama adı: Enhanced Launcher (48 dil).
* Demo sorunu: orijinal Microsoft giriş kontrolü geri konuldu.
* Önceki turdaki Java 8/17/21, LTW ve ANGLE değişiklikleri olduğu gibi duruyor (ayrıntı: `LTW_ANGLE_NOTES.md`).
* İkon scripti: `ikon_degistir.py`.
