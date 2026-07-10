import discord
from discord import app_commands
from discord.ext import commands, tasks
import Paginator
from uuid import UUID
import secrets

from src.config import GUILD_ID, MOD_ROLE_IDS, BOT_ROLE_IDS
from src.database.database_manager import DatabaseManager
from src.websocket.connection_manager import ConnectionManager
from src.database.player_handler import Player

class PlayerList(commands.Cog):
    def __init__(self, bot):
        self.bot = bot
        self.upd_last_uuid = None
        self.update.start()

    def cog_unload(self):
        self.update.cancel()

    @staticmethod
    def player_info_embed(player_data: Player) -> discord.Embed:
        info_embed = discord.Embed(title="Selectium Database", color=player_data.cat.color)

        info_embed.add_field(name="Username:", value=player_data.mc_name, inline=False)
        info_embed.add_field(name="Alt:", value=player_data.alias, inline=False)
        info_embed.add_field(name="Clan:", value=player_data.org, inline=False)
        info_embed.add_field(name="Category:", value=player_data.cat.name, inline=False)
        info_embed.add_field(name="UUID:", value=player_data.mc_uuid, inline=False)
        info_embed.add_field(name="Discord Id:", value=player_data.dc_id, inline=False)
        info_embed.add_field(name="Token:", value="Active" if player_data.token_hash else None, inline=False)

        return info_embed

    @staticmethod
    def token_embed(token: str) -> discord.Embed:
        info_embed = discord.Embed(title="Selectium Mod", description=
        "🇺🇸:\n If you have a previous version of the mod that wasn't installed through Modrinth, please delete it and download the latest version using the link below.\n\n"
        "🇲🇽:\n Si tienes una versión anterior del mod que no fue instalada a través de Modrinth, elimínala y descarga la versión más reciente usando el enlace que aparece a continuación.") # add description

        info_embed.add_field(name="Mod Link:", value="https://modrinth.com/mod/selectium-mod", inline=False)
        info_embed.add_field(name="Token (DO NOT SHARE):", value=token, inline=False)

        return info_embed

    @app_commands.command(name="player_add")
    @app_commands.guilds(discord.Object(GUILD_ID))
    @app_commands.checks.has_any_role(*BOT_ROLE_IDS)
    async def player_add(self, interaction, category: Player.Category, name_or_uuid: str):
        await interaction.response.defer()

        try:
            player_uuid = UUID(name_or_uuid)
            player_username = None
        except ValueError:
            player_uuid = None
            player_username = name_or_uuid

        try:
            player_data = await Player.create(player_uuid, player_username, category)
            if await self.bot.db.insert_player(player_data):
                await interaction.followup.send(f"Player `{player_data.mc_name}` added")
                await self.bot.conn_manager.broadcast(ConnectionManager.Actions.LIST_ADD, player_data)
            else:
                await interaction.followup.send(f"Player `{player_data.mc_name}` already in database")
        except ValueError:
            await interaction.followup.send(f"Failed to add player `{name_or_uuid}`")

    @app_commands.command(name="player_edit")
    @app_commands.guilds(discord.Object(GUILD_ID))
    @app_commands.checks.has_any_role(*BOT_ROLE_IDS)
    async def player_edit(self, interaction, field: DatabaseManager.Fields, field_input: str, alt: str = None,
                            clan: str = None, category: Player.Category = None, discord_id: str = None, token: str = None):
        await interaction.response.defer()

        upd_params = {}
        if alt is not None: upd_params[DatabaseManager.Fields.ALT.value] = alt if alt != "-" else None
        if clan is not None: upd_params[DatabaseManager.Fields.CLAN.value] = clan if clan != "-" else None
        if category is not None: upd_params[DatabaseManager.Fields.CATEGORY.value] = category
        if discord_id is not None: upd_params[DatabaseManager.Fields.DISCORD.value] = discord_id if discord_id.isdigit() else None
        if token is not None: upd_params[DatabaseManager.Fields.TOKEN.value] = None

        if upd_params:
            if player_data_list := await self.bot.db.update_player(field, field_input, **upd_params):
                await interaction.followup.send(f"Player(s) {' '.join([f'`{player_data.mc_name}`' for player_data in player_data_list])} updated")
                if category is not None or alt is not None: await self.bot.conn_manager.broadcast(ConnectionManager.Actions.LIST_EDIT, player_data_list)
            else:
                await interaction.followup.send(f"{field.name.capitalize()} `{field_input}` not found in database")
        else:
            await interaction.followup.send("Nothing provided to update")

    @app_commands.command(name="player_info")
    @app_commands.guilds(discord.Object(GUILD_ID))
    @app_commands.checks.has_any_role(*BOT_ROLE_IDS)
    async def player_info(self, interaction, field: DatabaseManager.Fields, field_input: str):
        await interaction.response.defer()

        if player_data_list := await self.bot.db.get_player(field, field_input):
            await Paginator.Simple().start(interaction, pages=[self.player_info_embed(player_data) for player_data in player_data_list])
        else:
            await interaction.followup.send(f"{field.name.capitalize()} `{field_input}` not found in database")

    @app_commands.command(name="player_remove")
    @app_commands.guilds(discord.Object(GUILD_ID))
    @app_commands.checks.has_any_role(*BOT_ROLE_IDS)
    async def player_remove(self, interaction, field: DatabaseManager.Fields, field_input: str):
        await interaction.response.defer()

        if player_data_list := await self.bot.db.remove_player(field, field_input):
            await interaction.followup.send(f"Player(s) {' '.join([f'`{player_data.mc_name}`' for player_data in player_data_list])} removed")
            await self.bot.conn_manager.broadcast(ConnectionManager.Actions.LIST_REMOVE, player_data_list)
        else:
            await interaction.followup.send(f"{field.name.capitalize()} `{field_input}` not found database")

    async def discord_autocomplete(self, field, current):
        if not field or not current: return []

        field_data = await self.bot.db.get_field(field)
        filtered_choice = []
        cur_input = current.lower()

        for choice in field_data:
            choice = str(choice)
            if cur_input in choice.lower():
                filtered_choice.append(app_commands.Choice(name=choice, value=choice))
            if len(filtered_choice) >= 25:
                break

        return filtered_choice

    @player_edit.autocomplete("field_input")
    @player_info.autocomplete("field_input")
    @player_remove.autocomplete("field_input")
    async def field_input_autocomplete(self, interaction: discord.Interaction, current: str) -> list[app_commands.Choice[str]]:
        return await self.discord_autocomplete(DatabaseManager.Fields(interaction.namespace.field), current)

    @player_edit.autocomplete("alt")
    async def edit_alt_autocomplete(self, interaction: discord.Interaction, current: str) -> list[app_commands.Choice[str]]:
        return ([app_commands.Choice(name="- (remove)", value="-")] if not current or current in ["-", "- (remove)"] else []
                + await self.discord_autocomplete(DatabaseManager.Fields.ALT, current))

    @player_edit.autocomplete("clan")
    async def edit_clan_autocomplete(self, interaction: discord.Interaction, current: str) -> list[app_commands.Choice[str]]:
        return ([app_commands.Choice(name="- (remove)", value="-")] if not current or current in ["-", "- (remove)"] else []
                + await self.discord_autocomplete(DatabaseManager.Fields.CLAN, current))

    @player_edit.autocomplete("discord_id")
    async def edit_discord_id_autocomplete(self, interaction: discord.Interaction, current: str) -> list[app_commands.Choice[str]]:
        return ([app_commands.Choice(name="- (remove)", value="-")] if not current or current in ["-", "- (remove)"] else []
                + await self.discord_autocomplete(DatabaseManager.Fields.DISCORD, current))

    @player_edit.autocomplete("token")
    async def edit_token_autocomplete(self, interaction: discord.Interaction, current: str) -> list[app_commands.Choice[str]]:
        return [app_commands.Choice(name="- (remove)", value="-")] if not current or current in ["-", "- (remove)"] else []

    async def update_player_discord(self, player_data):
        if player_data.token_hash is None and player_data.dc_id is not None:
            guild = self.bot.get_guild(GUILD_ID)
            try:
                dc_member: discord.Member = guild.get_member(player_data.dc_id) or await guild.fetch_member(player_data.dc_id)

                if any(member_role.id in MOD_ROLE_IDS for member_role in dc_member.roles):
                    token = secrets.token_hex(32)

                    await dc_member.send(embed=self.token_embed(token))
                    return {DatabaseManager.Fields.TOKEN.value:token}
            except discord.HTTPException:
                pass
        return {}

    @tasks.loop(minutes=30)
    async def update(self):
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

            upd_params.update(await self.update_player_discord(player_data))

            if upd_params:
                await self.bot.db.update_player(DatabaseManager.Fields.UUID, player_data.mc_uuid, **upd_params)

            await self.bot.get_cog("Announcements").append_list_player(player_data)
        await self.bot.get_cog("Announcements").push_list_update()

    @update.before_loop
    async def before_update(self):
        await self.bot.wait_until_ready()

async def setup(bot):
    await bot.add_cog(PlayerList(bot))