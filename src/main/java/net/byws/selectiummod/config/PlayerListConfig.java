package net.byws.selectiummod.config;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Formatting;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;

public class PlayerListConfig
{
    private static final Path CONFIG_FILE =
            FabricLoader.getInstance().getConfigDir().resolve("config-selectium-mod.json");

    public enum Category { ALLY, TARGET }

    private static class ConfigFileContents
    {
        private HashMap<UUID, Category> genPlayerList = new HashMap<>();
        private HashMap<UUID, Category> perPlayerList = new HashMap<>();
    }

    private static ConfigFileContents config = new ConfigFileContents();

    public static void load()
    {
        try
        {
            if (!Files.exists(CONFIG_FILE)) save();

            Gson gson = new Gson();

            config = gson.fromJson(Files.readString(CONFIG_FILE), ConfigFileContents.class);
        }
        catch (Exception e)
        {

        }
    }

    public static void save()
    {
        Gson gson = new Gson();

        try (FileWriter fw = new FileWriter(CONFIG_FILE.toFile())) {
            fw.write(gson.toJson(config));
        }
        catch (Exception e)
        {

        }
    }

    private static Category checkList(UUID playerUUID)
    {
        Category playerCat = config.genPlayerList.get(playerUUID);
        if (playerCat == null) playerCat = config.perPlayerList.get(playerUUID);
        return playerCat;
    }

    public static Integer getPlayerColorRGBA(UUID playerUUID)
    {
        return switch(checkList(playerUUID))
        {
            case null -> null;
            case ALLY -> 0xFF00FF00;
            case TARGET -> 0xFFFF0000;
        };
    }

    public static Formatting getPlayerColorFormatting(UUID playerUUID)
    {
        return switch(checkList(playerUUID))
        {
            case null -> null;
            case ALLY -> Formatting.GREEN;
            case TARGET -> Formatting.RED;
        };
    }

    public static UUID[] getPerList() { return config.perPlayerList.keySet().toArray(new UUID[0]); }

    public static boolean addPerList(UUID playerUUID, Category type)
    {
        boolean containsPlayer = (config.perPlayerList.put(playerUUID, type) != null);
        save();
        return !containsPlayer;
    }

    public static boolean removePerList(UUID playerUUID)
    {
        boolean containsPlayer = (config.perPlayerList.remove(playerUUID) != null);
        save();
        return containsPlayer;
    }

    public static void clearPerList()
    {
        config.perPlayerList.clear();
        save();
    }
}
