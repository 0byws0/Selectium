import discord
from discord.ext import commands

from src.config import GUILD_ID

class SelectiumManager(commands.Bot):
    def __init__(self, db, conn_manager):
        intents = discord.Intents.default()
        intents.members = True
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
        print("Selectium Bot Disconnecting")
        await super().close()