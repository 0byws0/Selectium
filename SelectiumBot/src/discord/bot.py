import discord
from discord import app_commands
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

    async def command_error(self, interaction, error):
        if isinstance(error, app_commands.errors.MissingAnyRole):
            await interaction.response.send_message("Missing required roles for this command")
            return

        raise error

    async def setup_hook(self):
        self.tree.on_error = self.command_error

        await self.load_extension("src.discord.cogs.player_list")
        await self.load_extension("src.discord.cogs.announcements")
        await self.tree.sync(guild=discord.Object(GUILD_ID))

    async def close(self):
        print("Selectium Bot Disconnecting")
        await super().close()