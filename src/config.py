from dotenv import load_dotenv
import os
import logging
from logging.handlers import TimedRotatingFileHandler, QueueHandler, QueueListener
from queue import Queue

load_dotenv()

try:
    TOKEN = os.environ["DISCORD_TOKEN"]
    GUILD_ID = int(os.environ["GUILD_ID"])
    ACCESS_ROLE_ID = int(os.environ["ACCESS_ROLE_ID"])
    # ACCESS_ID = tuple(map(int, os.getenv("ACCESS_ROLE_IDS").split(",")))
    ALLY_CHANNEL_ID = int(os.environ["ALLY_CHANNEL_ID"])
    NEUTRAL_CHANNEL_ID = int(os.environ["NEUTRAL_CHANNEL_ID"])
    TARGET_CHANNEL_ID = int(os.environ["TARGET_CHANNEL_ID"])
except (KeyError, ValueError):
    raise RuntimeError("Environment file misconfigured")

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