from plyer import notification

def notify(title: str, message: str, app_name: str = "Zientis", timeout: int = 5):
    notification.notify(
        title=title,
        message=message,
        app_name=app_name,
        timeout=timeout
    )
