# ui/launcher_ui.py
"""
ZientisLauncherUI
負責：UI初始化、事件繫結、UI狀態顯示、錯誤提示、樣式設置
不負責：任何伺服器啟動、設定檔存取、插件安裝備份、伺服器狀態查詢等業務邏輯
"""

import os
from PySide6.QtWidgets import (
    QMainWindow, QFileDialog, QMessageBox, QListWidgetItem, QInputDialog, QShortcut
)
from PySide6.QtUiTools import QUiLoader
from PySide6.QtCore import QFile, Qt, QTimer, Signal, QObject, QDateTime
from PySide6.QtGui import QColor, QTextCharFormat

from controller.server_controller import ServerController

class ZientisLauncherUI(QMainWindow):
    """Zientis GUI主視窗，僅負責UI與事件"""
    def __init__(self):
        super().__init__()
        # ========== 載入UI ==========
        loader = QUiLoader()
        ui_file = QFile("mainwindow.ui")
        if not ui_file.open(QFile.ReadOnly):
            QMessageBox.critical(self, "錯誤", "無法載入UI檔案")
            os._exit(1)
        self.ui = loader.load(ui_file, self)
        ui_file.close()
        self.setCentralWidget(self.ui.centralwidget)

        # ========== 控制層 ========== 
        self.controller = ServerController(self)

        # ========== UI初始化 ==========
        self.setup_ui()
        self.setup_shortcuts()
        self.set_custom_style()

    def setup_ui(self):
        """初始化UI元件、事件繫結"""
        # 指令輸入與發送
        self.ui.edit_command.returnPressed.connect(self.controller.on_send_command)
        self.ui.btn_send_command.clicked.connect(self.controller.on_send_command)

        # 啟動、停止、重啟按鈕
        self.ui.btn_start.clicked.connect(self.controller.on_start_server)
        self.ui.btn_stop.clicked.connect(self.controller.on_stop_server)
        self.ui.btn_restart.clicked.connect(self.controller.on_restart_server)

        # 選擇路徑
        self.ui.btn_core_path.clicked.connect(self.controller.on_select_core_path)
        self.ui.btn_java.clicked.connect(self.controller.on_select_java_path)
        self.ui.btn_folder.clicked.connect(self.controller.on_select_server_folder)
        self.ui.btn_world.clicked.connect(self.controller.on_select_world_folder)
        self.ui.btn_backup_dir.clicked.connect(self.controller.on_select_backup_dir)

        # 插件管理
        self.ui.btn_plugin_add.clicked.connect(self.controller.on_plugin_add)
        self.ui.btn_plugin_remove.clicked.connect(self.controller.on_plugin_remove)
        self.ui.btn_plugin_reload.clicked.connect(self.controller.on_plugin_reload)
        self.ui.btn_plugin_check_updates.clicked.connect(self.controller.on_check_plugin_updates)

        # 備份
        self.ui.btn_backup.clicked.connect(self.controller.on_manual_backup)
        self.ui.btn_restore.clicked.connect(self.controller.on_restore_backup)

        # 設定
        self.ui.btn_save.clicked.connect(self.controller.on_save_settings)

        # 標籤切換
        self.ui.tabWidget.currentChanged.connect(self.controller.on_tab_changed)

        # 配置變更
        self.ui.combo_language.currentIndexChanged.connect(self.controller.on_change_language)
        self.ui.spin_backup_interval.valueChanged.connect(self.controller.on_update_backup_timer)
        self.ui.edit_core_path.textChanged.connect(self.controller.on_validate_core_path)
        self.ui.edit_java_path.textChanged.connect(self.controller.on_validate_java_path)
        self.ui.spin_port.valueChanged.connect(self.controller.on_validate_port)
        self.ui.edit_args.textChanged.connect(self.controller.on_preview_launch_cmd)
        self.ui.spin_xms.valueChanged.connect(self.controller.on_preview_launch_cmd)
        self.ui.spin_xmx.valueChanged.connect(self.controller.on_preview_launch_cmd)

        # 初始化狀態
        self.controller.on_load_last_config()
        self.controller.reload_plugins_list()

        # 狀態更新計時器
        self.status_timer = QTimer()
        self.status_timer.timeout.connect(self.controller.on_update_status)
        self.status_timer.start(1000)

    def setup_shortcuts(self):
        """設定快捷鍵"""
        QShortcut(Qt.CTRL | Qt.Key_H, self)
        reload_shortcut = QShortcut(Qt.Key_F5, self)
        reload_shortcut.activated.connect(self.controller.reload_plugins_list)
        esc_shortcut = QShortcut(Qt.Key_Escape, self)
        esc_shortcut.activated.connect(lambda: self.ui.edit_command.setFocus())

    def set_custom_style(self):
        """自定義美術風格"""
        self.setStyleSheet("""
        QMainWindow {background: #1b1e24;}
        QLabel, QCheckBox, QLineEdit, QSpinBox, QComboBox, QListWidget, QTextEdit, QProgressBar {
            color: #c0ffe0; font-family: '標楷體'; font-size: 13px;
        }
        QPushButton {
            background: #2f353e; border-radius: 8px; padding: 6px 16px;
            color: #b9ffcc; font-family: '標楷體'; font-size: 13px; border: 1px solid #38d982;
        }
        QPushButton:hover { background: #1b2c1b; color: #38d982;}
        QTabBar::tab {background:#222; color:#c0ffe0; border-radius:6px; min-width:90px; min-height:28px;}
        QTabBar::tab:selected {background:#38d982; color:#121;}
        QProgressBar {
            background: #333;
            border-radius: 8px;
            text-align: center;
            height: 14px;
        }
        QProgressBar::chunk { background: qlineargradient(x1:0, y1:0, x2:1, y2:0, stop:0 #38d982, stop:1 #217c4a);}
        QTextEdit, QLineEdit, QListWidget {background: #242a34; border: 1px solid #38d982; border-radius: 6px;}
        """)

    # ============ UI幫助函式區 ============
    def restore_config_to_ui(self, cfg: dict):
        """將設定資料應用至UI各欄位（controller會呼叫）"""
        try:
            self.ui.combo_core.setCurrentText(cfg.get("core", "Paper"))
            self.ui.edit_core_path.setText(cfg.get("core_path", ""))
            self.ui.edit_folder_path.setText(cfg.get("folder", ""))
            self.ui.edit_java_path.setText(cfg.get("java_path", ""))
            self.ui.spin_xms.setValue(cfg.get("xms", 1024))
            self.ui.spin_xmx.setValue(cfg.get("xmx", 2048))
            self.ui.edit_args.setText(cfg.get("args", ""))
            self.ui.spin_port.setValue(cfg.get("port", 25565))
            self.ui.spin_max_players.setValue(cfg.get("max_players", 20))
            self.ui.edit_world_path.setText(cfg.get("world", ""))
            self.ui.edit_motd.setText(cfg.get("motd", ""))
            self.ui.check_backup.setChecked(cfg.get("backup", False))
            self.ui.spin_backup_interval.setValue(cfg.get("backup_interval", 0))
            self.ui.combo_language.setCurrentText(cfg.get("language", "繁體中文"))
            self.ui.edit_backup_dir.setText(cfg.get("backup_dir", "backups"))
        except Exception as e:
            QMessageBox.warning(self, "設定還原錯誤", str(e))

    def show_message(self, title, msg, level="info"):
        """統一UI訊息提示"""
        if level == "warn":
            QMessageBox.warning(self, title, msg)
        elif level == "error":
            QMessageBox.critical(self, title, msg)
        else:
            QMessageBox.information(self, title, msg)

    def append_log(self, text, is_error=False):
        """日誌顯示區，支援錯誤高亮"""
        cursor = self.ui.text_log.textCursor()
        if is_error:
            format = QTextCharFormat()
            format.setForeground(QColor("#ff5555"))
            cursor.setCharFormat(format)
        self.ui.text_log.append(text)

    # ============ 重要事件處理 =============
    def closeEvent(self, event):
        """視窗關閉時，通知controller清理"""
        self.controller.on_exit()
        event.accept()

