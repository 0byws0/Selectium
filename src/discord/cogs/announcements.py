import discord
from discord.ext import commands
from dataclasses import asdict, dataclass

from src.config import GUILD_CHANNEL_ID, LEADERBOARD_CHANNEL_ID, ALLY_CHANNEL_ID, NEUTRAL_CHANNEL_ID, TARGET_CHANNEL_ID, ORDERED_ROLES_IDS
from src.database.player_handler import Player

class Announcements(commands.Cog):
    GUILD_LIST = object()
    KILL_LEADERBOARD = object()
    NO_SUBGROUP = "Others / Otros"

    @dataclass
    class DisplayInfo:
        group: object
        channel_id: int
        db_message_id: str
        title: str

    DISPLAYS = [DisplayInfo(GUILD_LIST, GUILD_CHANNEL_ID, "guild_msg_id", "# SELECTIUM PLAYER LIST <:Selectium:1345924666526793848>"),
                DisplayInfo(Player.Category.ALLY, ALLY_CHANNEL_ID, "ally_msg_id", "# ALLY LIST <:Cubecraft:1468618358982443111>"),
                DisplayInfo(Player.Category.NEUTRAL, NEUTRAL_CHANNEL_ID, "neutral_msg_id", "# NEUTRAL LIST 🤚🏻"),
                DisplayInfo(Player.Category.TARGET, TARGET_CHANNEL_ID, "target_msg_id", "# TARGET LIST ⚔️"),
                DisplayInfo(KILL_LEADERBOARD, LEADERBOARD_CHANNEL_ID, "leaderboard_msg_id", "# TARGETS ELIMINATED LEADERBOARD")]

    def __init__(self, bot):
        self.bot = bot
        self.player_list = {}

    async def append_list_player(self, guild, player_data: Player):
        if player_data.org == guild.name:
            player_group = self.GUILD_LIST
            player_subgroup = self.NO_SUBGROUP

            try:
                if player_data.dc_id is not None:
                    dc_member = guild.get_member(player_data.dc_id) or await guild.fetch_member(player_data.dc_id)

                    if highest_role := next((r for role_id in reversed(ORDERED_ROLES_IDS)
                                             if (r := dc_member.get_role(role_id))), None):
                        player_subgroup = highest_role.name

                if player_group not in self.player_list:
                    self.player_list[player_group] = {(guild.get_role(role_id) or
                                                       await guild.fetch_role(role_id)).name:{}
                                                      for role_id in reversed(ORDERED_ROLES_IDS)}
            except discord.DiscordException:
                pass

            if player_data.vanquishes is not None:
                (self.player_list.setdefault(self.KILL_LEADERBOARD, {})
                 .setdefault(player_data.vanquishes, {})
                 .setdefault(player_data.alias, [])
                 .append(player_data.mc_name))
        else:
            player_group = player_data.cat
            player_subgroup = player_data.org or self.NO_SUBGROUP

        (self.player_list.setdefault(player_group, {})
         .setdefault(player_subgroup, {})
         .setdefault(player_data.alias, [])
         .append(player_data.mc_name))

    async def push_list_update(self):
        guild_config = asdict(await self.bot.db.get_server_data())

        for display in self.DISPLAYS:
            subgroups = self.player_list.get(display.group) or {}

            match display.group:
                case self.GUILD_LIST:
                    display_str = display.title + "\n" + "".join(
                        (f"## {subgroup}\n" + " ".join(
                            f"`{alias} = {' / '.join(sorted(members, key=str.lower))}`" if alias else
                            " ".join(f"`{m}`" for m in sorted(members, key=str.lower))
                            for alias, members in sorted(aliases.items(), key=lambda x: (x[0] is not None, (x[0] or "").lower()))
                        ) + "\n\n")
                        for subgroup, aliases in subgroups.items()
                    )
                case self.KILL_LEADERBOARD:
                    display_str = display.title + "\n```\n" + "".join(
                        f"{f'{player}: ':<20} {f'{subgroup} kills':>20}\n"
                        for subgroup, aliases in sorted(subgroups.items(), key=lambda x: x[0], reverse=True)
                        for alias, members in sorted(aliases.items(), key=lambda x: (x[0] is not None, (x[0] or "").lower()))
                        for player in ([f"{alias} = {' / '.join(sorted(members, key=str.lower))}"] if alias
                                       else [f"{m}" for m in sorted(members, key=str.lower)])
                    ) + "\n```"
                case _:
                    display_str = display.title + "\n" + "".join(
                        (f"## {subgroup}\n" + " ".join(
                            f"`{alias} = {' / '.join(sorted(members, key=str.lower))}`" if alias else
                            " ".join(f"`{m}`" for m in sorted(members, key=str.lower))
                            for alias, members in sorted(aliases.items(), key=lambda x: (x[0] is not None, (x[0] or "").lower()))
                        ) + "\n\n")
                        for subgroup, aliases in sorted(subgroups.items(), key=lambda x: (x[0] is self.NO_SUBGROUP, (x[0] or "").lower()))
                    )

            try:
                channel = self.bot.get_channel(display.channel_id) or await self.bot.fetch_channel(display.channel_id)
                if guild_config[display.db_message_id]:
                    await (channel.get_partial_message(guild_config[display.db_message_id])).edit(content=display_str)
                    continue
            except discord.NotFound:
                pass
            except discord.HTTPException:
                continue

            try:
                guild_config[display.db_message_id] = (await channel.send(display_str)).id
            except discord.DiscordException:
                pass

        self.player_list.clear()
        await self.bot.db.insert_server_data(**guild_config)

async def setup(bot):
    await bot.add_cog(Announcements(bot))