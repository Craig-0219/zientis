import os
import platform
import subprocess
import threading
import psutil  # pip install psutil

from model.config import ConfigManager
from model.plugin_manager import PluginManager
from model.backup_manager import BackupManager
from model.player_role_manager import PlayerRoleManager
from model.player import Player
from model.rcon_manager import RconManager
from utils.logger import log_info, log_error
from utils.notification import notify

from PySide6.QtCore import QTimer, QThread, Signal, QDateTime
from PySide6.QtWidgets import QFileDialog, QTableWidgetItem

ROLE_PRIORITY = {"服主": 0, "管理員": 1, "VIP": 2, "玩家": 3}

class PlayerSyncWorker(QThread):
    player_data_ready = Signal(list)  # list[Player]

    def __init__(self, rcon_mgr, role_mgr):
        super().__init__()
        self.rcon_mgr = rcon_mgr
        self.role_mgr = role_mgr

    def run(self):
        try:
            players = self.rcon_mgr.get_online_players()
            self.role_mgr.sync_roles_from_server(players)
            player_objs = []
            for name in players:
                role = self.role_mgr.get_role(name)
                player_objs.append(Player(name, role))
            player_objs.sort(key=lambda p: (ROLE_PRIORITY.get(p.role, 99), p.name.lower()))
            self.player_data_ready.emit(player_objs)
        except Exception as e:
            log_error(f"玩家名單同步失敗: {e}")
            self.player_data_ready.emit([])

