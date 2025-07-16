# model/player.py
class Player:
    def __init__(self, name, role="玩家"):
        self.name = name
        self.role = role    # 例如 "管理員"、"VIP"、"玩家"等
    def __repr__(self):
        return f"<Player {self.name} ({self.role})>"
