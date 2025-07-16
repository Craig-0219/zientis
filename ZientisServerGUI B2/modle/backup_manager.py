import os
import zipfile
from datetime import datetime
import shutil

class BackupManager:
    """
    管理世界備份與備份清理（已移除還原功能）。
    """
    def __init__(self, world_path: str, backup_dir: str, max_backups: int = 5):
        self.world_path = world_path
        self.backup_dir = backup_dir
        self.max_backups = max_backups

    def create_backup(self) -> str:
        """
        建立世界資料夾的 zip 備份，回傳備份路徑。
        """
        if not os.path.isdir(self.world_path):
            raise FileNotFoundError("世界資料夾不存在")
        if not os.path.exists(self.backup_dir):
            os.makedirs(self.backup_dir)
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        backup_name = f"world_backup_{timestamp}.zip"
        backup_path = os.path.join(self.backup_dir, backup_name)
        with zipfile.ZipFile(backup_path, "w", zipfile.ZIP_DEFLATED) as zipf:
            for root, _, files in os.walk(self.world_path):
                for file in files:
                    abs_file = os.path.join(root, file)
                    arcname = os.path.relpath(abs_file, self.world_path)
                    zipf.write(abs_file, arcname)
        self.manage_backups()
        return backup_path

    def manage_backups(self):
        """
        保留最新的 max_backups 份備份，其餘自動刪除。
        """
        backups = sorted(
            [f for f in os.listdir(self.backup_dir) if f.endswith(".zip")],
            key=lambda f: os.path.getctime(os.path.join(self.backup_dir, f)),
            reverse=True
        )
        for old in backups[self.max_backups:]:
            os.remove(os.path.join(self.backup_dir, old))

    def list_backups(self):
        """
        列出所有備份檔名，已排序。
        """
        return sorted([f for f in os.listdir(self.backup_dir) if f.endswith(".zip")])
