package net.byws.selectiummod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.byws.selectiummod.config.PlayerListConfig;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.UUID;

public class SelectiumCommand {

    // ── Suggestions ────────────────────────────────────────────────────────

    /** Suggests names already in the player list. */
    private static final SuggestionProvider<FabricClientCommandSource> LIST_PLAYERS =
            (ctx, builder) ->
            {
                for (UUID p : PlayerListConfig.getPvtList().keySet())
                {
                    builder.suggest(p.toString());
                }
                return builder.buildFuture();
            };

    // ── Registration ───────────────────────────────────────────────────────

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher)
    {

        dispatcher.register(
                ClientCommandManager.literal("selectium")

                        // /add <name>
                        .then(ClientCommandManager.literal("add")
                                .then(ClientCommandManager.literal("ally")
                                    .then(ClientCommandManager.argument("UUID", StringArgumentType.word())
                                            .executes(ctx -> addAllyPlayer(ctx, StringArgumentType.getString(ctx, "UUID")))))
                                .then(ClientCommandManager.literal("target")
                                        .then(ClientCommandManager.argument("UUID", StringArgumentType.word())
                                                .executes(ctx -> addTargetPlayer(ctx, StringArgumentType.getString(ctx, "UUID"))))))

                        // /remove <name>
                        .then(ClientCommandManager.literal("remove")
                                .then(ClientCommandManager.argument("UUID", StringArgumentType.word())
                                        .suggests(LIST_PLAYERS)
                                        .executes(ctx -> removePlayer(ctx, StringArgumentType.getString(ctx, "UUID")))))

                        // /help  (also default)
                        .then(ClientCommandManager.literal("help")
                                .executes(SelectiumCommand::showHelp))

                        .executes(SelectiumCommand::showHelp)
        );
    }

    // ── Handlers ───────────────────────────────────────────────────────────

    private static int addAllyPlayer(CommandContext<FabricClientCommandSource> ctx, String stringUUID)
    {
        try
        {
            UUID playerUUID = UUID.fromString(stringUUID);
            if (PlayerListConfig.addPvtList(playerUUID, PlayerListConfig.Category.ALLY))
            {
                chatMessage(ctx,playerUUID + " added as ally to personal list.", Formatting.GRAY);
            }
            else
            {
                chatMessage(ctx, playerUUID + " replaced as ally in personal list.", Formatting.YELLOW);
            }
        }
        catch (Exception e)
        {
            chatMessage(ctx, "invalid UUID.", Formatting.RED);
        }

        return 1;
    }

    private static int addTargetPlayer(CommandContext<FabricClientCommandSource> ctx, String stringUUID)
    {
        try
        {
            UUID playerUUID = UUID.fromString(stringUUID);
            if (PlayerListConfig.addPvtList(playerUUID, PlayerListConfig.Category.TARGET))
            {
                chatMessage(ctx,playerUUID + " added as target to personal list.", Formatting.GRAY);
            }
            else
            {
                chatMessage(ctx, playerUUID + " replaced as target in personal list.", Formatting.YELLOW);
            }
        }
        catch (Exception e)
        {
            chatMessage(ctx, "invalid UUID.", Formatting.RED);
        }

        return 1;
    }

    private static int removePlayer(CommandContext<FabricClientCommandSource> ctx, String stringUUID)
    {
        try {
            UUID playerUUID = UUID.fromString(stringUUID);
            if (PlayerListConfig.removePvtList(playerUUID)) {
                chatMessage(ctx, playerUUID + " removed from personal list.", Formatting.GRAY);
            } else {
                chatMessage(ctx, playerUUID + " was not in your personal list.", Formatting.RED);
            }
        }
        catch (Exception e)
        {
            chatMessage(ctx, "invalid UUID.", Formatting.RED);
        }
        return 1;
    }

    private static int showHelp(CommandContext<FabricClientCommandSource> ctx) {
        chatHeader(ctx, "Selectium Mod — Commands", Formatting.YELLOW);

        String[][] cmds = {
                {"/selectium add <UUID>",         "Add player to list"},
                {"/selectium remove <UUID>",      "Remove player from list"},
        };

        for (String[] cmd : cmds) {
            ctx.getSource().sendFeedback(
                    Text.literal("  " + cmd[0])
                            .formatted(Formatting.YELLOW)
                            .append(Text.literal("  — " + cmd[1])
                                    .formatted(Formatting.GRAY))
            );
        }
        return 1;
    }

    // ── Formatting helpers ─────────────────────────────────────────────────

    private static void chatHeader(CommandContext<FabricClientCommandSource> ctx, String title, Formatting color)
    {
        ctx.getSource().sendFeedback(
                Text.literal("━━━ ")
                .formatted(Formatting.GOLD)
                .append(Text.literal(title).formatted(color, Formatting.BOLD))
                .append(Text.literal(" ━━━").formatted(Formatting.GOLD))
        );
    }

    private static void chatMessage(CommandContext<FabricClientCommandSource> ctx, String message, Formatting color)
    {
        ctx.getSource().sendFeedback(
                Text.literal("[Selectium] ")
                        .formatted(Formatting.GOLD)
                        .append(Text.literal(message).formatted(color))
        );
    }
}
