import sys
from PySide6.QtWidgets import QApplication
from ui.launcher_ui import ZientisLauncherUI

if __name__ == "__main__":
    app = QApplication(sys.argv)
    window = ZientisLauncherUI()
    window.show()
    sys.exit(app.exec())
