import discord
from discord.ext import commands
import logging
from dotenv import load_dotenv
import os

from database_manager import DatabaseManager
from player_obj import Player

load_dotenv()
TOKEN = os.getenv("DISCORD_TOKEN")
GUILD_ID = int(os.getenv("GUILD_ID"))
# access_id = tuple(map(int, os.getenv("ACCESS_ROLE_IDS").split(",")))

handler = logging.FileHandler(filename="discord.log", encoding="utf-8", mode="w")

# def uuid_username_sep(name_or_uuid: str):
#     try:
#         uuid = UUID(name_or_uuid)
#         username = None
#     except ValueError:
#         uuid = None
#         username = name_or_uuid
#     return uuid, username

class SelectiumManager(commands.Bot):
    def __init__(self):
        intents = discord.Intents.default()
        super().__init__(command_prefix="!", intents=intents)

        self.db = DatabaseManager()

    async def on_ready(self):
        print(f"Logged in as {self.user} (ID: {self.user.id})")
        print("Selectium Bot Ready")

    async def setup_hook(self):
        await Player.connect()
        await self.db.connect()
        await self.load_extension("cogs.player_list")
        await self.load_extension("cogs.announcements")
        await self.tree.sync(guild=discord.Object(GUILD_ID))

    async def close(self):
        await Player.close()
        await self.db.close()
        await super().close()

if __name__ == "__main__":
    bot = SelectiumManager()
    bot.run(TOKEN, reconnect=True,log_handler=handler, log_level=logging.DEBUG)