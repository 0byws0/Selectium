package net.byws.selectiummod.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Formatting;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;

public class PlayerListConfig
{
//    private static final Path CONFIG_FILE =
//            FabricLoader.getInstance().getConfigDir().resolve("config-selectium-mod.json");

    public enum Category { ALLY, TARGET }
    private static HashMap<UUID, Category> playerList = new HashMap<>();

    public static void load()
    {
        try
        {
//            String configContent = Files.readString(CONFIG_FILE);
//            playerList.clear();

        } catch (Exception e) {

        }
        playerList.put(UUID.fromString("0422ee86-9842-43dd-8fd7-3665aeb87552"), Category.TARGET);
    }

    public static Integer getPlayerColorRGBA(UUID playerUUID)
    {
        return switch(playerList.get(playerUUID))
        {
            case null -> null;
            case ALLY -> 0xFF00FF00;
            case TARGET -> 0xFFFF0000;
        };
    }

    public static Formatting getPlayerColorFormatting(UUID playerUUID)
    {
        return switch(playerList.get(playerUUID))
        {
            case null -> null;
            case ALLY -> Formatting.GREEN;
            case TARGET -> Formatting.RED;
        };
    }
}
