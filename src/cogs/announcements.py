import discord
from discord.ext import commands

from src.config import GUILD_ID, ALLY_CHANNEL_ID, NEUTRAL_CHANNEL_ID, TARGET_CHANNEL_ID
from src.player_handler import Player

class Announcements(commands.Cog):
    NO_ORG = "OTROS / OTHERS"

    LIST_DISP = {Player.Category.ALLY:{"title":"# ALLY LIST :Cubecraft:", "ch_id":ALLY_CHANNEL_ID, "msg_idx":0},
                 Player.Category.NEUTRAL:{"title":"# NEUTRAL LIST 🤚🏻", "ch_id":NEUTRAL_CHANNEL_ID, "msg_idx":1},
                 Player.Category.TARGET:{"title":"# TARGET LIST ⚔️", "ch_id":TARGET_CHANNEL_ID, "msg_idx":2}}

    def __init__(self, bot):
        self.bot = bot
        self.player_list = {}

    async def append_list_player(self, player_data: Player):
        if player_data.cat not in self.player_list: self.player_list[player_data.cat] = {}

        self.player_list[player_data.cat][player_data.org or self.NO_ORG] = \
            (self.player_list[player_data.cat].get(player_data.org or self.NO_ORG, "") +
             "`" + player_data.mc_name + (" / " + player_data.alias if player_data.alias else "") + "` ")

    async def push_list_update(self):
        db_msg_ids = list(await self.bot.db.get_list_id(GUILD_ID) or (None, None, None))

        for cat, orgs in self.player_list.items():
            if (disp_config := self.LIST_DISP.get(cat)) is None: continue

            list_str = disp_config["title"] + "\n"

            if self.NO_ORG in orgs: orgs[self.NO_ORG] = orgs.pop(self.NO_ORG)
            for org, members in orgs.items():
                list_str += f"## {org}\n{members}\n\n"

            channel = self.bot.get_channel(disp_config["ch_id"]) or await self.bot.fetch_channel(disp_config["ch_id"])

            if db_msg_ids[disp_config["msg_idx"]]:
                try:
                    await (channel.get_partial_message(db_msg_ids[disp_config["msg_idx"]])).edit(content=list_str)
                except discord.NotFound:
                    db_msg_ids[disp_config["msg_idx"]] = (await channel.send(list_str)).id
            else:
                db_msg_ids[disp_config["msg_idx"]] = (await channel.send(list_str)).id

        self.player_list.clear()
        await self.bot.db.insert_list_id(GUILD_ID, *db_msg_ids)

async def setup(bot):
    await bot.add_cog(Announcements(bot))