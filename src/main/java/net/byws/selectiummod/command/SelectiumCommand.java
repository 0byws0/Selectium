package net.byws.selectiummod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.byws.selectiummod.config.ConfigJsonObj;
import net.byws.selectiummod.config.ConfigManger;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import java.util.UUID;

public class SelectiumCommand {
    // ── Suggestions ────────────────────────────────────────────────────────
    private static final SuggestionProvider<FabricClientCommandSource> ONLINE_PLAYER_NAMES =
            (ctx, builder) ->
            {
                Minecraft mc = Minecraft.getInstance();
                if (mc.getConnection() != null)
                {
                    mc.getConnection().getOnlinePlayers().stream()
                            .map(entry -> entry.getProfile().getName())
                            .forEach(builder::suggest);
                }
                return builder.buildFuture();
            };
    /** Suggests names already in the player list. */
    private static final SuggestionProvider<FabricClientCommandSource> PLAYER_LIST_NAMES =
            (ctx, builder) ->
            {
                for (String name : ConfigManger.getNamesList()) builder.suggest(name);
                return builder.buildFuture();
            };

    // ── Registration ───────────────────────────────────────────────────────

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher)
    {

        dispatcher.register(
                ClientCommandManager.literal("selectium")

                        // /listAdd <name/UUID>
                        .then(ClientCommandManager.literal("listAdd")
                                .then(ClientCommandManager.literal("ALLY")
                                    .then(ClientCommandManager.argument("name/UUID", StringArgumentType.word())
                                            .suggests(ONLINE_PLAYER_NAMES)
                                            .executes(ctx -> addPlayer(ctx, StringArgumentType.getString(ctx, "name/UUID"), ConfigJsonObj.Category.ALLY))))
                                .then(ClientCommandManager.literal("NEUTRAL")
                                        .then(ClientCommandManager.argument("name/UUID", StringArgumentType.word())
                                                .suggests(ONLINE_PLAYER_NAMES)
                                                .executes(ctx -> addPlayer(ctx, StringArgumentType.getString(ctx, "name/UUID"), ConfigJsonObj.Category.NEUTRAL))))
                                .then(ClientCommandManager.literal("TARGET")
                                        .then(ClientCommandManager.argument("name/UUID", StringArgumentType.word())
                                                .suggests(ONLINE_PLAYER_NAMES)
                                                .executes(ctx -> addPlayer(ctx, StringArgumentType.getString(ctx, "name/UUID"), ConfigJsonObj.Category.TARGET)))))

                        // /listRemove <name/UUID>
                        .then(ClientCommandManager.literal("listRemove")
                                .then(ClientCommandManager.argument("name/UUID", StringArgumentType.word())
                                        .suggests(PLAYER_LIST_NAMES)
                                        .executes(ctx -> removePlayer(ctx, StringArgumentType.getString(ctx, "name/UUID")))))

                        // /addNickname <name/UUID> <nickname>
                        .then(ClientCommandManager.literal("nicknameAdd")
                                .then(ClientCommandManager.argument("name/UUID", StringArgumentType.word())
                                        .suggests(PLAYER_LIST_NAMES)
                                        .then(ClientCommandManager.argument("nickname", StringArgumentType.word())
                                                .executes(ctx -> setNickname(ctx, StringArgumentType.getString(ctx, "name/UUID"), StringArgumentType.getString(ctx, "nickname"))))))

                        // /removeNickname <name/UUID>
                        .then(ClientCommandManager.literal("nicknameRemove")
                                .then(ClientCommandManager.argument("name/UUID", StringArgumentType.word())
                                        .suggests(PLAYER_LIST_NAMES)
                                        .executes(ctx -> setNickname(ctx, StringArgumentType.getString(ctx, "name/UUID"), ""))))

                        // /showOutline <Boolean>
//                        .then(ClientCommandManager.literal("showOutline")
//                                .then(ClientCommandManager.argument("true/false", BoolArgumentType.bool())
//                                        .executes(ctx -> enableOutline(ctx, BoolArgumentType.getBool(ctx, "true/false")))))

                        // /showNameColor <Boolean>
                        .then(ClientCommandManager.literal("showNameInfo")
                                .then(ClientCommandManager.argument("true/false", BoolArgumentType.bool())
                                        .executes(ctx -> enableNameInfo(ctx, BoolArgumentType.getBool(ctx, "true/false")))))

                        .then(ClientCommandManager.literal("reset")
                                .executes(SelectiumCommand::resetList))

                        // /help  (also default)
                        .then(ClientCommandManager.literal("help")
                                .executes(SelectiumCommand::showHelp))

                        .executes(SelectiumCommand::showHelp)
        );
    }

    // ── Handlers ───────────────────────────────────────────────────────────

    private static int addPlayer(CommandContext<FabricClientCommandSource> ctx, String nameOrUUID, ConfigJsonObj.Category type)
    {
        try
        {
            for (PlayerInfo p: Minecraft.getInstance().getConnection().getOnlinePlayers())
            {
                if (p.getProfile().getName().equals(nameOrUUID))
                {
                    nameOrUUID = p.getProfile().getId().toString();
                    break;
                }
            }
            if (ConfigManger.addListPlayer(UUID.fromString(nameOrUUID), type))
            {
                chatMessage(ctx,nameOrUUID + " added as " + type.name() + " to list", ChatFormatting.GRAY);
            }
            else
            {
                chatMessage(ctx, nameOrUUID + " replaced as " + type.name() + " in list", ChatFormatting.YELLOW);
            }
        }
        catch (Exception e)
        {
            chatMessage(ctx, "Invalid UUID", ChatFormatting.RED);
        }

        return 1;
    }

    private static int removePlayer(CommandContext<FabricClientCommandSource> ctx, String nameOrUUID)
    {
        try
        {
            if (ConfigManger.removeListPlayer(nameOrUUID) || ConfigManger.removeListPlayer(UUID.fromString(nameOrUUID)))
            {
                chatMessage(ctx, nameOrUUID + " removed from list", ChatFormatting.GRAY);
            }
            else
            {
                chatMessage(ctx, nameOrUUID + " was not in list", ChatFormatting.RED);
            }
        }
        catch (Exception e)
        {
            chatMessage(ctx, "Invalid UUID", ChatFormatting.RED);
        }
        return 1;
    }

    public static int setNickname(CommandContext<FabricClientCommandSource> ctx, String nameOrUUID, String nickname)
    {
        try
        {
            if (ConfigManger.setPlayerNickname(nameOrUUID, nickname) || ConfigManger.setPlayerNickname(UUID.fromString(nameOrUUID), nickname))
            {
                chatMessage(ctx, nameOrUUID + (nickname.isEmpty() ? " nickname removed" : " nickname set to " + nickname), ChatFormatting.GRAY);
            }
            else
            {
                chatMessage(ctx, nameOrUUID + " was not in list", ChatFormatting.RED);
            }
        }
        catch (Exception e)
        {
            chatMessage(ctx, "Invalid UUID or not in list", ChatFormatting.RED);
        }
        return 1;
    }

    private static int enableOutline(CommandContext<FabricClientCommandSource> ctx, boolean enable)
    {
        ConfigManger.setEnableOutline(enable);
        chatMessage(ctx, "Player outlines " + (enable ? "shown" : "hidden"), ChatFormatting.GRAY);
        return 1;
    }

    private static int enableNameInfo(CommandContext<FabricClientCommandSource> ctx, boolean enable)
    {
        ConfigManger.setEnableNameInfo(enable);
        chatMessage(ctx, "Player name information " + (enable ? "shown" : "hidden"), ChatFormatting.GRAY);
        return 1;
    }

    private static int resetList(CommandContext<FabricClientCommandSource> ctx)
    {
        ConfigManger.resetList();
        chatMessage(ctx, "Player list reset", ChatFormatting.GRAY);
        return 1;
    }

    public static int displayList(CommandContext<FabricClientCommandSource> ctx)
    {
        chatHeader(ctx, "Player List", ChatFormatting.GOLD);

        String listPlayers[][] = {ConfigManger.getNamesList(), ConfigManger.getNameUUIDs()};

        for (String[] player : listPlayers) {
            ctx.getSource().sendFeedback(
                    Component.literal("  " + player[0]).withStyle(ChatFormatting.YELLOW)
                            .append(Component.literal("  — " + player[1]).withStyle(ChatFormatting.GRAY))
            );
        }

        return 1;
    }

    private static int showHelp(CommandContext<FabricClientCommandSource> ctx) {
        chatHeader(ctx, "Selectium Mod — Commands", ChatFormatting.YELLOW);

        String[][] cmds = {
                {"/selectium listAdd <name/UUID>", "Add or replace player on list"},
                {"/selectium listRemove <name/UUID>", "Remove player from list"},
                {"/selectium nicknameAdd <name/UUID> <nickname>", "Add or replace nickname of player on list"},
                {"/selectium nicknameRemove <name/UUID>", "Remove nickname of player on list"},
                // {"/selectium showOutline <true/false>", "Enable or disable player outlines"},
                {"/selectium showNameInfo <true/false>", "Enable or disable player name colors and nicknames"},
                {"/selectium reset", "Reset list to default"},
        };

        for (String[] cmd : cmds) {
            ctx.getSource().sendFeedback(
                    Component.literal("  " + cmd[0]).withStyle(ChatFormatting.YELLOW)
                            .append(Component.literal("  — " + cmd[1]).withStyle(ChatFormatting.GRAY))
            );
        }
        return 1;
    }

    // ── Formatting helpers ─────────────────────────────────────────────────

    private static void chatHeader(CommandContext<FabricClientCommandSource> ctx, String title, ChatFormatting color)
    {
        ctx.getSource().sendFeedback(
                Component.literal("━━━ ")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal(title).withStyle(color, ChatFormatting.BOLD))
                .append(Component.literal(" ━━━").withStyle(ChatFormatting.GOLD))
        );
    }

    private static void chatMessage(CommandContext<FabricClientCommandSource> ctx, String message, ChatFormatting color)
    {
        ctx.getSource().sendFeedback(
                Component.literal("[Selectium] ")
                        .withStyle(ChatFormatting.GOLD)
                        .append(Component.literal(message).withStyle(color))
        );
    }
}
