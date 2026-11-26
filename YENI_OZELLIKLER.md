# Bu turda ne yapıldı, ne yapılmadı

## Yapıldı
* **Özel LTW renderer'ı:** Yüklediğin `libltw.so` APK'ya `libltw_custom.so` adıyla eklendi (yalnızca arm64).
  Renderer listesinde "LTW (özel kütüphane) - yalnızca 1.17+" olarak çıkar. Resmi LTW (`libltw.so`, aar'dan) ayrı kalır.
  Dosya adı çakışmasın diye yeniden adlandırıldı. Yüklediğin dosyanın EGL çağrıları resmiyle aynı (4 fonksiyon);
  ağ/çalıştırma çağrısı içermiyor. Ama cihazda çalışıp çalışmayacağını test edemedim.
* **Video işleyici alt ekranı:** Video ayarlarında "Video işleyici" satırına basınca açılır. İçinde:
  * Renderer seçimi (özel LTW dahil),
  * **Görüntü ölçekleme profili:** Performans %50, Dengeli %67, Kalite %80, Yerel %100. Mevcut çözünürlük
    ayarını (`resolutionRatio`) yazar, yani kaydırıcıyla aynı etkiyi verir,
  * Çözünürlük kaydırıcısı.
* **Cihaz kartı:** Cihaz adı (üretici + model), chip (SoC), Android sürümü, RAM, çekirdek, 32/64-bit, GPU adı
  (arka planda okunur), tahmini üst sınır.

## Yapılmadı, ve nedeni
* **FSR benzeri keskinleştirme (AMD FSR):** FSR, GPU'da çalışan bir shader geçişidir ve oyunun kare takas
  zincirine girmesi gerekir. Burada GPU yok, körlemesine yazarsam render'ı herkes için bozabilirim.
  Yapılabilecek sonraki adım: FSR 1.0 (EASU + RCAS) shader'ını yazıp yalnızca "Keskinlik" seçeneği olarak,
  varsayılan kapalı ekleyip, cihazda test ettikten sonra açmak.
* **Pelerin (cape):** Minecraft pelerinleri hesaba bağlıdır (Mojang/Minecraft.net), launcher onları değiştiremez.
  Özel pelerin için oyun içi mod (ör. OptiFine/CapeMod) gerekir. Launcher'a eklenemez.
* **Benchmark:** Projede önceden bir benchmark yoktu. Cihaz kartı bunun yerine geçmiyor, FPS ölçümü değil.
  Gerçek bir benchmark için oyunun belirli bir sahnesinde sabit süre çalışıp ortalama FPS yazan bir test gerekir.
* **Bellek yönetimi:** Hangi kısmın kötü çalıştığını bilmeden değiştirmek riskli. Bellek sorunu nerede yaşadığını
  söylersen (çökme, yavaşlama, RAM uyarısı) oraya odaklanırım.
* **"Aklına ne geliyorsa":** Her özelliği aynı anda eklemek, hiçbirinin test edilmeden çalışmamasına yol açar.
  Öncelik sırasını sen seç.

## Bana bildirmen gerekenler
1. Bug listesi: hangi hatalar var (ekran, çökme, yanlış değer gibi)?
2. Yukarıdakiler arasında hangileri önce gelsin?
