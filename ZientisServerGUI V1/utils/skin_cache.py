# utils/skin_cache.py
import os
from urllib.request import urlopen
from PySide6.QtGui import QPixmap

class SkinCache:
    CACHE_DIR = ".skin_cache"
    def __init__(self):
        if not os.path.exists(self.CACHE_DIR):
            os.makedirs(self.CACHE_DIR)
    def get_pixmap(self, name, size=32):
        cache_path = os.path.join(self.CACHE_DIR, f"{name}_{size}.png")
        if os.path.exists(cache_path):
            pix = QPixmap(cache_path)
            return pix
        skin_url = f"https://crafatar.com/avatars/{name}?size={size}&overlay"
        try:
            data = urlopen(skin_url, timeout=2).read()
            with open(cache_path, "wb") as f:
                f.write(data)
            pix = QPixmap()
            pix.loadFromData(data)
            return pix
        except Exception:
            return QPixmap()
