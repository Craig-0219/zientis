from mcrcon import MCRcon

class RconManager:
    def __init__(self, host, port, password):
        self.host = host
        self.port = port
        self.password = password
        self.mcr = None

    def connect(self):
        try:
            if self.mcr is None:
                self.mcr = MCRcon(self.host, self.password, port=self.port)
                self.mcr.connect()
        except Exception as e:
            self.mcr = None
            raise e

    def disconnect(self):
        if self.mcr:
            try:
                self.mcr.disconnect()
            except Exception:
                pass
            self.mcr = None

    def run_command(self, cmd):
        try:
            if self.mcr is None:
                self.connect()
            return self.mcr.command(cmd)
        except Exception as e:
            self.disconnect()
            raise e

    def reset_plugman_cache(self):
        self._plugman_available = None

    def check_plugman_available(self):
        try:
            resp = self.run_command("plugman help")
            return "PlugMan" in resp
        except Exception:
            return False

    def reload_plugin(self, plugin_name):
        return self.run_command(f"plugman reload {plugin_name}")

    def enable_plugin(self, plugin_name):
        return self.run_command(f"plugman enable {plugin_name}")

    def disable_plugin(self, plugin_name):
        return self.run_command(f"plugman disable {plugin_name}")

    def get_online_players(self):
        resp = self.run_command("list")
        if isinstance(resp, str) and "There are" in resp:
            try:
                parts = resp.split(":")
                if len(parts) > 1:
                    players = [p.strip() for p in parts[1].split(",") if p.strip()]
                    return players
            except Exception:
                pass
        return []
