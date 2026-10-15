from dotenv import load_dotenv
import os
import logging
from logging.handlers import TimedRotatingFileHandler, QueueHandler, QueueListener
from queue import Queue

load_dotenv()

try:
    TOKEN = os.environ["DISCORD_TOKEN"]
    GUILD_ID = int(os.environ["GUILD_ID"])
    GUILD_CHANNEL_ID = int(os.environ["GUILD_CHANNEL_ID"])
    LEADERBOARD_CHANNEL_ID = int(os.environ["LEADERBOARD_CHANNEL_ID"])
    ALLY_CHANNEL_ID = int(os.environ["ALLY_CHANNEL_ID"])
    NEUTRAL_CHANNEL_ID = int(os.environ["NEUTRAL_CHANNEL_ID"])
    TARGET_CHANNEL_ID = int(os.environ["TARGET_CHANNEL_ID"])
    ORDERED_ROLES_IDS = list(map(int, os.environ["ORDERED_ROLE_IDS"].split(",")))
    MOD_ROLE_IDS = ORDERED_ROLES_IDS[ORDERED_ROLES_IDS.index(int(os.environ["MIN_MOD_ROLE_ID"])):]
    BOT_ROLE_IDS = ORDERED_ROLES_IDS[ORDERED_ROLES_IDS.index(int(os.environ["MIN_BOT_ROLE_ID"])):]
except (KeyError, ValueError) as e:
    raise RuntimeError(f"Environment file misconfigured: {e}")

_file_handler = TimedRotatingFileHandler("logs/app.log", when="midnight", interval=1, backupCount=7, encoding="utf-8")
_file_handler.suffix = "%Y-%m-%d"
_file_handler.setFormatter(logging.Formatter("%(asctime)s | %(name)s | %(levelname)s | %(message)s"))

_log_queue = Queue(-1)
_queue_handler = QueueHandler(_log_queue)
log_listener = QueueListener(_log_queue, _file_handler)

_discord_logger = logging.getLogger("discord")
_discord_logger.setLevel(logging.INFO)
_discord_logger.addHandler(_queue_handler)
_discord_logger.propagate = False

for logger_name in ("uvicorn.error", "uvicorn.access"):
    _uvicorn_logger = logging.getLogger(logger_name)
    _uvicorn_logger.setLevel(logging.INFO)
    _uvicorn_logger.addHandler(_queue_handler)