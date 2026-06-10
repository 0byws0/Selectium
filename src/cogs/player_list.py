import discord
from discord import app_commands
from discord.ext import commands, tasks
import Paginator
from uuid import UUID

from src.config import GUILD_ID
from src.database_manager import DatabaseManager
from src.player_handler import Player

class PlayerList(commands.Cog):
    def __init__(self, bot):
        self.bot = bot
        self.upd_last_uuid = None
        self.update.start()

    def cog_unload(self):
        self.update.cancel()

    @staticmethod
    def player_info_embed(player_data: Player) -> discord.Embed:
        info_embed = discord.Embed(
            title="Selectium Database",
            color=player_data.cat.color
        )
        info_embed.add_field(name="Username:", value=player_data.mc_name, inline=False)
        info_embed.add_field(name="Alt:", value=player_data.alias, inline=False)
        info_embed.add_field(name="Clan:", value=player_data.org, inline=False)
        info_embed.add_field(name="Category:", value=player_data.cat.name, inline=False)
        info_embed.add_field(name="UUID:", value=player_data.mc_uuid, inline=False)
        info_embed.add_field(name="Discord Id:", value=player_data.dc_id, inline=False)

        return info_embed

    @app_commands.command(name="player_add")
    @app_commands.guilds(discord.Object(GUILD_ID))
    # @app_commands.default_permissions(administrator=True)
    # @app_commands.checks.has_any_role(*access_id)
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
                await interaction.followup.send(f"Player {player_data.mc_name} added")
                await self.bot.conn_manager.broadcast()
            else:
                await interaction.followup.send(f"Player {player_data.mc_name} already in database")
        except ValueError:
            await interaction.followup.send(f"Failed to add player {name_or_uuid}")

    @app_commands.command(name="player_edit")
    @app_commands.guilds(discord.Object(GUILD_ID))
    async def player_edit(self, interaction, field: DatabaseManager.Fields, field_input: str, alt: str = None,
                            clan: str = None, category: Player.Category = None, discord_id: str = None):
        await interaction.response.defer()

        upd_params = {}
        if alt is not None: upd_params[DatabaseManager.Fields.ALT.value] = alt if alt != "-" else None
        if clan is not None: upd_params[DatabaseManager.Fields.CLAN.value] = clan if clan != "-" else None
        if category is not None: upd_params[DatabaseManager.Fields.CATEGORY.value] = category
        if discord_id is not None: upd_params[DatabaseManager.Fields.DISCORD.value] = int(discord_id) if discord_id.isdigit() else None

        if upd_params:
            if player_names := await self.bot.db.update_player(field, field_input, **upd_params):
                await interaction.followup.send(f"Player(s) {', '.join(player_names)} updated")
                if category is not None or alt is not None: await self.bot.conn_manager.broadcast()
            else:
                await interaction.followup.send(f"{field.name.capitalize()} {field_input} not found in database")
        else:
            await interaction.followup.send("Nothing provided to update")

    @app_commands.command(name="player_info")
    @app_commands.guilds(discord.Object(GUILD_ID))
    async def player_info(self, interaction, field: DatabaseManager.Fields, field_input: str):
        await interaction.response.defer()

        if player_data_list := await self.bot.db.get_player(field, field_input):
            await Paginator.Simple().start(interaction, pages=[self.player_info_embed(player_data) for player_data in player_data_list])
        else:
            await interaction.followup.send(f"{field.name.capitalize()} {field_input} not found in database")

    @app_commands.command(name="player_remove")
    @app_commands.guilds(discord.Object(GUILD_ID))
    async def player_remove(self, interaction, field: DatabaseManager.Fields, field_input: str):
        await interaction.response.defer()

        if player_names := await self.bot.db.remove_player(field, field_input):
            await interaction.followup.send(f"Player(s) {', '.join(player_names)} removed")
            await self.bot.conn_manager.broadcast()
        else:
            await interaction.followup.send(f"{field.name.capitalize()} {field_input} not found database")

    # @player_edit.autocomplete("field_input")
    # @player_info.autocomplete("field_input")
    # @player_remove.autocomplete("field_input")
    # async def field_input_autocomplete(self, interaction: discord.Interaction, current: str) -> list[app_commands.Choice[str]]:
    #     if not current: return [app_commands.Choice(name="Type to search fields...", value="")]
    #
    #     try:
    #         field_data = await self.bot.db.get_field(DatabaseManager.Fields(interaction.namespace.field))
    #     except Exception:
    #         return []
    #
    #     current = current.lower()
    #     filtered_choice = []
    #
    #     for choice in field_data:
    #         choice = str(choice)
    #         if current in choice.lower():
    #             filtered_choice.append(app_commands.Choice(name=choice, value=choice))
    #         if len(filtered_choice) >= 25:
    #             break
    #
    #     return filtered_choice
    #
    # @player_edit.autocomplete("clan")
    # async def clan_autocomplete(self, interaction: discord.Interaction, current: str) -> list[app_commands.Choice[str]]:
    #     if not current or current == "-" or current == "- (remove)": choices = [app_commands.Choice(name="- (remove)", value="-")]
    #     choices += await self.field_input_autocomplete(interaction, current)
    #     return choices

    @tasks.loop(minutes=30)
    async def update(self):
        players = await self.bot.db.get_player()
        upd_count = -next((i for i, p in enumerate(players) if p.mc_uuid == self.upd_last_uuid), 0)
        self.upd_last_uuid = None

        for player_data in players:
            if self.upd_last_uuid is None:
                if upd_count >= 150:
                    self.upd_last_uuid = player_data.mc_uuid
                elif upd_count >= 0:
                    await player_data.update()
                    print(player_data.mc_name) # remove later
                    await self.bot.db.update_player(DatabaseManager.Fields.UUID, player_data.mc_uuid, mc_name=player_data.mc_name)
                upd_count += 1

            await self.bot.get_cog("Announcements").append_list_player(player_data)
        await self.bot.get_cog("Announcements").push_list_update()

    @update.before_loop
    async def before_update(self):
        await self.bot.wait_until_ready()

async def setup(bot):
    await bot.add_cog(PlayerList(bot))