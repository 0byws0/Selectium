package net.byws.selectiummod.config;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import org.slf4j.Logger;
import com.google.gson.*;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class ConfigManager
{
    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("config-selectium-mod.json");
    private static ConfigJsonObj CONFIG;
    private static Gson GSON = new Gson();
    public static Logger LOGGER;

    private static void load()
    {
        try {
            if (!Files.exists(CONFIG_PATH)) save();
            CONFIG = GSON.fromJson(Files.readString(CONFIG_PATH), ConfigJsonObj.class);
        }
        catch (IOException e)
        {
            LOGGER.error("Selectium Mod failed to load config: {}", e.getMessage());
        }
    }

    private static void save()
    {
        try (FileWriter fw = new FileWriter(CONFIG_PATH.toFile()))
        {
            fw.write(GSON.toJson(CONFIG));
        }
        catch (IOException e)
        {
            LOGGER.error("Selectium Mod failed to save config: {}", e.getMessage());
        }
    }

    public static void init(final Logger modLogger)
    {
        LOGGER = modLogger;
        CONFIG = new ConfigJsonObj();
        load();
    }

    public static void resetList()
    {
        CONFIG.playerCatList.clear();
        CONFIG.playerNameList.clear();
        save();
    }

    public static ChatFormatting getPlayerColorFormatting(ConfigJsonObj.Category playerType)
    {
        return switch(playerType)
        {
            case null -> null;
            case ALLY -> ChatFormatting.GREEN;
            case NEUTRAL -> ChatFormatting.YELLOW;
            case TARGET -> ChatFormatting.RED;
        };
    }

    public static String[] getNamesList() { return CONFIG.playerNameList.keySet().toArray(new String[0]); }

    public static boolean getEnableNameInfo() { return CONFIG.enableNameColor; }

    public static ConfigJsonObj.PlayerData getPlayerData(String playerName, UUID playerUUID)
    {
        ConfigJsonObj.PlayerData player = CONFIG.playerCatList.get(playerUUID);
        boolean saveToFile = false;

        if (player == null)
        {
            player = CONFIG.playerCatList.get(CONFIG.playerNameList.get(playerName));
        }
        else if (CONFIG.playerNameList.get(playerName) != playerUUID)
        {
            CONFIG.playerNameList.remove(CONFIG.getNameFromList(playerUUID));
            CONFIG.playerNameList.put(playerName, playerUUID);
            saveToFile = true;
        }

        if (player != null && player.nickname.isEmpty())
        {
            player.nickname = playerName;
            saveToFile = true;
        }

        if (saveToFile) save();

        return player;
    }

    public static boolean addListPlayer(UUID playerUUID, ConfigJsonObj.Category type)
    {
        if (CONFIG.playerCatList.putIfAbsent(playerUUID, new ConfigJsonObj.PlayerData(false, type, "")) == null)
        {
            save();
            return true;
        }
        return false;
    }

    public static boolean editListPlayer(String playerName, ConfigJsonObj.Category type) { return editListPlayer(CONFIG.playerNameList.get(playerName), type); }

    public static boolean editListPlayer(UUID playerUUID, ConfigJsonObj.Category type)
    {
        ConfigJsonObj.PlayerData player = CONFIG.playerCatList.get(playerUUID);
        if (player != null)
        {
            player.shared = false;
            player.type = type;
            save();
            return true;
        }
        return false;
    }

    public static boolean removeListPlayer(String playerName) { return removeListPlayer(CONFIG.playerNameList.get(playerName)); }

    public static boolean removeListPlayer(UUID playerUUID)
    {
        if (CONFIG.playerCatList.remove(playerUUID) != null | CONFIG.playerNameList.remove(CONFIG.getNameFromList(playerUUID)) != null)
        {
            save();
            return true;
        }
        return false;
    }

    public static boolean setPlayerNickname(String playerName, String playerNickname) { return setPlayerNickname(CONFIG.playerNameList.get(playerName), playerNickname); }

    public static boolean setPlayerNickname(UUID playerUUID, String playerNickname)
    {
        ConfigJsonObj.PlayerData player = CONFIG.playerCatList.get(playerUUID);
        if (player != null)
        {
            player.nickname = playerNickname;
            return true;
        }
        return false;
    }

    public static void setEnableNameInfo(boolean enable)
    {
        CONFIG.enableNameColor = enable;
        save();
    }
}
