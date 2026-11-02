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
        match error:
            case app_commands.errors.MissingAnyRole():
                error_msg = "Missing required roles for this command"
            case app_commands.errors.MissingPermissions():
                error_msg = "Missing permissions for this command"
            case _:
                raise error

        if interaction.response.is_done():
            await interaction.followup.send(error_msg, ephemeral=True)
        else:
            await interaction.response.send_message(error_msg, ephemeral=True)

    async def setup_hook(self):
        self.tree.on_error = self.command_error

        await self.load_extension("src.discord.cogs.player_list")
        await self.load_extension("src.discord.cogs.announcements")
        await self.tree.sync(guild=discord.Object(GUILD_ID))

    async def close(self):
        print("Selectium Bot Disconnecting")
        await super().close()