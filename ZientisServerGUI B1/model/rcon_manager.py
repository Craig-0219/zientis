from mcrcon import MCRcon

class RconManager:
    """
    RCON 指令與狀態查詢封裝。
    """
    def __init__(self, host: str, port: int, password: str):
        self.host = host
        self.port = port
        self.password = password

    def send_command(self, cmd: str) -> str:
        with MCRcon(self.host, self.password, port=self.port, timeout=3) as mcr:
            return mcr.command(cmd)
