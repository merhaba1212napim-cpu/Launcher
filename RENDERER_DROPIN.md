# Görüntü işleyici (renderer) ekleme

Renderer kütüphanesini `app_pojavlauncher/src/main/jniLibs/arm64-v8a/` klasörüne
`libr_<isim>.so` adıyla koy. Derleyince Ayarlar > Video işleyici içinde
`<isim> (özel renderer)` olarak çıkar. Ek dosya gerekmez.

Kurallar:
- Dosya adı `libr_` ile başlayıp `.so` ile bitmeli. İsim sadece harf, rakam, `_` ve `-` içerebilir (en fazla 32 karakter).
- Kütüphane hem EGL hem GL sağlamalı (libltw_custom.so gibi). Sadece GL veren bir kütüphane bu şekilde çalışmaz.
- Cihazın OpenGL ES 3 desteği olmalı, yoksa listede görünmez.
- Sadece arm64 cihazlar için: kütüphaneyi `arm64-v8a` klasörüne koy.
