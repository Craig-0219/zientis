import os
import json

class LangManager:
    """
    多語系管理。預設放在 translations/zh_TW.json、translations/en_US.json
    """
    def __init__(self, lang: str = "zh_TW"):
        self.lang = lang
        self.translations = {}
        self.load_lang(self.lang)

    def load_lang(self, lang):
        path = os.path.join("translations", f"{lang}.json")
        if os.path.exists(path):
            with open(path, "r", encoding="utf-8") as f:
                self.translations = json.load(f)
        else:
            self.translations = {}

    def tr(self, key: str) -> str:
        return self.translations.get(key, key)
