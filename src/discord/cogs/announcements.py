import discord
from discord.ext import commands
from dataclasses import asdict

from src.config import GUILD_ID, GUILD_ORG_CHANNEL_ID, ALLY_CHANNEL_ID, NEUTRAL_CHANNEL_ID, TARGET_CHANNEL_ID, ORDERED_ROLES_IDS
from src.database.player_handler import Player

class Announcements(commands.Cog):
    GUILD_ORG = "Selectium"
    NO_SUBGROUP = "OTROS / OTHERS"
    LIST_DISP = {GUILD_ORG:{"title":"# SELECTIUM PLAYER LIST <:Selectium:1345924666526793848>", "ch_id":GUILD_ORG_CHANNEL_ID, "data_name":"guild_org_msg_id"},
                 Player.Category.ALLY:{"title":"# ALLY LIST <:Cubecraft:1468618358982443111>", "ch_id":ALLY_CHANNEL_ID, "data_name":"ally_msg_id"},
                 Player.Category.NEUTRAL:{"title":"# NEUTRAL LIST 🤚🏻", "ch_id":NEUTRAL_CHANNEL_ID, "data_name":"neutral_msg_id"},
                 Player.Category.TARGET:{"title":"# TARGET LIST ⚔️", "ch_id":TARGET_CHANNEL_ID, "data_name":"target_msg_id"}}

    def __init__(self, bot):
        self.bot = bot
        self.player_list = {}

    async def append_list_player(self, player_data: Player):
        if player_data.org == self.GUILD_ORG:
            player_group = self.GUILD_ORG
            player_subgroup = self.NO_SUBGROUP

            try:
                guild = self.bot.get_guild(GUILD_ID) or await self.bot.fetch_guild(GUILD_ID)
                if player_data.dc_id is not None:
                        dc_member = guild.get_member(player_data.dc_id) or await guild.fetch_member(player_data.dc_id)

                        if highest_role := next((r for role_id in reversed(ORDERED_ROLES_IDS)
                                                 if (r := dc_member.get_role(role_id))), None):
                            player_subgroup = highest_role.name.upper()

                if player_group not in self.player_list:
                    self.player_list[player_group] = {(guild.get_role(role_id) or
                                                       await guild.fetch_role(role_id)).name.upper():{None:[]}
                                                      for role_id in reversed(ORDERED_ROLES_IDS)}
            except discord.DiscordException:
                pass
        else:
            player_group = player_data.cat
            player_subgroup = player_data.org or self.NO_SUBGROUP

        group_dict = self.player_list.setdefault(player_group, {})
        subgroup_dict = group_dict.setdefault(player_subgroup, {None:[]})
        alias_list = subgroup_dict.setdefault(player_data.alias, [])

        alias_list.append(player_data.mc_name)

    async def push_list_update(self):
        guild_config = asdict(await self.bot.db.get_server_data())

        for group, subgroups in self.player_list.items():
            if (disp_config := self.LIST_DISP.get(group)) is None: continue

            if self.NO_SUBGROUP in subgroups: subgroups[self.NO_SUBGROUP] = subgroups.pop(self.NO_SUBGROUP)
            list_str = (disp_config["title"] + "\n" +
                        "".join(f"## {subgroup}\n" +
                                " ".join(f"`{alias} = {' / '.join(members)}`" if alias
                                         else " ".join(f"`{m}`" for m in members)
                                         for alias, members in aliases.items()) + "\n\n"
                                for subgroup, aliases in subgroups.items()))

            try:
                channel = self.bot.get_channel(disp_config["ch_id"]) or await self.bot.fetch_channel(disp_config["ch_id"])
                if guild_config[disp_config["data_name"]]:
                    await (channel.get_partial_message(guild_config[disp_config["data_name"]])).edit(content=list_str)
                    continue
            except discord.NotFound:
                pass
            except discord.HTTPException:
                continue

            try:
                guild_config[disp_config["data_name"]] = (await channel.send(list_str)).id
            except discord.DiscordException:
                pass

        self.player_list.clear()
        await self.bot.db.insert_server_data(**guild_config)

async def setup(bot):
    await bot.add_cog(Announcements(bot))