import sys
from PySide6.QtWidgets import QApplication
from ui.launcher_ui import ZientisLauncherUI

def my_excepthook(exctype, value, traceback):
    print("UNCAUGHT EXCEPTION", exctype, value)
    import traceback as tb; tb.print_exception(exctype, value, traceback)
sys.excepthook = my_excepthook

if __name__ == "__main__":
    app = QApplication(sys.argv)
    window = ZientisLauncherUI()
    window.show()
    sys.exit(app.exec())