class ServerController:
    def __init__(self, ui):
        self.ui = ui  # MainWindow
        self.config_mgr = ConfigManager()
        self.server_process = None
        self.server_running = False
        self.log_thread = None

        self.config = self.config_mgr.load()
        self.plugin_mgr = None
        self.backup_mgr = None
        self.role_mgr = None
        self.rcon_mgr = None

        self.player_timer = QTimer()
        self.player_timer.timeout.connect(self.update_player_list)
        self.player_worker = None

        # 狀態列定時器（只用主執行緒！）
        self.status_timer = QTimer(self.ui)
        self.status_timer.timeout.connect(self.on_update_status)
        self.status_timer.start(1000)

        self._update_managers()

    def _update_managers(self):
        folder = self.config.get("folder", "")
        backup_dir = self.config.get("backup_dir", os.path.join(os.getcwd(), "backups"))
        world_path = self.config.get("world", "")

        self.plugin_mgr = PluginManager(os.path.join(folder, "plugins")) if folder else None
        self.backup_mgr = BackupManager(world_path, backup_dir) if world_path else None

        rcon_host = self.config.get("rcon_host", "127.0.0.1")
        rcon_port = int(self.config.get("rcon_port", 25575))
        rcon_pass = self.config.get("rcon_pass", "")
        self.role_mgr = PlayerRoleManager()
        self.rcon_mgr = RconManager(rcon_host, rcon_port, rcon_pass)

    def on_load_last_config(self):
        self.config = self.config_mgr.load()
        self._update_managers()
        if self.config:
            self.ui.restore_config_to_ui(self.config)

    def on_save_settings(self):
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
            "rcon_host": self.ui.ui.edit_rcon_host.text() if hasattr(self.ui.ui, "edit_rcon_host") else "127.0.0.1",
            "rcon_port": self.ui.ui.spin_rcon_port.value() if hasattr(self.ui.ui, "spin_rcon_port") else 25575,
            "rcon_pass": self.ui.ui.edit_rcon_pass.text() if hasattr(self.ui.ui, "edit_rcon_pass") else "",
        }
        self.config_mgr.save(cfg)
        self.config = cfg
        self._update_managers()
        self.ui.show_message("設定已儲存", "伺服器設定已更新！")

    # ================= 伺服器啟動/停止 =================
    def on_start_server(self):
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
            self.player_timer.start(3000)
            self.server_running = True
            threading.Timer(3.0, self.update_plugman_status).start()
        except Exception as e:
            log_error(f"伺服器啟動失敗: {e}")
            notify("伺服器啟動失敗", str(e))
            self.ui.show_message("錯誤", f"伺服器啟動失敗：{e}", "error")

    def _read_server_output(self):
        try:
            while self.server_process.poll() is None:
                line = self.server_process.stdout.readline()
                if line:
                    self.ui.append_log(line.strip(), is_error=("ERROR" in line.upper() or "SEVERE" in line.upper()))
        except Exception as e:
            log_error(f"讀取伺服器日誌失敗: {e}")

    def on_stop_server(self):
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
                self.player_timer.stop()
        else:
            self.ui.append_log("伺服器未啟動。")

    def on_restart_server(self):
        """重啟伺服器（用單執行緒安全的 QTimer 方式）"""
        # 先停止
        self.on_stop_server()
        # 2 秒後檢查設定再啟動（避免直接 crash）
        def try_start():
            # 跟 on_start_server 同步，檢查資料夾與檔案是否存在
            java_path = self.ui.ui.edit_java_path.text()
            jar_path = self.ui.ui.edit_core_path.text()
            folder = self.ui.ui.edit_folder_path.text()
            if not (os.path.exists(folder) and os.path.isfile(jar_path) and os.path.isfile(java_path)):
                self.ui.show_message("錯誤", "請檢查 Java、核心與資料夾設定！", "error")
                return
            self.on_start_server()
        QTimer.singleShot(2000, try_start)

    # =============== 玩家名單/權限管理 ===============
    def update_player_list(self):
        if not self.rcon_mgr or not self.role_mgr:
            self.ui.show_player_list([])
            return
        self.player_worker = PlayerSyncWorker(self.rcon_mgr, self.role_mgr)
        self.player_worker.player_data_ready.connect(self.ui.show_player_list)
        self.player_worker.start()

    # =============== 插件管理（QTableWidget） ===============
    def reload_plugins_list(self):
        table = self.ui.ui.table_plugins
        table.setRowCount(0)
        if not self.plugin_mgr:
            table.setRowCount(1)
            table.setItem(0, 0, QTableWidgetItem("（未設定插件資料夾）"))
            self.update_plugman_status()
            return

        plugins = self.plugin_mgr.list_plugins()
        if not plugins:
            table.setRowCount(1)
            table.setItem(0, 0, QTableWidgetItem("（無插件）"))
            self.update_plugman_status()
            return

        table.setRowCount(len(plugins))
        for row, jar in enumerate(plugins):
            info = self.plugin_mgr.parse_plugin_info(os.path.join(self.plugin_mgr.plugins_dir, jar))
            items = [
                QTableWidgetItem(jar),
                QTableWidgetItem(info.get('name', '')),
                QTableWidgetItem(info.get('version', '')),
                QTableWidgetItem(info.get('author', ''))
            ]
            for col, item in enumerate(items):
                item.setToolTip(f"名稱: {info.get('name','')}\n版本: {info.get('version','')}\n作者: {info.get('author','')}")
                table.setItem(row, col, item)
        self.update_plugman_status()

    def _get_selected_plugin_jar(self):
        table = self.ui.ui.table_plugins
        row = table.currentRow()
        if row < 0:
            return None
        item = table.item(row, 0)
        if not item:
            return None
        jar = item.text()
        if not jar.endswith(".jar"):
            return None
        return jar

    def on_plugin_add(self):
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
        jar = self._get_selected_plugin_jar()
        if not jar:
            self.ui.show_message("請選擇", "請先點選要移除的插件。")
            return
        try:
            self.plugin_mgr.remove_plugin(jar)
            self.reload_plugins_list()
            self.ui.show_message("移除完成", f"已移除 {jar}")
        except Exception as e:
            self.ui.show_message("移除失敗", str(e), "error")

    def on_plugin_reload(self):
        jar = self._get_selected_plugin_jar()
        if not jar:
            self.ui.show_message("請選擇", "請先點選要重載的插件。")
            return
        plugin_name = jar.rsplit(".", 1)[0]
        if (not self.rcon_mgr or
                not hasattr(self.rcon_mgr, "check_plugman_available") or
                not self.rcon_mgr.check_plugman_available()):
            self.ui.show_message("PlugMan未安裝", "未偵測到 PlugMan 外掛或外掛未正常運作。")
            return
        resp = self.rcon_mgr.reload_plugin(plugin_name)
        if "success" in resp.lower():
            self.ui.show_message("插件熱重載", f"{plugin_name} 已熱重載完成！")
        else:
            self.ui.show_message("熱重載失敗", f"伺服器回應：\n{resp}", "error")

    def on_plugin_enable(self):
        jar = self._get_selected_plugin_jar()
        if not jar:
            self.ui.show_message("請選擇", "請先點選要啟用的插件。")
            return
        plugin_name = jar.rsplit(".", 1)[0]
        resp = self.rcon_mgr.enable_plugin(plugin_name)
        self.ui.show_message("啟用結果", resp)

    def on_plugin_disable(self):
        jar = self._get_selected_plugin_jar()
        if not jar:
            self.ui.show_message("請選擇", "請先點選要停用的插件。")
            return
        plugin_name = jar.rsplit(".", 1)[0]
        resp = self.rcon_mgr.disable_plugin(plugin_name)
        self.ui.show_message("停用結果", resp)

    def update_plugman_status(self):
        available = False
        if self.rcon_mgr and hasattr(self.rcon_mgr, "check_plugman_available"):
            available = self.rcon_mgr.check_plugman_available()
        self.ui.set_plugman_status(available)

    def on_check_plugin_updates(self):
        self.ui.show_message("尚未實作", "插件更新查詢功能暫未開放", "warn")

    # =============== 備份管理 ===============
    def on_manual_backup(self):
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
        pass

    def on_change_language(self, idx):
        pass

    def on_update_backup_timer(self):
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
        from psutil import cpu_percent, virtual_memory
        now = QDateTime.currentDateTime()
        cpu = cpu_percent()
        ram = virtual_memory().percent
        # 用 self.ui.label_time 等更新
        self.ui.label_time.setText(f"系統時間：{now.toString('yyyy/MM/dd HH:mm:ss')}")
        self.ui.label_cpu.setText(f"CPU：{cpu}%")
        self.ui.label_ram.setText(f"RAM：{ram}%")

    def on_exit(self):
        if self.server_running:
            self.on_stop_server()
        self.player_timer.stop()
        self.status_timer.stop()
