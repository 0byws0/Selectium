import discord
from discord.ext import commands
import os

from database_manager import DatabaseManager
from player_obj import Player

GUILD_ID = int(os.getenv("GUILD_ID"))
ALLY_CHANNEL_ID = int(os.getenv("ALLY_CHANNEL_ID"))
NEUTRAL_CHANNEL_ID = int(os.getenv("NEUTRAL_CHANNEL_ID"))
TARGET_CHANNEL_ID = int(os.getenv("TARGET_CHANNEL_ID"))

class Announcements(commands.Cog):
    def __init__(self, bot):
        self.bot = bot

    async def update_list_channels(self):
        NO_ORG = "OTROS / OTHERS"
        id_tuple = await self.bot.db.get_list_id(GUILD_ID) or (None, None, None)

        lists_disp = [
            {"ch":ALLY_CHANNEL_ID, "msg":id_tuple[0], "cat":Player.Category.ALLY, "text":"# ALLY LIST :Cubecraft:\n"},
            {"ch":NEUTRAL_CHANNEL_ID, "msg":id_tuple[1], "cat":Player.Category.NEUTRAL, "text":"# NEUTRAL LIST 🤚🏻\n"},
            {"ch":TARGET_CHANNEL_ID, "msg":id_tuple[2], "cat":Player.Category.TARGET, "text":"# TARGET LIST ⚔️\n"}
        ]

        for list_info in lists_disp:
            orgs = {}
            players = await self.bot.db.get_player(DatabaseManager.Fields.CATEGORY, list_info["cat"])
            for player_data in players:
                orgs[player_data.org or NO_ORG] = (orgs.get(player_data.org or NO_ORG, "") +
                                                   "`" + player_data.mc_name +
                                                   (" / " + player_data.alias if player_data.alias else "") + "` ")


            if NO_ORG in orgs: orgs[NO_ORG] = orgs.pop(NO_ORG)
            for org, members in orgs.items():
                list_info["text"] += f"## {org}\n{members}\n\n"

            channel = self.bot.get_channel(list_info["ch"]) or await self.bot.fetch_channel(list_info["ch"])

            if list_info["msg"]:
                try:
                    await (await channel.fetch_message(list_info["msg"])).edit(content=list_info["text"])
                except discord.NotFound:
                    list_info["msg"] = (await channel.send(list_info["text"])).id
            else:
                list_info["msg"] = (await channel.send(list_info["text"])).id

        await self.bot.db.insert_list_id(GUILD_ID, ally_msg_id=(lists_disp[0]["msg"]),
                                         neutral_msg_id=(lists_disp[1]["msg"]),
                                         target_msg_id=(lists_disp[2]["msg"]))

async def setup(bot):
    await bot.add_cog(Announcements(bot))