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

    /** Suggests names already in the partner list. */
    private static final SuggestionProvider<FabricClientCommandSource> KNOWN_PLAYERS =
            (ctx, builder) -> {
                for (UUID p : PlayerListConfig.getPerList())
                {
                    builder.suggest(p.toString());
                }
                return builder.buildFuture();
            };

    // ── Registration ───────────────────────────────────────────────────────

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {

        dispatcher.register(
                ClientCommandManager.literal("selectium")

                        // /partner add <name>
                        .then(ClientCommandManager.literal("addAlly")
                                .then(ClientCommandManager.argument("UUID", StringArgumentType.word())
                                        .executes(ctx -> addAllyPlayer(ctx, StringArgumentType.getString(ctx, "UUID")))))

                        .then(ClientCommandManager.literal("addTarget")
                                .then(ClientCommandManager.argument("UUID", StringArgumentType.word())
                                        .executes(ctx -> addTargetPlayer(ctx, StringArgumentType.getString(ctx, "UUID")))))

                        // /partner remove <name>
                        .then(ClientCommandManager.literal("remove")
                                .then(ClientCommandManager.argument("UUID", StringArgumentType.word())
                                        .suggests(KNOWN_PLAYERS)
                                        .executes(ctx -> removePlayer(ctx, StringArgumentType.getString(ctx, "UUID")))))

                        // /partner clear
                        .then(ClientCommandManager.literal("clear")
                                .executes(SelectiumCommand::clearPlayers))

                        // /partner help  (also default)
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
            if (PlayerListConfig.addPerList(playerUUID, PlayerListConfig.Category.ALLY))
            {
                reply(ctx, Formatting.GRAY,playerUUID + " added as ally to personal list.");
            }
            else
            {
                reply(ctx, Formatting.YELLOW, playerUUID + " replaced as ally in personal list.");
            }
        }
        catch (Exception e)
        {
            reply(ctx, Formatting.RED, "invalid UUID.");
        }

        return 1;
    }

    private static int addTargetPlayer(CommandContext<FabricClientCommandSource> ctx, String stringUUID) {
        try
        {
            UUID playerUUID = UUID.fromString(stringUUID);
            if (PlayerListConfig.addPerList(playerUUID, PlayerListConfig.Category.TARGET))
            {
                reply(ctx, Formatting.GRAY,playerUUID + " added as target to personal list.");
            }
            else
            {
                reply(ctx, Formatting.YELLOW, playerUUID + " replaced as target in personal list.");
            }
        }
        catch (Exception e)
        {
            reply(ctx, Formatting.RED, "invalid UUID.");
        }

        return 1;
    }

    private static int removePlayer(CommandContext<FabricClientCommandSource> ctx, String stringUUID) {
        try {
            UUID playerUUID = UUID.fromString(stringUUID);
            if (PlayerListConfig.removePerList(playerUUID)) {
                reply(ctx, Formatting.GRAY, playerUUID + " removed from personal list.");
            } else {
                reply(ctx, Formatting.RED, playerUUID + " was not in your personal list.");
            }
        }
        catch (Exception e)
        {
            reply(ctx, Formatting.RED, "invalid UUID.");
        }
        return 1;
    }

    private static int clearPlayers(CommandContext<FabricClientCommandSource> ctx) {
        PlayerListConfig.clearPerList();
        reply(ctx, Formatting.GRAY, "removed all players from personal list.");
        return 1;
    }

    private static int showHelp(CommandContext<FabricClientCommandSource> ctx) {
        ctx.getSource().sendFeedback(header("Selectium Mod — Commands"));

        String[][] cmds = {
                {"/partner add <name>",         "Mark a player as a partner"},
                {"/partner remove <name>",      "Un-mark a partner"},
                {"/partner list",               "Show all partners & settings"},
                {"/partner clear",              "Remove all partners"},
                {"/partner color <RRGGBB>",     "Change highlight colour"},
                {"/partner particles <on|off>", "Toggle floating particles"},
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

    private static MutableText header(String title) {
        return Text.literal("━━━ ")
                .formatted(Formatting.GOLD)
                .append(Text.literal(title).formatted(Formatting.YELLOW, Formatting.BOLD))
                .append(Text.literal(" ━━━").formatted(Formatting.GOLD));
    }

    private static void reply(CommandContext<FabricClientCommandSource> ctx,
                              Formatting color, String message) {
        ctx.getSource().sendFeedback(
                Text.literal("[Selectium] ")
                        .formatted(Formatting.GOLD)
                        .append(Text.literal(message).formatted(color))
        );
    }
}
