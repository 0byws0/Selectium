package net.byws.selectiummod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.UUID;

import net.byws.selectiummod.config.ConfigJsonObj;
import net.byws.selectiummod.config.ConfigManager;

public class SelectiumCommand
{
    private static String[] onlinePlayerNames = new String[0];

    // ── Suggestions ────────────────────────────────────────────────────────
    private static final SuggestionProvider<FabricClientCommandSource> ONLINE_PLAYER_NAMES =
            (ctx, builder) ->
            {
                var connection = Minecraft.getInstance().getConnection();

                if (connection != null)
                {
                    onlinePlayerNames = connection.getOnlinePlayers().stream()
                            .map(entry -> entry.getProfile().getName())
                            .toArray(String[]::new);
                }

                return SharedSuggestionProvider.suggest(onlinePlayerNames, builder);
            };
    /** Suggests names already in the player list. */
    private static final SuggestionProvider<FabricClientCommandSource> PLAYER_LIST_NAMES =
            (ctx, builder) ->
                    SharedSuggestionProvider.suggest(ConfigManager.getInstance().getNamesList(), builder);

    private static String validateName(String commandInput, String[] usernames)
    {
        for (String username : usernames)
        {
            if (username.equalsIgnoreCase(commandInput)) return username;
        }
        return commandInput;
    }

    // ── Registration ───────────────────────────────────────────────────────

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
                            .executes(ctx ->
                                    playerAdd(ctx, validateName(StringArgumentType.getString(ctx, "name/UUID"), onlinePlayerNames), category))));
        }
        for (ConfigJsonObj.Category category : ConfigJsonObj.Category.values())
        {
            playerEditLiteral.then(ClientCommandManager.literal(category.name())
                    .then(ClientCommandManager.argument("name/UUID", StringArgumentType.word())
                            .suggests(PLAYER_LIST_NAMES)
                            .executes(ctx ->
                                    playerEdit(ctx, validateName(StringArgumentType.getString(ctx, "name/UUID"), configManager.getNamesList()), category))));
        }

        dispatcher.register(
                ClientCommandManager.literal("selectium")

                        .then(ClientCommandManager.literal("player")

                                // /playerAdd <name/UUID>
                                .then(playerAddLiteral)

                                // /playerEdit <name/UUID>
                                .then(playerEditLiteral)

                                // /playerRemove <name/UUID>
                                .then(ClientCommandManager.literal("remove")
                                        .then(ClientCommandManager.argument("name/UUID", StringArgumentType.word())
                                                .suggests(PLAYER_LIST_NAMES)
                                                .executes(ctx ->
                                                        playerRemove(ctx, validateName(StringArgumentType.getString(ctx, "name/UUID"), configManager.getNamesList())))))

                                .then(ClientCommandManager.literal("nickname")

                                        // /playerNicknameAdd <name/UUID> <nickname>
                                        .then(ClientCommandManager.literal("edit")
                                                .then(ClientCommandManager.argument("name/UUID", StringArgumentType.word())
                                                        .suggests(PLAYER_LIST_NAMES)
                                                        .then(ClientCommandManager.argument("nickname", StringArgumentType.word())
                                                                .executes(ctx ->
                                                                        playerNicknameSet(ctx, validateName(StringArgumentType.getString(ctx, "name/UUID"), configManager.getNamesList()),
                                                                                StringArgumentType.getString(ctx, "nickname"))))))

                                        // /playerNicknameRemove <name/UUID>
                                        .then(ClientCommandManager.literal("remove")
                                                .then(ClientCommandManager.argument("name/UUID", StringArgumentType.word())
                                                        .suggests(PLAYER_LIST_NAMES)
                                                        .executes(ctx ->
                                                                playerNicknameSet(ctx, validateName(StringArgumentType.getString(ctx, "name/UUID"), configManager.getNamesList()), ""))))
                                )

                                // /playerToggleInfo
                                .then(ClientCommandManager.literal("toggleInfo")
                                        .executes(SelectiumCommand::playerToggleInfo))

                                .then(ClientCommandManager.literal("reset")
                                        .executes(SelectiumCommand::resetList))
                        )

                        // /help  (also default)
                        .then(ClientCommandManager.literal("help")
                                .executes(SelectiumCommand::showHelp))

                        .executes(SelectiumCommand::showHelp)
        );
    }

    // ── Handlers ───────────────────────────────────────────────────────────

    private static int playerAdd(CommandContext<FabricClientCommandSource> ctx, String nameOrUUID, ConfigJsonObj.Category type)
    {
        try
        {
            var connection = Minecraft.getInstance().getConnection();
            String uuid = nameOrUUID;

            if (connection != null)
            {
                uuid = connection.getOnlinePlayers().stream()
                    .filter(p -> p.getProfile().getName().equals(nameOrUUID))
                    .findFirst()
                    .map(p -> p.getProfile().getId().toString())
                    .orElse(nameOrUUID);
            }

            if (ConfigManager.getInstance().addListPlayer(UUID.fromString(uuid), false, type, ""))
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
            if (configManager.editListPlayer(nameOrUUID, false, type, null) || configManager.editListPlayer(UUID.fromString(nameOrUUID), false, type, null))
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
        catch (Exception e) {
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

    private static int playerToggleInfo(CommandContext<FabricClientCommandSource> ctx)
    {
        ConfigManager configManager = ConfigManager.getInstance();

        configManager.setEnableNameInfo(!configManager.getEnableNameInfo());
        chatMessage(ctx, ChatFormatting.GRAY, (configManager.getEnableNameInfo() ? "commands.selectium-mod.playerToggleInfo.enable"
                : "commands.selectium-mod.playerToggleInfo.disable"));
        return 1;
    }

    private static int resetList(CommandContext<FabricClientCommandSource> ctx)
    {
        ConfigManager.getInstance().resetList();
        chatMessage(ctx, ChatFormatting.GRAY, "commands.selectium-mod.resetList");
        return 1;
    }

    private static int showHelp(CommandContext<FabricClientCommandSource> ctx) {
        chatHeader(ctx, ChatFormatting.YELLOW,"commands.selectium-mod.showHelp.title");

        String[][] cmds = {
                {"/selectium listAdd <name/UUID>", "commands.selectium-mod.showHelp.playerAdd"},
                {"/selectium listRemove <name/UUID>", "commands.selectium-mod.showHelp.playerRemove"},
                {"/selectium nicknameAdd <name/UUID> <nickname>", "commands.selectium-mod.showHelp.playerNicknameEdit"},
                {"/selectium nicknameRemove <name/UUID>", "commands.selectium-mod.showHelp.playerNicknameRemove"},
                {"/selectium playerToggleInfo", "commands.selectium-mod.showHelp.playerToggleInfo"},
                {"/selectium reset", "commands.selectium-mod.showHelp.reset"},
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
