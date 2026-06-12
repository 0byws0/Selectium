from dotenv import load_dotenv
import os

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
