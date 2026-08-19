package net.byws.selectiummod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.UUID;

import net.byws.selectiummod.config.ConfigJsonObj;
import net.byws.selectiummod.config.ConfigManager;
import net.byws.selectiummod.websocket.DiscordConnection;

public class SelectiumCommand
{
    // ── Suggestions ────────────────────────────────────────────────────────
    private static final SuggestionProvider<FabricClientCommandSource> ONLINE_PLAYER_NAMES =
            (ctx, builder) ->
                    SharedSuggestionProvider.suggest(ConfigManager.getInstance().getOnlinePlayers().keySet(), builder);

    /** Suggests names already in the player list. */
    private static final SuggestionProvider<FabricClientCommandSource> PLAYER_LIST_NAMES =
            (ctx, builder) ->
                    SharedSuggestionProvider.suggest(ConfigManager.getInstance().getPlayerNameList().keySet(), builder);

    private static String validateName(String commandInput, Collection<String> usernames)
    {
        for (String username : usernames) { if (username.equalsIgnoreCase(commandInput)) return username; }
        return commandInput;
    }

    // ── Registration ───────────────────────────────────────────────────────

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher)
    {
        ConfigManager configManager = ConfigManager.getInstance();
        var playerAddLiteral = ClientCommands.literal("add");
        var playerEditLiteral = ClientCommands.literal("edit");

        for (ConfigJsonObj.Category category : ConfigJsonObj.Category.values())
        {
            playerAddLiteral.then(ClientCommands.literal(category.name())
                    .then(ClientCommands.argument("name/UUID", StringArgumentType.word())
                            .suggests(ONLINE_PLAYER_NAMES)
                            .executes(ctx -> playerAdd(ctx,
                                    validateName(StringArgumentType.getString(ctx, "name/UUID"), configManager.getOnlinePlayers().keySet()),
                                    category))));
        }
        for (ConfigJsonObj.Category category : ConfigJsonObj.Category.values())
        {
            playerEditLiteral.then(ClientCommands.literal(category.name())
                    .then(ClientCommands.argument("name/UUID", StringArgumentType.word())
                            .suggests(PLAYER_LIST_NAMES)
                            .executes(ctx -> playerEdit(ctx,
                                    validateName(StringArgumentType.getString(ctx, "name/UUID"), configManager.getPlayerNameList().keySet()),
                                    category))));
        }

        dispatcher.register(
                ClientCommands.literal("selectium")

                        .then(ClientCommands.literal("player")

                                // /playerAdd <Category> <name/UUID>
                                .then(playerAddLiteral)

                                // /playerEdit <Category> <name/UUID>
                                .then(playerEditLiteral)

                                // /playerRemove <name/UUID>
                                .then(ClientCommands.literal("remove")
                                        .then(ClientCommands.argument("name/UUID", StringArgumentType.word())
                                                .suggests(PLAYER_LIST_NAMES)
                                                .executes(ctx -> playerRemove(ctx,
                                                        validateName(StringArgumentType.getString(ctx, "name/UUID"), configManager.getPlayerNameList().keySet())))))

                                .then(ClientCommands.literal("nickname")

                                        // /playerNicknameAdd <name/UUID> <nickname>
                                        .then(ClientCommands.literal("edit")
                                                .then(ClientCommands.argument("name/UUID", StringArgumentType.word())
                                                        .suggests(PLAYER_LIST_NAMES)
                                                        .then(ClientCommands.argument("nickname", StringArgumentType.word())
                                                                .executes(ctx -> playerNicknameSet(ctx,
                                                                        validateName(StringArgumentType.getString(ctx, "name/UUID"), configManager.getPlayerNameList().keySet()),
                                                                        StringArgumentType.getString(ctx, "nickname"))))))

                                        // /playerNicknameRemove <name/UUID>
                                        .then(ClientCommands.literal("remove")
                                                .then(ClientCommands.argument("name/UUID", StringArgumentType.word())
                                                        .suggests(PLAYER_LIST_NAMES)
                                                        .executes(ctx -> playerNicknameSet(ctx,
                                                                validateName(StringArgumentType.getString(ctx, "name/UUID"), configManager.getPlayerNameList().keySet()),
                                                                ""))))
                                )

                                .then(ClientCommands.literal("clearAll")
                                        .executes(SelectiumCommand::playerClearAll))

                                // /playerToggleInfo
                                .then(ClientCommands.literal("toggleInfo")
                                        .executes(SelectiumCommand::playerToggleInfo))
                        )

                        .then(ClientCommands.literal("discord")

                                // /discordLink <token>
                                .then(ClientCommands.literal("link")
                                        .then(ClientCommands.argument("token", StringArgumentType.word())
                                        .executes(ctx -> discordSet(ctx,
                                                StringArgumentType.getString(ctx, "token")))))

                                // /discordReink
                                .then(ClientCommands.literal("relink")
                                        .executes(ctx -> discordSet(ctx, null)))

                                // /discordUnlink
                                .then(ClientCommands.literal("unlink")
                                        .executes(ctx -> discordSet(ctx, ""))))


                        // /help  (also default)
                        .then(ClientCommands.literal("help")
                                .executes(SelectiumCommand::showHelp))

                        .executes(SelectiumCommand::showHelp)
        );
    }

    // ── Handlers ───────────────────────────────────────────────────────────

    private static int playerAdd(CommandContext<FabricClientCommandSource> ctx, String nameOrUUID, ConfigJsonObj.Category type)
    {
        ConfigManager configManager = ConfigManager.getInstance();

        try
        {
            if (configManager.addListPlayer(configManager.getOnlinePlayers().get(nameOrUUID), true, type, null) ||
                    configManager.addListPlayer(UUID.fromString(nameOrUUID), true, type, null))
            {
                chatMessage(ctx, ChatFormatting.GRAY, "commands.selectium-mod.playerAdd.added", nameOrUUID, type.name());
            }
            else
            {
                chatMessage(ctx, ChatFormatting.RED, "commands.selectium-mod.playerAdd.failed", nameOrUUID);
            }
        }
        catch (Exception e)
        {
            chatMessage(ctx, ChatFormatting.RED, "commands.selectium-mod.playerAdd.invalid");
        }

        return 1;
    }

    private static int playerEdit(CommandContext<FabricClientCommandSource> ctx, String nameOrUUID, ConfigJsonObj.Category type)
    {
        ConfigManager configManager = ConfigManager.getInstance();

        try
        {
            if (configManager.editListPlayer(nameOrUUID, true, type, null) || configManager.editListPlayer(UUID.fromString(nameOrUUID), true, type, null))
            {
                chatMessage(ctx, ChatFormatting.GRAY, "commands.selectium-mod.playerEdit.edited", nameOrUUID, type.name());
            }
            else
            {
                chatMessage(ctx, ChatFormatting.RED, "commands.selectium-mod.playerEdit.failed", nameOrUUID);
            }
        }
        catch (Exception e)
        {
            chatMessage(ctx, ChatFormatting.RED, "commands.selectium-mod.playerEdit.invalid");
        }

        return 1;
    }

    private static int playerRemove(CommandContext<FabricClientCommandSource> ctx, String nameOrUUID)
    {
        ConfigManager configManager = ConfigManager.getInstance();

        try
        {
            if (configManager.removeListPlayer(nameOrUUID) || configManager.removeListPlayer(UUID.fromString(nameOrUUID)))
            {
                chatMessage(ctx, ChatFormatting.GRAY, "commands.selectium-mod.playerRemove.removed", nameOrUUID);
            }
            else
            {
                chatMessage(ctx, ChatFormatting.RED, "commands.selectium-mod.playerRemove.failed", nameOrUUID);
            }
        }
        catch (Exception e)
        {
            chatMessage(ctx, ChatFormatting.RED, "commands.selectium-mod.playerRemove.invalid");
        }

        return 1;
    }

    public static int playerNicknameSet(CommandContext<FabricClientCommandSource> ctx, String nameOrUUID, String nickname)
    {
        ConfigManager configManager = ConfigManager.getInstance();

        try
        {
            if (configManager.editListPlayer(nameOrUUID, null, null, nickname) || configManager.editListPlayer(UUID.fromString(nameOrUUID), null, null, nickname))
            {
                if (nickname.isEmpty())
                {
                    chatMessage(ctx, ChatFormatting.GRAY, "commands.selectium-mod.playerNicknameSet.removed", nameOrUUID);
                }
                else
                {
                    chatMessage(ctx, ChatFormatting.GRAY, "commands.selectium-mod.playerNicknameSet.edited", nameOrUUID, nickname);
                }
            }
            else
            {
                chatMessage(ctx, ChatFormatting.RED,"commands.selectium-mod.playerNicknameSet.failed", nameOrUUID);
            }
        }
        catch (Exception e)
        {
            chatMessage(ctx, ChatFormatting.RED, "commands.selectium-mod.playerNicknameSet.invalid");
        }

        return 1;
    }

    private static int playerClearAll(CommandContext<FabricClientCommandSource> ctx)
    {
        ConfigManager.getInstance().clearList();
        chatMessage(ctx, ChatFormatting.GRAY, "commands.selectium-mod.playerClearAll");
        return 1;
    }

    private static int playerToggleInfo(CommandContext<FabricClientCommandSource> ctx)
    {
        ConfigManager configManager = ConfigManager.getInstance();

        configManager.setEnableNameInfo(!configManager.getEnableNameInfo());
        chatMessage(ctx, ChatFormatting.GRAY, configManager.getEnableNameInfo() ? "commands.selectium-mod.playerToggleInfo.enable"
                : "commands.selectium-mod.playerToggleInfo.disable");
        return 1;
    }

    private static int discordSet(CommandContext<FabricClientCommandSource> ctx, String token)
    {
        DiscordConnection discordConnection = DiscordConnection.getInstance();

        if (discordConnection.isConnecting())
        {
            chatMessage(ctx, ChatFormatting.RED, "commands.selectium-mod.discordSet.connecting");
            return 1;
        }
        if (discordConnection.isDisconnecting())
        {
            chatMessage(ctx, ChatFormatting.RED, "commands.selectium-mod.discordSet.disconnecting");
            return 1;
        }

        if (token != null && token.isEmpty())
        {
            ConfigManager.getInstance().setToken(null);

            if (discordConnection.isDisconnected())
            {
                chatMessage(ctx, ChatFormatting.RED, "commands.selectium-mod.discordSet.disconnected");
            }
            else
            {
                chatMessage(ctx, ChatFormatting.WHITE, "commands.selectium-mod.discordSet.disconnecting");

                discordConnection.disconnect()
                        .thenAcceptAsync(ws ->
                        {
                            chatMessage(ctx, ChatFormatting.WHITE, "commands.selectium-mod.discordSet.disconnected");
                        }, ctx.getSource().getClient()::execute)
                        .exceptionallyAsync(ex ->
                        {
                            chatMessage(ctx, ChatFormatting.RED, "commands.selectium-mod.discordSet.connected");
                            return null;
                        }, ctx.getSource().getClient()::execute);
            }
        }
        else
        {
            if (discordConnection.isConnected())
            {
                chatMessage(ctx, ChatFormatting.RED, "commands.selectium-mod.discordSet.connected");
            }
            else
            {
                if (token != null) ConfigManager.getInstance().setToken(token);
                chatMessage(ctx, ChatFormatting.WHITE, "commands.selectium-mod.discordSet.connecting");

                discordConnection.connect()
                        .thenAcceptAsync(ws ->
                        {
                            chatMessage(ctx, ChatFormatting.WHITE, "commands.selectium-mod.discordSet.connected");
                        }, ctx.getSource().getClient()::execute)
                        .exceptionallyAsync(ex ->
                        {
                            chatMessage(ctx, ChatFormatting.RED, "commands.selectium-mod.discordSet.disconnected");
                            return null;
                        }, ctx.getSource().getClient()::execute);
            }
        }

        return 1;
    }

    private static int showHelp(CommandContext<FabricClientCommandSource> ctx)
    {
        chatHeader(ctx, ChatFormatting.YELLOW,"commands.selectium-mod.showHelp.title");

        String[][] cmds = {
                {"/selectium player add <Category> <name/UUID>", "commands.selectium-mod.showHelp.playerAdd"},
                {"/selectium player edit <Category> <name/UUID>", "commands.selectium-mod.showHelp.playerEdit"},
                {"/selectium player remove <name/UUID>", "commands.selectium-mod.showHelp.playerRemove"},
                {"/selectium player nickname add <name/UUID> <nickname>", "commands.selectium-mod.showHelp.playerNicknameEdit"},
                {"/selectium player nickname remove <name/UUID>", "commands.selectium-mod.showHelp.playerNicknameRemove"},
                {"/selectium player clearAll", "commands.selectium-mod.showHelp.playerClearAll"},
                {"/selectium player toggleInfo", "commands.selectium-mod.showHelp.playerToggleInfo"},
                {"/selectium discord link <token>", "commands.selectium-mod.showHelp.discordLink"},
                {"/selectium discord relink", "commands.selectium-mod.showHelp.discordRelink"},
                {"/selectium discord unlink", "commands.selectium-mod.showHelp.discordUnlink"},
        };

        for (String[] cmd : cmds)
        {
            ctx.getSource().sendFeedback(
                    Component.literal("  " + cmd[0]).withStyle(ChatFormatting.YELLOW)
                            .append(Component.literal("  — ").withStyle(ChatFormatting.GRAY))
                            .append(Component.translatable(cmd[1]).withStyle(ChatFormatting.GRAY))
            );
        }

        return 1;
    }

    // ── Formatting helpers ─────────────────────────────────────────────────

    private static void chatHeader(CommandContext<FabricClientCommandSource> ctx, ChatFormatting color, String key, Object... params)
    {
        ctx.getSource().sendFeedback(
                Component.literal("━━━ ")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.translatable(key, params).withStyle(color, ChatFormatting.BOLD))
                .append(Component.literal(" ━━━").withStyle(ChatFormatting.GOLD))
        );
    }

    private static void chatMessage(CommandContext<FabricClientCommandSource> ctx, ChatFormatting color, String key, Object... params)
    {
        ctx.getSource().sendFeedback(
                Component.literal("[Selectium] ")
                        .withStyle(ChatFormatting.GOLD)
                        .append(Component.translatable(key, params).withStyle(color))
        );
    }
}
