# controller/server_controller.py

import os
import platform
import subprocess
import threading

from PySide6.QtCore import QDateTime
from PySide6.QtWidgets import QFileDialog, QListWidgetItem

from model.config import ConfigManager
from model.plugin_manager import PluginManager
from model.backup_manager import BackupManager
from utils.logger import log_info, log_error
from utils.notification import notify

class ServerController:
    """
    業務控制層，負責協調 UI、Model 與 Utils，處理伺服器相關的操作流程。
    """
    def __init__(self, ui):
        self.ui = ui  # UI主窗體物件
        self.config_mgr = ConfigManager()
        self.server_process = None
        self.server_running = False
        self.log_thread = None

        # 狀態記錄與模型預設
        self.config = self.config_mgr.load()
        self.plugin_mgr = None
        self.backup_mgr = None

        self._update_managers()

    def _update_managers(self):
        """根據目前 UI 設定初始化 PluginManager 與 BackupManager"""
        folder = self.config.get("folder", "")
        backup_dir = self.config.get("backup_dir", os.path.join(os.getcwd(), "backups"))
        world_path = self.config.get("world", "")

        if folder:
            self.plugin_mgr = PluginManager(os.path.join(folder, "plugins"))
        if world_path:
            self.backup_mgr = BackupManager(world_path, backup_dir)

    def on_load_last_config(self):
        """啟動時載入上次設定並還原到UI"""
        self.config = self.config_mgr.load()
        self._update_managers()
        if self.config:
            self.ui.restore_config_to_ui(self.config)

    def on_save_settings(self):
        """將目前 UI 設定存檔，更新 model 層"""
        cfg = {
            "core": self.ui.ui.combo_core.currentText(),
            "core_path": self.ui.ui.edit_core_path.text(),
            "folder": self.ui.ui.edit_folder_path.text(),
            "java_path": self.ui.ui.edit_java_path.text(),
            "xms": self.ui.ui.spin_xms.value(),
            "xmx": self.ui.ui.spin_xmx.value(),
            "args": self.ui.ui.edit_args.text(),
            "port": self.ui.ui.spin_port.value(),
            "max_players": self.ui.ui.spin_max_players.value(),
            "world": self.ui.ui.edit_world_path.text(),
            "motd": self.ui.ui.edit_motd.text(),
            "backup": self.ui.ui.check_backup.isChecked(),
            "backup_interval": self.ui.ui.spin_backup_interval.value(),
            "backup_dir": self.ui.ui.edit_backup_dir.text(),
            "language": self.ui.ui.combo_language.currentText(),
            "setup_done": True,
            # RCON密碼如有可加密儲存
        }
        self.config_mgr.save(cfg)
        self.config = cfg
        self._update_managers()
        self.ui.show_message("設定已儲存", "伺服器設定已更新！")

    # ================= 伺服器啟動/停止 =================

    def on_start_server(self):
        """啟動 Minecraft 伺服器流程"""
        if self.server_running:
            self.ui.append_log("伺服器已在運行中。")
            return
        java_path = self.ui.ui.edit_java_path.text()
        jar_path = self.ui.ui.edit_core_path.text()
        xms = self.ui.ui.spin_xms.value()
        xmx = self.ui.ui.spin_xmx.value()
        args = self.ui.ui.edit_args.text()
        folder = self.ui.ui.edit_folder_path.text()

        if not (os.path.exists(folder) and os.path.isfile(jar_path) and os.path.isfile(java_path)):
            self.ui.show_message("錯誤", "請檢查 Java、核心與資料夾設定！", "error")
            return

        cmd = [
            java_path,
            f"-Xms{xms}M",
            f"-Xmx{xmx}M",
            "-jar",
            jar_path
        ] + args.split() + ["nogui"]

        try:
            self.server_process = subprocess.Popen(
                cmd,
                cwd=folder,
                stdin=subprocess.PIPE,
                stdout=subprocess.PIPE,
                stderr=subprocess.STDOUT,
                text=True,
                bufsize=1,
                shell=(platform.system() != "Windows")
            )
            self.server_running = True
            self.ui.append_log("伺服器已啟動")
            log_info(f"伺服器啟動成功: {cmd}")
            self.log_thread = threading.Thread(target=self._read_server_output, daemon=True)
            self.log_thread.start()
        except Exception as e:
            log_error(f"伺服器啟動失敗: {e}")
            notify("伺服器啟動失敗", str(e))
            self.ui.show_message("錯誤", f"伺服器啟動失敗：{e}", "error")

    def _read_server_output(self):
        """從伺服器進程讀取並顯示日誌"""
        try:
            while self.server_process.poll() is None:
                line = self.server_process.stdout.readline()
                if line:
                    self.ui.append_log(line.strip(), is_error=("ERROR" in line.upper() or "SEVERE" in line.upper()))
        except Exception as e:
            log_error(f"讀取伺服器日誌失敗: {e}")

    def on_stop_server(self):
        """停止 Minecraft 伺服器"""
        if self.server_process and self.server_process.poll() is None:
            try:
                self.server_process.stdin.write("stop\n")
                self.server_process.stdin.flush()
                self.server_process.wait(timeout=10)
                self.ui.append_log("伺服器已停止")
                log_info("伺服器已停止")
            except Exception as e:
                log_error(f"伺服器停止失敗: {e}")
                notify("伺服器停止失敗", str(e))
                self.ui.append_log(f"停止失敗：{e}", is_error=True)
                self.server_process.terminate()
            finally:
                self.server_running = False
                self.server_process = None
                self.log_thread = None
        else:
            self.ui.append_log("伺服器未啟動。")

    def on_restart_server(self):
        """重啟伺服器"""
        self.on_stop_server()
        # 等待幾秒再啟動
        threading.Timer(2.0, self.on_start_server).start()

    # =============== 插件管理 ================

    def reload_plugins_list(self):
        """刷新插件清單，並顯示在UI"""
        if not self.plugin_mgr:
            self.ui.ui.list_plugins.clear()
            self.ui.ui.list_plugins.addItem("（未設定插件資料夾）")
            return
        self.ui.ui.list_plugins.clear()
        plugins = self.plugin_mgr.list_plugins()
        if plugins:
            for jar in plugins:
                info = self.plugin_mgr.parse_plugin_info(os.path.join(self.plugin_mgr.plugins_dir, jar))
                tooltip = f"名稱: {info.get('name','')}\n版本: {info.get('version','')}\n作者: {info.get('author','')}"
                item = QListWidgetItem(jar)
                item.setToolTip(tooltip)
                self.ui.ui.list_plugins.addItem(item)
        else:
            self.ui.ui.list_plugins.addItem("（無插件）")

    def on_plugin_add(self):
        """手動選擇並安裝插件 JAR"""
        jar_path, _ = QFileDialog.getOpenFileName(self.ui, "選擇插件 Jar 檔", "", "Jar files (*.jar)")
        if not jar_path:
            return
        try:
            self.plugin_mgr.install_plugin(jar_path, overwrite=True)
            self.reload_plugins_list()
            self.ui.show_message("安裝完成", f"{os.path.basename(jar_path)} 已安裝")
        except Exception as e:
            self.ui.show_message("安裝失敗", str(e), "error")

    def on_plugin_remove(self):
        """移除選定的插件"""
        item = self.ui.ui.list_plugins.currentItem()
        if not item or "插件" in item.text():
            self.ui.show_message("請選擇", "請先點選要移除的插件。")
            return
        try:
            self.plugin_mgr.remove_plugin(item.text())
            self.reload_plugins_list()
            self.ui.show_message("移除完成", f"已移除 {item.text()}")
        except Exception as e:
            self.ui.show_message("移除失敗", str(e), "error")

    def on_plugin_reload(self):
        """插件熱重載（需 RCON/PlugMan）"""
        self.ui.show_message("未支援", "請搭配 RCON/PlugMan 控制外掛熱重載", "warn")

    def on_check_plugin_updates(self):
        """檢查插件是否有新版（模擬）"""
        updates = []
        for jar in self.plugin_mgr.list_plugins():
            info = self.plugin_mgr.parse_plugin_info(os.path.join(self.plugin_mgr.plugins_dir, jar))
            if info.get("name") and info.get("version"):
                latest = self.plugin_mgr.check_plugin_update(info["name"], info["version"])
                if latest != info["version"]:
                    updates.append(f"{info['name']}: {info['version']} → {latest}")
        if updates:
            self.ui.append_log("可更新插件：\n" + "\n".join(updates))
            self.ui.show_message("插件更新", "\n".join(updates))
        else:
            self.ui.show_message("插件更新", "所有插件均為最新版")

    # ================ 備份 ================

    def on_manual_backup(self):
        """手動備份世界資料夾"""
        if not self.backup_mgr:
            self.ui.show_message("錯誤", "請先設定世界資料夾與備份路徑", "error")
            return
        try:
            path = self.backup_mgr.create_backup()
            self.ui.append_log(f"備份完成: {os.path.basename(path)}")
            self.ui.show_message("備份完成", f"已備份 {os.path.basename(path)}")
        except Exception as e:
            log_error(f"備份失敗: {e}")
            notify("備份失敗", str(e))
            self.ui.show_message("備份失敗", str(e), "error")

    # ============= 其他UI聯動 =============

    def on_select_core_path(self):
        fname, _ = QFileDialog.getOpenFileName(self.ui, "選擇伺服器核心檔 (.jar)", "", "JAR files (*.jar);;All Files (*)")
        if fname:
            self.ui.ui.edit_core_path.setText(fname)

    def on_select_java_path(self):
        fname, _ = QFileDialog.getOpenFileName(self.ui, "選擇 Java 執行檔", "", "Java (*java*);;All Files (*)")
        if fname:
            self.ui.ui.edit_java_path.setText(fname)

    def on_select_server_folder(self):
        folder = QFileDialog.getExistingDirectory(self.ui, "選擇伺服器資料夾")
        if folder:
            self.ui.ui.edit_folder_path.setText(folder)
            self._update_managers()
            self.reload_plugins_list()

    def on_select_world_folder(self):
        folder = QFileDialog.getExistingDirectory(self.ui, "選擇世界資料夾")
        if folder:
            self.ui.ui.edit_world_path.setText(folder)
            self._update_managers()

    def on_select_backup_dir(self):
        folder = QFileDialog.getExistingDirectory(self.ui, "選擇備份儲存目錄")
        if folder:
            self.ui.ui.edit_backup_dir.setText(folder)
            self._update_managers()

    def on_tab_changed(self, idx):
        pass  # 可根據Tab index做對應切換

    def on_change_language(self, idx):
        # 可配合 utils/lang.py 動態切換語系
        pass

    def on_update_backup_timer(self):
        # 可根據UI備份間隔調整自動備份timer
        pass

    def on_validate_core_path(self):
        pass

    def on_validate_java_path(self):
        pass

    def on_validate_port(self):
        pass

    def on_preview_launch_cmd(self):
        pass

    def on_send_command(self):
        """將 UI 輸入指令寫入 server stdin"""
        if not self.server_running or not self.server_process or self.server_process.poll() is not None:
            self.ui.show_message("錯誤", "伺服器未啟動，無法發送指令。", "error")
            return
        cmd = self.ui.ui.edit_command.text().strip()
        if cmd:
            try:
                self.server_process.stdin.write(cmd + "\n")
                self.server_process.stdin.flush()
                self.ui.append_log(f"> {cmd}")
                self.ui.ui.edit_command.clear()
            except Exception as e:
                self.ui.append_log(f"指令發送失敗：{e}", is_error=True)

    def on_update_status(self):
        """定時更新系統狀態（可呼叫於timer）"""
        now = QDateTime.currentDateTime()
        self.ui.ui.label_time.setText("系統時間：" + now.toString("yyyy/MM/dd HH:mm:ss"))
        # 其他系統資訊也可加在這裡

    def on_exit(self):
        """主視窗關閉前自動釋放資源"""
        if self.server_running:
            self.on_stop_server()
        # 停用各種timer或監控
