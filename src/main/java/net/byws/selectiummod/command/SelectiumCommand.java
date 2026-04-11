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

public class SelectiumCommand
{
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
            Minecraft mc = Minecraft.getInstance();
            if (mc.getConnection() != null)
            {
                for (PlayerInfo p: mc.getConnection().getOnlinePlayers())
                {
                    if (p.getProfile().getName().equals(nameOrUUID))
                    {
                        nameOrUUID = p.getProfile().getId().toString();
                        break;
                    }
                }
            }
            if (ConfigManger.addListPlayer(UUID.fromString(nameOrUUID), type))
            {
                chatMessage(ctx, ChatFormatting.GRAY, "commands.selectium-mod.addPlayer.added", nameOrUUID, type.name());
            }
            else
            {
                chatMessage(ctx, ChatFormatting.YELLOW, "commands.selectium-mod.addPlayer.replaced", nameOrUUID, type.name());
            }
        }
        catch (Exception e)
        {
            chatMessage(ctx, ChatFormatting.RED, "commands.selectium-mod.addPlayer.invalid");
        }

        return 1;
    }

    private static int removePlayer(CommandContext<FabricClientCommandSource> ctx, String nameOrUUID)
    {
        try
        {
            if (ConfigManger.removeListPlayer(nameOrUUID) || ConfigManger.removeListPlayer(UUID.fromString(nameOrUUID)))
            {
                chatMessage(ctx, ChatFormatting.GRAY, "commands.selectium-mod.removePlayer.removed", nameOrUUID);
            }
            else
            {
                chatMessage(ctx, ChatFormatting.RED, "commands.selectium-mod.removePlayer.failed", nameOrUUID);
            }
        }
        catch (Exception e)
        {
            chatMessage(ctx, ChatFormatting.RED, "commands.selectium-mod.removePlayer.invalid");
        }
        return 1;
    }

    public static int setNickname(CommandContext<FabricClientCommandSource> ctx, String nameOrUUID, String nickname)
    {
        try
        {
            if (ConfigManger.setPlayerNickname(nameOrUUID, nickname) || ConfigManger.setPlayerNickname(UUID.fromString(nameOrUUID), nickname))
            {
                if (nickname.isEmpty())
                {
                    chatMessage(ctx, ChatFormatting.GRAY, "commands.selectium-mod.setNickname.removed", nameOrUUID);
                }
                else
                {
                    chatMessage(ctx, ChatFormatting.GRAY, "commands.selectium-mod.setNickname.set", nameOrUUID, nickname);
                }
            }
            else
            {
                chatMessage(ctx, ChatFormatting.RED,"commands.selectium-mod.setNickname.failed", nameOrUUID);
            }
        }
        catch (Exception e)
        {
            chatMessage(ctx, ChatFormatting.RED, "commands.selectium-mod.setNickname.invalid");
        }
        return 1;
    }

//    private static int enableOutline(CommandContext<FabricClientCommandSource> ctx, boolean enable)
//    {
//        ConfigManger.setEnableOutline(enable);
//        chatMessage(ctx, "Player outlines " + (enable ? "shown" : "hidden"), ChatFormatting.GRAY);
//        return 1;
//    }

    private static int enableNameInfo(CommandContext<FabricClientCommandSource> ctx, boolean enable)
    {
        ConfigManger.setEnableNameInfo(enable);
        chatMessage(ctx, ChatFormatting.GRAY, (enable ? "commands.selectium-mod.enableNameInfo.enable" : "commands.selectium-mod.enableNameInfo.disable"));
        return 1;
    }

    private static int resetList(CommandContext<FabricClientCommandSource> ctx)
    {
        ConfigManger.resetList();
        chatMessage(ctx, ChatFormatting.GRAY, "commands.selectium-mod.resetList");
        return 1;
    }

//    public static int displayList(CommandContext<FabricClientCommandSource> ctx)
//    {
//        chatHeader(ctx, "Player List", ChatFormatting.GOLD);
//
//        String listPlayers[][] = {ConfigManger.getNamesList(), ConfigManger.getNameUUIDs()};
//
//        for (String[] player : listPlayers) {
//            ctx.getSource().sendFeedback(
//                    Component.literal("  " + player[0]).withStyle(ChatFormatting.YELLOW)
//                            .append(Component.literal("  — " + player[1]).withStyle(ChatFormatting.GRAY))
//            );
//        }
//
//        return 1;
//    }

    private static int showHelp(CommandContext<FabricClientCommandSource> ctx) {
        chatHeader(ctx, ChatFormatting.YELLOW,"commands.selectium-mod.showHelp.title");

        String[][] cmds = {
                {"/selectium listAdd <name/UUID>", "commands.selectium-mod.showHelp.listAdd"},
                {"/selectium listRemove <name/UUID>", "commands.selectium-mod.showHelp.listRemove"},
                {"/selectium nicknameAdd <name/UUID> <nickname>", "commands.selectium-mod.showHelp.nicknameAdd"},
                {"/selectium nicknameRemove <name/UUID>", "commands.selectium-mod.showHelp.nicknameRemove"},
                // {"/selectium showOutline <true/false>", "Enable or disable player outlines"},
                {"/selectium showNameInfo <true/false>", "commands.selectium-mod.showHelp.showNameInfo"},
                {"/selectium reset", "commands.selectium-mod.showHelp.reset"},
        };

        for (String[] cmd : cmds) {
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
