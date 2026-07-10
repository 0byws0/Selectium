from dotenv import load_dotenv
import os
import logging
from logging.handlers import TimedRotatingFileHandler, QueueHandler, QueueListener
from queue import Queue

load_dotenv()

try:
    TOKEN = os.environ["DISCORD_TOKEN"]
    GUILD_ID = int(os.environ["GUILD_ID"])
    ALLY_CHANNEL_ID = int(os.environ["ALLY_CHANNEL_ID"])
    NEUTRAL_CHANNEL_ID = int(os.environ["NEUTRAL_CHANNEL_ID"])
    TARGET_CHANNEL_ID = int(os.environ["TARGET_CHANNEL_ID"])

    _all_roles = list(map(int, os.environ["ROLE_IDS"].split(",")))
    MOD_ROLE_IDS = _all_roles[_all_roles.index(int(os.environ["MIN_MOD_ROLE_ID"])):]
    BOT_ROLE_IDS = _all_roles[_all_roles.index(int(os.environ["MIN_BOT_ROLE_ID"])):]
except (KeyError, ValueError) as e:
    raise RuntimeError(f"Environment file misconfigured: {e}")

file_handler = TimedRotatingFileHandler("logs/app.log", when="midnight", interval=1, backupCount=7, encoding="utf-8")
file_handler.suffix = "%Y-%m-%d"
file_handler.setFormatter(logging.Formatter("%(asctime)s | %(name)s | %(levelname)s | %(message)s"))

log_queue = Queue(-1)
queue_handler = QueueHandler(log_queue)
log_listener = QueueListener(log_queue, file_handler)

discord_logger = logging.getLogger("discord")
discord_logger.setLevel(logging.INFO)
discord_logger.addHandler(queue_handler)
discord_logger.propagate = False

for logger_name in ("uvicorn.error", "uvicorn.access"):
    uvicorn_logger = logging.getLogger(logger_name)
    uvicorn_logger.setLevel(logging.INFO)
    uvicorn_logger.addHandler(queue_handler)