import discord
from discord.ext import commands, tasks
from datetime import datetime

from config import GUILD_ID
from src.database.database_manager import DatabaseManager

class Maintenance(commands.Cog):
    def __init__(self, bot):
        self.bot = bot
        self.upd_last_uuid = None
        self.update.start()

    def cog_unload(self):
        self.update.cancel()

    @tasks.loop(minutes=1)
    async def update(self):
        await self.bot.db.backup(datetime.now().strftime("%Y-%m-%d"))
        try:
            guild = self.bot.get_guild(GUILD_ID) or await self.bot.fetch_guild(GUILD_ID)
        except discord.DiscordException:
            return

        player_list_cog = self.bot.get_cog("PlayerList")
        announcements_cog = self.bot.get_cog("Announcements")

        players = await self.bot.db.get_player()
        upd_count = -next((i for i, p in enumerate(players) if p.mc_uuid == self.upd_last_uuid), 0)
        self.upd_last_uuid = None

        for player_data in players:
            upd_params = {}

            if self.upd_last_uuid is None:
                if upd_count >= 50:
                    self.upd_last_uuid = player_data.mc_uuid
                elif upd_count >= 0:
                    await player_data.update()
                    upd_params[DatabaseManager.Fields.USERNAME.value] = player_data.mc_name
                upd_count += 1

            if player_list_cog: upd_params.update(await player_list_cog.update_player_data(guild, player_data))

            if upd_params:
                await self.bot.db.update_player(DatabaseManager.Fields.UUID, player_data.mc_uuid, **upd_params)

            if announcements_cog: await announcements_cog.append_list_player(guild, player_data)
        if announcements_cog: await announcements_cog.push_list_update()

    @update.before_loop
    async def before_update(self):
        await self.bot.wait_until_ready()

async def setup(bot):
    await bot.add_cog(Maintenance(bot))