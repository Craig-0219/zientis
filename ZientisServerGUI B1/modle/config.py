import os
import json
from cryptography.fernet import Fernet

class ConfigManager:
    """
    管理配置檔案的讀寫和密碼加密/解密。
    """
    CONFIG_FILE = "launcher_config.json"
    KEY_FILE = "key.key"

    def __init__(self):
        self.key = self._load_key()

    def _load_key(self):
        if not os.path.exists(self.KEY_FILE):
            self._generate_key()
        with open(self.KEY_FILE, "rb") as f:
            return f.read()

    def _generate_key(self):
        key = Fernet.generate_key()
        with open(self.KEY_FILE, "wb") as f:
            f.write(key)

    def encrypt_password(self, password: str) -> str:
        f = Fernet(self.key)
        return f.encrypt(password.encode()).decode()

    def decrypt_password(self, encrypted: str) -> str:
        f = Fernet(self.key)
        return f.decrypt(encrypted.encode()).decode()

    def load(self) -> dict:
        if os.path.exists(self.CONFIG_FILE):
            try:
                with open(self.CONFIG_FILE, "r", encoding="utf-8") as f:
                    return json.load(f)
            except Exception:
                return {}
        return {}

    def save(self, config: dict):
        with open(self.CONFIG_FILE, "w", encoding="utf-8") as f:
            json.dump(config, f, ensure_ascii=False, indent=2)
