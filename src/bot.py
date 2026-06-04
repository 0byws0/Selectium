import discord
from discord.ext import commands

from src.config import GUILD_ID

# handler = logging.FileHandler(filename="discord.log", encoding="utf-8", mode="w")

class SelectiumManager(commands.Bot):
    def __init__(self, db, conn_manager):
        intents = discord.Intents.default()
        super().__init__(command_prefix="!", intents=intents)

        self.db = db
        self.conn_manager = conn_manager

    async def on_ready(self):
        print(f"Logged in as {self.user} (ID: {self.user.id})")
        print("Selectium Bot Ready")

    async def setup_hook(self):
        await self.load_extension("cogs.player_list")
        await self.load_extension("cogs.announcements")
        await self.tree.sync(guild=discord.Object(GUILD_ID))

    async def close(self):
        await super().close()

# if __name__ == "__main__":
#     bot = SelectiumManager()
#     bot.run(TOKEN, reconnect=True, log_handler=handler, log_level=logging.DEBUG)