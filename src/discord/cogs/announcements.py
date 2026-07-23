import discord
from discord.ext import commands

from src.config import GUILD_ID, GUILD_ORG_CHANNEL_ID, ALLY_CHANNEL_ID, NEUTRAL_CHANNEL_ID, TARGET_CHANNEL_ID, ORDERED_ROLES_IDS
from src.database.player_handler import Player

class Announcements(commands.Cog):
    GUILD_ORG = "Selectium"
    NO_SUBGROUP = "OTROS / OTHERS"
    LIST_DISP = {GUILD_ORG:{"title":"# SELECTIUM <:Selectium:1345924666526793848>", "ch_id":GUILD_ORG_CHANNEL_ID, "msg_idx":0},
                 Player.Category.ALLY:{"title":"# ALLY LIST <:Cubecraft:1468618358982443111>", "ch_id":ALLY_CHANNEL_ID, "msg_idx":1},
                 Player.Category.NEUTRAL:{"title":"# NEUTRAL LIST 🤚🏻", "ch_id":NEUTRAL_CHANNEL_ID, "msg_idx":2},
                 Player.Category.TARGET:{"title":"# TARGET LIST ⚔️", "ch_id":TARGET_CHANNEL_ID, "msg_idx":3}}

    def __init__(self, bot):
        self.bot = bot
        self.player_list = {}

    async def append_list_player(self, player_data: Player):
        if player_data.org == self.GUILD_ORG:
            player_group = self.GUILD_ORG
            player_subgroup = self.NO_SUBGROUP
            guild = self.bot.get_guild(GUILD_ID)

            if player_data.dc_id is not None:
                try:
                    dc_member = guild.get_member(player_data.dc_id) or await guild.fetch_member(player_data.dc_id)

                    if highest_role := next((r for role_id in reversed(ORDERED_ROLES_IDS) if (r := dc_member.get_role(role_id))), None):
                        player_subgroup = highest_role.name.upper()
                except discord.DiscordException:
                    pass

            if player_group not in self.player_list:
                self.player_list[player_group] = {(guild.get_role(role_id) or await guild.fetch_role(role_id)).name.upper():""
                                                  for role_id in reversed(ORDERED_ROLES_IDS)}
        else:
            player_group = player_data.cat
            player_subgroup = player_data.org or self.NO_SUBGROUP
            if player_group not in self.player_list: self.player_list[player_group] = {}
        
        self.player_list[player_group][player_subgroup] = (self.player_list[player_group].get(player_subgroup, "") +
            "`" + player_data.mc_name + (" / " + player_data.alias if player_data.alias else "") + "` ")

    async def push_list_update(self):
        db_msg_ids = list(await self.bot.db.get_server_data() or (None, None, None))

        for group, subgroups in self.player_list.items():
            if (disp_config := self.LIST_DISP.get(group)) is None: continue

            list_str = disp_config["title"] + "\n"

            if self.NO_SUBGROUP in subgroups: subgroups[self.NO_SUBGROUP] = subgroups.pop(self.NO_SUBGROUP)
            for subgroup, members in subgroups.items():
                list_str += f"## {subgroup}\n{members}\n\n"

            channel = self.bot.get_channel(disp_config["ch_id"]) or await self.bot.fetch_channel(disp_config["ch_id"])

            if db_msg_ids[disp_config["msg_idx"]]:
                try:
                    await (channel.get_partial_message(db_msg_ids[disp_config["msg_idx"]])).edit(content=list_str)
                except discord.NotFound:
                    db_msg_ids[disp_config["msg_idx"]] = (await channel.send(list_str)).id
            else:
                db_msg_ids[disp_config["msg_idx"]] = (await channel.send(list_str)).id

        self.player_list.clear()
        await self.bot.db.insert_server_data(*db_msg_ids)

async def setup(bot):
    await bot.add_cog(Announcements(bot))