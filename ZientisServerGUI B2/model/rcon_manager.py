# model/rcon_manager.py

from mcrcon import MCRcon

class RconManager:
    def __init__(self, host, port, password):
        self.host = host
        self.port = port
        self.password = password
        self._plugman_available = None  # 快取

    def run_command(self, cmd):
        with MCRcon(self.host, self.password, port=self.port) as mcr:
            return mcr.command(cmd)

    def check_plugman_available(self):
        """自動檢查 PlugMan 狀態，快取於本層減少壓力"""
        if self._plugman_available is not None:
            return self._plugman_available
        try:
            resp = self.run_command("plugman help")
            # 只要有 PlugMan 字樣，代表可用
            self._plugman_available = "PlugMan" in resp
        except Exception:
            self._plugman_available = False
        return self._plugman_available

    def reload_plugin(self, plugin_name):
        return self.run_command(f"plugman reload {plugin_name}")

    def enable_plugin(self, plugin_name):
        return self.run_command(f"plugman enable {plugin_name}")

    def disable_plugin(self, plugin_name):
        return self.run_command(f"plugman disable {plugin_name}")

    def reset_plugman_cache(self):
        """有必要時可強制重查（如熱插拔後）"""
        self._plugman_available = None
