package net.byws.selectiummod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
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
    private static final SuggestionProvider<FabricClientCommandSource> ONLINE_PLAYER_NAMES =
            (ctx, builder) ->
                    SharedSuggestionProvider.suggest(ConfigManager.getInstance().getOnlinePlayers().keySet(), builder);

    private static final SuggestionProvider<FabricClientCommandSource> PLAYER_LIST_NAMES =
            (ctx, builder) ->
                    SharedSuggestionProvider.suggest(ConfigManager.getInstance().getPlayerNameList().keySet(), builder);

    private static String validateName(String commandInput, Collection<String> usernames)
    {
        for (String username : usernames) { if (username.equalsIgnoreCase(commandInput)) return username; }
        return commandInput;
    }

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher)
    {
        ConfigManager configManager = ConfigManager.getInstance();
        var playerAddLiteral = ClientCommandManager.literal("add");
        var playerEditLiteral = ClientCommandManager.literal("edit");

        for (ConfigJsonObj.Category category : ConfigJsonObj.Category.values())
        {
            playerAddLiteral.then(ClientCommandManager.literal(category.name())
                    .then(ClientCommandManager.argument("name/UUID", StringArgumentType.word())
                            .suggests(ONLINE_PLAYER_NAMES)
                            .executes(ctx -> playerAdd(ctx,
                                    validateName(StringArgumentType.getString(ctx, "name/UUID"), configManager.getOnlinePlayers().keySet()),
                                    category))));
        }
        for (ConfigJsonObj.Category category : ConfigJsonObj.Category.values())
        {
            playerEditLiteral.then(ClientCommandManager.literal(category.name())
                    .then(ClientCommandManager.argument("name/UUID", StringArgumentType.word())
                            .suggests(PLAYER_LIST_NAMES)
                            .executes(ctx -> playerEdit(ctx,
                                    validateName(StringArgumentType.getString(ctx, "name/UUID"), configManager.getPlayerNameList().keySet()),
                                    category))));
        }

        dispatcher.register(
                ClientCommandManager.literal("selectium")

                        .then(ClientCommandManager.literal("player")

                                // /playerAdd <Category> <name/UUID>
                                .then(playerAddLiteral)

                                // /playerEdit <Category> <name/UUID>
                                .then(playerEditLiteral)

                                // /playerRemove <name/UUID>
                                .then(ClientCommandManager.literal("remove")
                                        .then(ClientCommandManager.argument("name/UUID", StringArgumentType.word())
                                                .suggests(PLAYER_LIST_NAMES)
                                                .executes(ctx -> playerRemove(ctx,
                                                        validateName(StringArgumentType.getString(ctx, "name/UUID"), configManager.getPlayerNameList().keySet())))))

                                .then(ClientCommandManager.literal("group")

                                        // /playerGroupEdit <name/UUID> <group>
                                        .then(ClientCommandManager.literal("edit")
                                                .then(ClientCommandManager.argument("name/UUID", StringArgumentType.word())
                                                        .suggests(PLAYER_LIST_NAMES)
                                                        .then(ClientCommandManager.argument("group", StringArgumentType.word())
                                                                .executes(ctx -> playerGroupSet(ctx,
                                                                        validateName(StringArgumentType.getString(ctx, "name/UUID"), configManager.getPlayerNameList().keySet()),
                                                                        StringArgumentType.getString(ctx, "group"))))))

                                        // /playerGroupRemove <name/UUID>
                                        .then(ClientCommandManager.literal("remove")
                                                .then(ClientCommandManager.argument("name/UUID", StringArgumentType.word())
                                                        .suggests(PLAYER_LIST_NAMES)
                                                        .executes(ctx -> playerGroupSet(ctx,
                                                                validateName(StringArgumentType.getString(ctx, "name/UUID"), configManager.getPlayerNameList().keySet()),
                                                                ""))))

                                        // /playerGroupToggle
                                        .then(ClientCommandManager.literal("toggle")
                                                .executes(SelectiumCommand::playerGroupToggle))
                                )

                                .then(ClientCommandManager.literal("nickname")

                                        // /playerNicknameEdit <name/UUID> <nickname>
                                        .then(ClientCommandManager.literal("edit")
                                                .then(ClientCommandManager.argument("name/UUID", StringArgumentType.word())
                                                        .suggests(PLAYER_LIST_NAMES)
                                                        .then(ClientCommandManager.argument("nickname", StringArgumentType.word())
                                                                .executes(ctx -> playerNicknameSet(ctx,
                                                                        validateName(StringArgumentType.getString(ctx, "name/UUID"), configManager.getPlayerNameList().keySet()),
                                                                        StringArgumentType.getString(ctx, "nickname"))))))

                                        // /playerNicknameRemove <name/UUID>
                                        .then(ClientCommandManager.literal("remove")
                                                .then(ClientCommandManager.argument("name/UUID", StringArgumentType.word())
                                                        .suggests(PLAYER_LIST_NAMES)
                                                        .executes(ctx -> playerNicknameSet(ctx,
                                                                validateName(StringArgumentType.getString(ctx, "name/UUID"), configManager.getPlayerNameList().keySet()),
                                                                ""))))

                                        // /playerNicknameToggle
                                        .then(ClientCommandManager.literal("toggle")
                                                .executes(SelectiumCommand::playerNicknameToggle))
                                )

                                .then(ClientCommandManager.literal("clear")
                                        .executes(SelectiumCommand::playerClear))

                                // /playerToggle
                                .then(ClientCommandManager.literal("toggle")
                                        .executes(SelectiumCommand::playerToggle))
                        )

                        .then(ClientCommandManager.literal("discord")

                                // /discordLink <token>
                                .then(ClientCommandManager.literal("link")
                                        .then(ClientCommandManager.argument("token", StringArgumentType.word())
                                        .executes(ctx -> discordSet(ctx,
                                                StringArgumentType.getString(ctx, "token")))))

                                // /discordUnlink
                                .then(ClientCommandManager.literal("unlink")
                                        .executes(ctx -> discordSet(ctx, null)))
                        )

                        .then(ClientCommandManager.literal("stats")
                                .executes(SelectiumCommand::showStats))

                        // /help  (also default)
                        .then(ClientCommandManager.literal("help")
                                .executes(SelectiumCommand::showHelp))

                        .executes(SelectiumCommand::showHelp)
        );
    }

    private static int playerAdd(CommandContext<FabricClientCommandSource> ctx, String nameOrUUID, ConfigJsonObj.Category type)
    {
        ConfigManager configManager = ConfigManager.getInstance();

        try
        {
            if (configManager.addListPlayer(configManager.getOnlinePlayers().get(nameOrUUID), true, type, null, null) ||
                    configManager.addListPlayer(UUID.fromString(nameOrUUID), true, type, null, null))
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
            if (configManager.editListPlayer(nameOrUUID, true, type, null, null) ||
                    configManager.editListPlayer(UUID.fromString(nameOrUUID), true, type, null, null))
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
            if (configManager.removeListPlayer(nameOrUUID, true) || configManager.removeListPlayer(UUID.fromString(nameOrUUID), true))
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

    public static int playerGroupSet(CommandContext<FabricClientCommandSource> ctx, String nameOrUUID, String group)
    {
        ConfigManager configManager = ConfigManager.getInstance();

        try
        {
            if (configManager.editListPlayer(nameOrUUID, true, null, group, null) ||
                    configManager.editListPlayer(UUID.fromString(nameOrUUID), true, null, group, null))
            {
                if (group.isEmpty()) chatMessage(ctx, ChatFormatting.GRAY, "commands.selectium-mod.playerGroupSet.removed", nameOrUUID);
                else chatMessage(ctx, ChatFormatting.GRAY, "commands.selectium-mod.playerGroupSet.edited", nameOrUUID, group);
            }
            else
            {
                chatMessage(ctx, ChatFormatting.RED,"commands.selectium-mod.playerGroupSet.failed", nameOrUUID);
            }
        }
        catch (Exception e)
        {
            chatMessage(ctx, ChatFormatting.RED, "commands.selectium-mod.playerGroupSet.invalid");
        }

        return 1;
    }

    private static int playerGroupToggle(CommandContext<FabricClientCommandSource> ctx)
    {
        ConfigManager configManager = ConfigManager.getInstance();

        configManager.setEnableGroupInfo(!configManager.getEnableGroupInfo());
        chatMessage(ctx, ChatFormatting.GRAY, configManager.getEnableGroupInfo() ?
                "commands.selectium-mod.playerGroupToggle.enable" : "commands.selectium-mod.playerGroupToggle.disable");
        return 1;
    }

    public static int playerNicknameSet(CommandContext<FabricClientCommandSource> ctx, String nameOrUUID, String nickname)
    {
        ConfigManager configManager = ConfigManager.getInstance();

        try
        {
            if (configManager.editListPlayer(nameOrUUID, true, null, null, nickname) ||
                    configManager.editListPlayer(UUID.fromString(nameOrUUID), true, null, null, nickname))
            {
                if (nickname.isEmpty()) chatMessage(ctx, ChatFormatting.GRAY, "commands.selectium-mod.playerNicknameSet.removed", nameOrUUID);
                else chatMessage(ctx, ChatFormatting.GRAY, "commands.selectium-mod.playerNicknameSet.edited", nameOrUUID, nickname);
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

    private static int playerNicknameToggle(CommandContext<FabricClientCommandSource> ctx)
    {
        ConfigManager configManager = ConfigManager.getInstance();

        configManager.setEnableNicknameInfo(!configManager.getEnableNicknameInfo());
        chatMessage(ctx, ChatFormatting.GRAY, configManager.getEnableNicknameInfo() ?
                "commands.selectium-mod.playerNicknameToggle.enable" : "commands.selectium-mod.playerNicknameToggle.disable");
        return 1;
    }

    private static int playerClear(CommandContext<FabricClientCommandSource> ctx)
    {
        ConfigManager.getInstance().clearList(true);
        chatMessage(ctx, ChatFormatting.GRAY, "commands.selectium-mod.playerClear");
        return 1;
    }

    private static int playerToggle(CommandContext<FabricClientCommandSource> ctx)
    {
        ConfigManager configManager = ConfigManager.getInstance();

        configManager.setEnableInfo(!configManager.getEnableInfo());
        chatMessage(ctx, ChatFormatting.GRAY, configManager.getEnableInfo() ?
                "commands.selectium-mod.playerToggle.enable" : "commands.selectium-mod.playerToggle.disable");
        return 1;
    }

    private static int discordSet(CommandContext<FabricClientCommandSource> ctx, String token)
    {
        ConfigManager configManager = ConfigManager.getInstance();
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

        if (token == null)
        {
            configManager.setToken(null);

            if (discordConnection.isDisconnected())
            {
                configManager.clearList(false);
                chatMessage(ctx, ChatFormatting.RED, "commands.selectium-mod.discordSet.disconnected");
            }
            else
            {
                chatMessage(ctx, ChatFormatting.GRAY, "commands.selectium-mod.discordSet.disconnecting");

                discordConnection.disconnect()
                        .whenCompleteAsync((ws, ex) ->
                        {
                            configManager.clearList(false);
                            if (ex == null) chatMessage(ctx, ChatFormatting.GRAY, "commands.selectium-mod.discordSet.disconnected");
                            else chatMessage(ctx, ChatFormatting.RED, "commands.selectium-mod.discordSet.disconnected");
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
                ConfigManager.getInstance().setToken(token);
                chatMessage(ctx, ChatFormatting.GRAY, "commands.selectium-mod.discordSet.connecting");

                discordConnection.connect()
                        .whenCompleteAsync((ws, ex) ->
                        {
                            if (ex == null) chatMessage(ctx, ChatFormatting.GRAY, "commands.selectium-mod.discordSet.connected");
                            else chatMessage(ctx, ChatFormatting.RED, "commands.selectium-mod.discordSet.disconnected");
                        }, ctx.getSource().getClient()::execute);
            }
        }

        return 1;
    }

    private static int showStats(CommandContext<FabricClientCommandSource> ctx)
    {
        ConfigManager configManager = ConfigManager.getInstance();
        chatHeader(ctx, ChatFormatting.YELLOW,"commands.selectium-mod.showStats.title");

        ctx.getSource().sendFeedback(
                Component.literal("  ")
                        .append(Component.translatable("commands.selectium-mod.showStats.clientKills").withStyle(ChatFormatting.RED))
                        .append(Component.literal(": " + configManager.getKills(true)).withStyle(ChatFormatting.GRAY))
        );

        ctx.getSource().sendFeedback(
                Component.literal("  ")
                        .append(Component.translatable("commands.selectium-mod.showStats.serverKills").withStyle(ChatFormatting.DARK_RED))
                        .append(Component.literal(": " + configManager.getKills(false)).withStyle(ChatFormatting.GRAY))
        );

        return 1;
    }

    private static int showHelp(CommandContext<FabricClientCommandSource> ctx)
    {
        chatHeader(ctx, ChatFormatting.YELLOW,"commands.selectium-mod.showHelp.title");

        String[][] cmds = {
                {"/selectium player add <Category> <name/UUID>", "commands.selectium-mod.showHelp.playerAdd"},
                {"/selectium player edit <Category> <name/UUID>", "commands.selectium-mod.showHelp.playerEdit"},
                {"/selectium player remove <name/UUID>", "commands.selectium-mod.showHelp.playerRemove"},
                {"/selectium player group add <name/UUID> <group>", "commands.selectium-mod.showHelp.playerGroupEdit"},
                {"/selectium player group remove <name/UUID>", "commands.selectium-mod.showHelp.playerGroupRemove"},
                {"/selectium player group toggle", "commands.selectium-mod.showHelp.playerGroupToggle"},
                {"/selectium player nickname add <name/UUID> <nickname>", "commands.selectium-mod.showHelp.playerNicknameEdit"},
                {"/selectium player nickname remove <name/UUID>", "commands.selectium-mod.showHelp.playerNicknameRemove"},
                {"/selectium player nickname toggle", "commands.selectium-mod.showHelp.playerNicknameToggle"},
                {"/selectium player clear", "commands.selectium-mod.showHelp.playerClear"},
                {"/selectium player toggle", "commands.selectium-mod.showHelp.playerToggle"},
                {"/selectium discord link <token>", "commands.selectium-mod.showHelp.discordLink"},
                {"/selectium discord unlink", "commands.selectium-mod.showHelp.discordUnlink"},
                {"/selectium stats", "commands.selectium-mod.showHelp.stats"},
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

    private static void chatHeader(CommandContext<FabricClientCommandSource> ctx, ChatFormatting color, String key, Object... params)
    {
        ctx.getSource().sendFeedback(
                Component.literal("━━━ ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.translatable(key, params).withStyle(color, ChatFormatting.BOLD))
                .append(Component.literal(" ━━━").withStyle(ChatFormatting.GRAY))
        );
    }

    private static void chatMessage(CommandContext<FabricClientCommandSource> ctx, ChatFormatting color, String key, Object... params)
    {
        ctx.getSource().sendFeedback(
                Component.literal("[Selectium] ")
                        .withStyle(ChatFormatting.YELLOW)
                        .append(Component.translatable(key, params).withStyle(color))
        );
    }
}
