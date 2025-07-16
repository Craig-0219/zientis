import json
import os

class PlayerRoleManager:
    """
    負責同步和查詢玩家職位（可對接資料庫或伺服器API）
    """
    ROLE_FILE = "player_roles.json"

    def __init__(self, role_file=None):
        self.role_file = role_file or self.ROLE_FILE
        self.roles = self.load_roles()

    def load_roles(self):
        if os.path.exists(self.role_file):
            with open(self.role_file, "r", encoding="utf-8") as f:
                return json.load(f)
        return {}

    def save_roles(self):
        with open(self.role_file, "w", encoding="utf-8") as f:
            json.dump(self.roles, f, ensure_ascii=False, indent=2)

    def get_role(self, name):
        """回傳職位(字串)，找不到預設玩家"""
        return self.roles.get(name, "玩家")

    def update_role(self, name, role):
        """手動同步/變更單一玩家職位"""
        self.roles[name] = role
        self.save_roles()

    def sync_roles_from_server(self, player_list):
        """
        可對接伺服器外掛或API，這裡僅示意全部預設為'玩家'
        你可根據伺服器權限指令API自動刷新
        """
        # Example: 接外掛/資料庫獲得的名單與職位後同步 roles
        for name in player_list:
            if name not in self.roles:
                self.roles[name] = "玩家"
        self.save_roles()
