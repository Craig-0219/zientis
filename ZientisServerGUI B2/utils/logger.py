import logging

def setup_logger(log_file: str = "server_launcher.log"):
    logging.basicConfig(
        filename=log_file,
        level=logging.INFO,
        format='%(asctime)s - %(levelname)s - %(message)s'
    )

def log_info(msg: str):
    logging.info(msg)

def log_error(msg: str):
    logging.error(msg)
