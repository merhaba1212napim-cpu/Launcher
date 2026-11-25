# FPS / performans değişiklikleri

Bu ağaç `PojavLauncher-3_openjdk.zip` üzerine yapılmış değişiklikleri içerir. Her şey Android
cihazda **derlenip denenmemiştir** (bu ortamda NDK/Android SDK yok). Doğrulananlar en altta.

## Eklenenler / düzeltilenler

| Alan | Ne yapıldı | Dosyalar |
|---|---|---|
| Büyük çekirdek | `Force the renderer to run on the big core` ayarı daha önce hiçbir şey yapmıyordu (env değişkeni set ediliyor, ama native tarafta kimse okumuyordu). Artık render thread'ine bağlanıyor. Ayrıca tarama hataları düzeltildi: kapalı çekirdekte tarama duruyordu, tampon taşması, yanlış `sched_setaffinity` boyutu. | `jni/bigcoreaffinity.c`, `jni/perf_tuning.c/.h`, `jni/egl_bridge.c` |
| Oyun içi FPS sayacı | Eski sayaç sadece AWT canvas'ını ölçüyordu. Yenisi her `swap`'ı native sayar, ekranda gösterir. Ayar: Video > "Show FPS counter" | `jni/perf_tuning.c`, `jni/environ/environ.h`, `utils/FpsOverlay.java`, `MainActivity.java` |
| Kare hızı sınırı | 30/45/60/90/120 FPS. Native swap yolunda `clock_nanosleep` ile. Ayar: Video > "Frame rate limit" | `jni/perf_tuning.c`, `utils/JREUtils.java`, `prefs/LauncherPreferences.java` |
| JVM ayar profilleri | Default (hiçbir şey eklemez) / Smooth (G1) / Low memory (SerialGC). Kullanıcı zaten bir GC bayrağı yazdıysa profil devreye girmez (aksi halde JVM açılmaz). Ayar: Java > "JVM tuning preset" | `utils/JvmPresets.java`, `utils/JREUtils.java` |
| Java 25 | `Internal-25` çalışma zamanı yuvası eklendi (yalnızca APK'ya paketlenmişse seçilir). Java 24+ için `--enable-native-access=ALL-UNNAMED` ve `--sun-misc-unsafe-memory-access=allow` eklenir (eski sürümlerde eklenmez). | `NewJREUtil.java`, `Tools.java` |
| Türkçe | Yeni ayarların TR metinleri | `res/values-tr/performance_options.xml` |

## Java 25 hakkında (önemli)
Launcher tarafı hazır, **Java 25 çalışma zamanının kendisi bu ağaçta yok.** İki yol:
1. Ayarlar > Java'dan elle bir Java 25 çalışma zamanı yükle, ya da
2. APK'ya paketle: `app_pojavlauncher/src/main/assets/components/jre-25/` içine, `jre-21` ile aynı düzende
   (`version`, `universal.tar.xz`, `bin-<arch>.tar.xz`) koy.
Minecraft 26.1+ sürümleri Java 25 ister; hangi çalışma zamanının seçileceğini sürümün kendi JSON'u belirler.

## Karşılaştırarak test et
Bu ayarların FPS'e etkisi cihaza göre değişir ve **burada ölçülmedi.** Önerilen yöntem:
"Show FPS counter" aç, aynı dünyada aynı noktada her profili/limiti dene, sonucu karşılaştır.
Davranış değiştiren her şey varsayılan olarak kapalıdır (limit: sınırsız, profil: Default, sayaç: kapalı).

## Bilerek yapılmayanlar
* **Yeni renderer'lar:** `.so` ikili dosyaları gerekir, burada derlenemez. Amethyst-Android, MobileGlues ve
  Krypton Wrapper'ı zaten entegre ediyor; taşımak için doğru kaynak orası.
* **9minecraft.com / TLMods:** API'leri yok, yeniden barındırma siteleri. Kazıma kırılgan ve güvenlik riski var.
  CurseForge ve Modrinth zaten bu ağaçta var. CurseForge için derleme sırasında `CURSEFORGE_API_KEY` ortam
  değişkeni ya da proje kökünde `curseforge_key.txt` gerekir.
* **Eski telefonlar için "çevirici":** gl4es/Zink/LTW zaten GL çeviricileridir. Donanımın desteklemediği
  OpenGL/Java sürümünü yazılımla kapatan genel bir çevirici yoktur.

## İkinci tur eklemeler

| Alan | Ne yapıldı | Ayar |
|---|---|---|
| Otomatik FPS sınırı | Kare hızı sınırını ekranın yenileme hızına eşitler, böylece oyun ekranın gösteremeyeceği kadar hızlı render etmez. | Video > Kare hızı sınırı > "Otomatik (ekranla eşleştir)" |
| Uyarlanabilir FPS sınırı | Pil ≤%20 ve şarjda değilken, ya da Android 10+'ta ciddi ısınma (thermal throttling) algılanınca sınırı 30 FPS'e düşürür. 8 saniyede bir kontrol eder. Kendi seçtiğin sınırı **asla gevşetmez**, yalnızca daha da sıkılaştırır. | Video > "Uyarlanabilir FPS sınırı (pil/ısı)" |
| En kötü an göstergesi | FPS sayacı artık anlık değerin yanında son ~10 saniyenin en kötü yarım-saniyelik penceresini de gösterir ("58 FPS (en kötü 41)"). Bu gerçek bir "1. yüzdelik" ölçümü değil, kaba bir yaklaşıklıktır; kesin olması için native tarafın her kareyi tek tek kaydetmesi gerekirdi, bunu kapsam dışı tuttum. | Video > "FPS sayacını göster" (zaten açık olan ayarı genişletir) |
| Cihaz ve uyumluluk bilgisi | RAM, çekirdek sayısı, 32/64-bit ve kurulu Java sürümlerine bakarak "muhtemelen hangi sürüme kadar açılır" tahmini verir (örn. "1.20.5 – 26.0"). Sadece Java yeterliliği ve kaba RAM eşiklerine bakar; sürücü çökmelerini ya da mod uyumluluğunu hesaba katmaz. | Video ekranının altında "Cihaz ve uyumluluk bilgisi" |

### Bilerek eklemediklerim (kapsamı dar tutmak için)
* **Yapay zekâ / NPU ile motion blur:** Önceki mesajımda zaten şüpheliydim (NPU erişimi üreticiye göre değişir, her karede ekstra gecikme ekler). Gerçek bir shader geçişi GL takas zincirine kanca atmayı gerektirir ve burada GPU olmadığı için körlemesine yazıp test edemezdim; yanlış yaparsam render'ı herkes için bozabilirdi. Değerinden emin olmadığım ve test edemediğim bu riski almadım.
* **Dinamik çözünürlük, shader önbelleği, sunucuya katıl kısayolu, kurulum/mod paketi paylaşımı, çökme analizcisi, yedekleme, sadece-arm64 derleme:** Her biri ayrı bir ekran akışı, dosya I/O'su ya da ağ entegrasyonu gerektiriyor. "Kendini zorlama" talimatına uyup bu turda dışarıda bıraktım; istersen ayrı ayrı ekleriz.

### Teknik not: adaptif sınır nasıl çalışıyor
Kare sınırlayıcı iki değeri birleştiriyor: kullanıcının Ayarlar'dan seçtiği taban sınır ve pil/ısı denetleyicisinin istediği geçici sınır. İkisinden **daha sıkı olanı** (daha düşük FPS) uygulanır; adaptif denetleyici kullanıcının sınırını hiçbir zaman gevşetmez. Bu paylaşılan değer, launcher (Dalvik) ve oyunun çalıştığı JVM'nin aynı süreç içinde ayrı ayrı yüklediği native kütüphane kopyaları arasında `pojav_environ` paylaşımlı yapısı üzerinden aktarılıyor (frame sayacıyla aynı mekanizma).

## Doğrulananlar
* Native: ASan+UBSan ile host'ta derlenip çalıştırıldı. Sınırlayıcı 30 FPS hedefte 29,99 ölçtü, yavaş karelerde ek gecikme yok,
  sahte sysfs ile (kapalı çekirdek, bozuk veri, taşan dosya) doğru çekirdek seçildi.
* Java: `JvmPresets` ve `FpsOverlay` gerçek `android.jar` (API 33) ile derlendi. Tüm ağacın derleme hataları orijinal ile
  birebir aynı (eksik androidx/R kaynaklı, yeni hata yok). Mutasyon testiyle bu taramanın hata yakaladığı doğrulandı.
* XML: bütünlük ve kaynak referansları kontrol edildi.
* **Doğrulanmadı:** gerçek cihazda derleme/çalıştırma, FPS kazancı, G1/SerialGC profillerinin Pojav JRE'lerinde davranışı.
* İkinci tur: adaptif sınırın "sıkılaştır / gevşetme / temizle / aralık-dışını yok say" mantığı ASan+UBSan ile host'ta test edildi, hepsi geçti. `VersionCompatibilityPredictor` 11 ayrı senaryoyla (RAM/Java kombinasyonları, sınır durumları) test edildi. Yeni Java dosyaları gerçek `android.jar`'a (API 33) karşı derlendi; tüm ağacın derleme hataları, yalnızca bu test ortamında gerçek androidx kütüphanesi bulunmamasından kaynaklanan (projenin geri kalanında zaten var olan) farklarla açıklanabiliyor, başka yeni hata yok. **Doğrulanmadı:** pil/ısı okumalarının gerçek bir telefonda beklendiği gibi tetiklenmesi, "Otomatik" sınırın gerçek ekran yenileme hızını doğru okuması.
