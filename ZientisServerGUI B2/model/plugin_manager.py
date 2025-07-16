import os
import shutil
import zipfile
import yaml
import requests

class PluginManager:
    """
    處理插件的安裝、移除、解析資訊、檢查更新。
    """
    def __init__(self, plugins_dir: str):
        self.plugins_dir = plugins_dir

    def list_plugins(self):
        if not os.path.isdir(self.plugins_dir):
            return []
        return [f for f in os.listdir(self.plugins_dir) if f.endswith(".jar")]

    def install_plugin(self, src_path: str, overwrite: bool = False) -> str:
        if not os.path.isdir(self.plugins_dir):
            os.makedirs(self.plugins_dir)
        dest_path = os.path.join(self.plugins_dir, os.path.basename(src_path))
        if os.path.exists(dest_path) and not overwrite:
            raise FileExistsError("檔案已存在")
        shutil.copy(src_path, dest_path)
        return dest_path

    def remove_plugin(self, jar_name: str):
        jar_path = os.path.join(self.plugins_dir, jar_name)
        if os.path.exists(jar_path):
            os.remove(jar_path)
            return True
        return False

    def parse_plugin_info(self, jar_path: str) -> dict:
        try:
            with zipfile.ZipFile(jar_path, "r") as jar:
                with jar.open("plugin.yml") as yml_file:
                    info = yaml.safe_load(yml_file)
                    return {
                        "name": info.get("name", ""),
                        "version": info.get("version", ""),
                        "author": info.get("author", "")
                    }
        except Exception:
            return {}

    def check_plugin_update(self, plugin_name: str, current_version: str) -> str:
        # 模擬查詢 SpigotMC 版本API
        try:
            resp = requests.get(f"https://api.spigotmc.org/legacy/update.php?resource={plugin_name}", timeout=5)
            if resp.ok:
                latest_version = resp.text
                if latest_version != current_version:
                    return latest_version
        except Exception:
            pass
        return current_version
