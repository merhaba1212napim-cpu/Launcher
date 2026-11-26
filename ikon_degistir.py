#!/usr/bin/env python3
"""
Enhanced Launcher ikonunu değiştirir. Nerede çalıştırırsan çalıştır çalışır.

Kullanım:
    pip install pillow
    python ikon_degistir.py C:\\yol\\ikonun.png

Girdi: kare PNG, 1024x1024 önerilir (en az 192x192).
Çıktı:
  - app_pojavlauncher/ikon/ikon_1024.png  (kaynak kopyası)
  - app_pojavlauncher/ikon/ikon_192.png   (uygulamanın ana ikon boyutu)
  - tüm mipmap klasörleri (uygulama bunları kullanır)
"""
import sys, pathlib
from PIL import Image

HERE = pathlib.Path(__file__).resolve().parent
APP = HERE / "app_pojavlauncher"
RES = APP / "src" / "main" / "res"
OUT = APP / "ikon"
DENSITIES = {"mdpi": 1.0, "hdpi": 1.5, "xhdpi": 2.0, "xxhdpi": 3.0, "xxxhdpi": 4.0}

def main():
    if len(sys.argv) != 2:
        sys.exit(__doc__)
    src_path = pathlib.Path(sys.argv[1].strip('"'))
    if not src_path.is_file():
        sys.exit(f"Hata: dosya bulunamadı: {src_path}")
    if not RES.is_dir():
        sys.exit(f"Hata: proje klasörü bulunamadı ({RES}). Scripti proje klasörünün içine koy.")

    src = Image.open(src_path).convert("RGBA")
    if src.width != src.height or src.width < 192:
        sys.exit(f"Hata: görsel kare ve en az 192x192 olmalı (şu an {src.width}x{src.height}).")

    OUT.mkdir(parents=True, exist_ok=True)
    master = src.resize((1024, 1024), Image.LANCZOS)
    master.save(OUT / "ikon_1024.png")
    master.resize((192, 192), Image.LANCZOS).save(OUT / "ikon_192.png")

    for density, scale in DENSITIES.items():
        folder = RES / f"mipmap-{density}"
        folder.mkdir(parents=True, exist_ok=True)
        icon = master.resize((round(48 * scale),) * 2, Image.LANCZOS)
        icon.save(folder / "ic_launcher.png")
        icon.save(folder / "ic_launcher_round.png")
        fg_size = round(108 * scale)
        inner = round(fg_size * 0.66)
        art = master.resize((inner, inner), Image.LANCZOS)
        fg = Image.new("RGBA", (fg_size, fg_size), (0, 0, 0, 0))
        fg.paste(art, ((fg_size - inner) // 2, (fg_size - inner) // 2), art)
        fg.save(folder / "ic_launcher_foreground.png")
        print(f"ok: mipmap-{density}")
    print(f"Bitti. Önizleme: {OUT / 'ikon_192.png'}")

if __name__ == "__main__":
    main()
