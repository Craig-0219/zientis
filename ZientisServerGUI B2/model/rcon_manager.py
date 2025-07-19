from mcrcon import MCRcon

class RconManager:
    def __init__(self, host, port, password):
        self.host = host
        self.port = port
        self.password = password
        self._plugman_available = None

    def run_command(self, cmd):
        try:
            with MCRcon(self.host, self.password, port=self.port) as mcr:
                return mcr.command(cmd)
        except Exception as e:
            return f"[RCON ERROR] {type(e).__name__}: {e}"

    def check_plugman_available(self):
        if self._plugman_available is not None:
            return self._plugman_available
        try:
            resp = self.run_command("plugman help")
            self._plugman_available = "PlugMan" in resp if isinstance(resp, str) else False
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
        self._plugman_available = None

    def get_online_players(self):
        resp = self.run_command("list")
        if isinstance(resp, str) and "There are" in resp:
            try:
                parts = resp.split(":")
                if len(parts) > 1 and "players" in parts[1]:
                    players = [p.strip() for p in parts[1].split(",") if p.strip()]
                    return players
                elif len(parts) > 1:
                    players = [p.strip() for p in parts[1].split("") if p.strip()]
                    return players if players and players[0] else []
            except Exception:
                return []
        return []
