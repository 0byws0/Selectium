package net.byws.selectiummod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
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

                        // /add <UUID>
                        .then(ClientCommandManager.literal("listAdd")
                                .then(ClientCommandManager.literal("ALLY")
                                    .then(ClientCommandManager.argument("name/UUID", StringArgumentType.word())
                                            .suggests(ONLINE_PLAYER_NAMES)
                                            .executes(ctx -> addPlayer(ctx, StringArgumentType.getString(ctx, "name/UUID"), ConfigManger.Category.ALLY))))
                                .then(ClientCommandManager.literal("NEUTRAL")
                                        .then(ClientCommandManager.argument("name/UUID", StringArgumentType.word())
                                                .suggests(ONLINE_PLAYER_NAMES)
                                                .executes(ctx -> addPlayer(ctx, StringArgumentType.getString(ctx, "name/UUID"), ConfigManger.Category.NEUTRAL))))
                                .then(ClientCommandManager.literal("TARGET")
                                        .then(ClientCommandManager.argument("name/UUID", StringArgumentType.word())
                                                .suggests(ONLINE_PLAYER_NAMES)
                                                .executes(ctx -> addPlayer(ctx, StringArgumentType.getString(ctx, "name/UUID"), ConfigManger.Category.TARGET)))))

                        // /remove <UUID>
                        .then(ClientCommandManager.literal("listRemove")
                                .then(ClientCommandManager.argument("UUID", StringArgumentType.word())
                                        .suggests(PLAYER_LIST_NAMES)
                                        .executes(ctx -> removePlayer(ctx, StringArgumentType.getString(ctx, "UUID")))))

                        // /showOutline <Boolean>
                        .then(ClientCommandManager.literal("showOutline")
                                .then(ClientCommandManager.argument("true/false", BoolArgumentType.bool())
                                        .executes(ctx -> enableOutline(ctx, BoolArgumentType.getBool(ctx, "true/false")))))

                        // /showNameColor <Boolean>
                        .then(ClientCommandManager.literal("showNameColor")
                                .then(ClientCommandManager.argument("true/false", BoolArgumentType.bool())
                                        .executes(ctx -> enableNameColor(ctx, BoolArgumentType.getBool(ctx, "true/false")))))

                        .then(ClientCommandManager.literal("reset")
                                .executes(SelectiumCommand::resetList))

                        // /help  (also default)
                        .then(ClientCommandManager.literal("help")
                                .executes(SelectiumCommand::showHelp))

                        .executes(SelectiumCommand::showHelp)
        );
    }

    // ── Handlers ───────────────────────────────────────────────────────────

    private static int addPlayer(CommandContext<FabricClientCommandSource> ctx, String input, ConfigManger.Category type)
    {
        try
        {
            Minecraft mc = Minecraft.getInstance();
            if (mc.getConnection() != null)
            {
                for (PlayerInfo p: mc.getConnection().getOnlinePlayers())
                {
                    if (p.getProfile().getName().equals(input))
                    {
                        input = p.getProfile().getId().toString();
                        break;
                    }
                }
            }
            if (ConfigManger.addListPlayer(UUID.fromString(input), type))
            {
                chatMessage(ctx,input + " added as " + type.name() + " to personal list", ChatFormatting.GRAY);
            }
            else
            {
                chatMessage(ctx, input + " replaced as " + type.name() + " in personal list", ChatFormatting.YELLOW);
            }
        }
        catch (Exception e)
        {
            chatMessage(ctx, "Invalid UUID", ChatFormatting.RED);
        }

        return 1;
    }

    private static int removePlayer(CommandContext<FabricClientCommandSource> ctx, String input)
    {
        try
        {
            if (ConfigManger.removeListPlayer(input) || ConfigManger.removeListPlayer(UUID.fromString(input)))
            {
                chatMessage(ctx, input + " removed from personal list", ChatFormatting.GRAY);
            }
            else
            {
                chatMessage(ctx, input + " was not in your personal list", ChatFormatting.RED);
            }
        }
        catch (Exception e)
        {
            chatMessage(ctx, "Invalid UUID", ChatFormatting.RED);
        }
        return 1;
    }

    private static int enableOutline(CommandContext<FabricClientCommandSource> ctx, boolean enable)
    {
        ConfigManger.setEnableOutline(enable);
        chatMessage(ctx, "Player outlines " + ((enable) ? "shown" : "hidden"), ChatFormatting.GRAY);
        return 1;
    }

    private static int enableNameColor(CommandContext<FabricClientCommandSource> ctx, boolean enable)
    {
        ConfigManger.setEnableNameColor(enable);
        chatMessage(ctx, "Player name colors " + ((enable) ? "shown" : "hidden"), ChatFormatting.GRAY);
        return 1;
    }

    private static int resetList(CommandContext<FabricClientCommandSource> ctx)
    {
        ConfigManger.resetList();
        chatMessage(ctx, "Player list reset", ChatFormatting.GRAY);
        return 1;
    }

    private static int showHelp(CommandContext<FabricClientCommandSource> ctx) {
        chatHeader(ctx, "Selectium Mod — Commands", ChatFormatting.YELLOW);

        String[][] cmds = {
                {"/selectium listAdd <UUID>", "Add or replace player on list"},
                {"/selectium listRemove <UUID>", "Remove player from list"},
                {"/selectium showOutline <true/false>", "Enable or disable player outlines"},
                {"/selectium showNameColor <true/false>", "Enable or disable player name colors"},
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
