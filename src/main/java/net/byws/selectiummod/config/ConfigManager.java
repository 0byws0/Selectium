package net.byws.selectiummod.config;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import com.google.gson.*;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class ConfigManager
{
    private static ConfigManager INSTANCE;
    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("config-selectium-mod.json");
    private final Logger LOGGER;
    private final Gson GSON;
    
    private ConfigJsonObj config;

    private ConfigManager(Logger modLogger, Gson gson)
    {
        this.LOGGER = modLogger;
        this.GSON = gson;
        this.config = new ConfigJsonObj();
    }

    public static synchronized void init(final Logger modLogger, final Gson gson)
    {
        if (INSTANCE == null)
        {
            INSTANCE = new ConfigManager(modLogger, gson);
            INSTANCE.load();
        }
    }

    private void load()
    {
        try
        {
            if (!Files.exists(CONFIG_PATH)) save();
            config = GSON.fromJson(Files.readString(CONFIG_PATH), ConfigJsonObj.class);
        }
        catch (IOException e)
        {
            LOGGER.error("Selectium Mod failed to load config: {}", e.getMessage());
        }
    }

    private void save()
    {
        try (FileWriter fw = new FileWriter(CONFIG_PATH.toFile()))
        {
            fw.write(GSON.toJson(config));
        }
        catch (IOException e)
        {
            LOGGER.error("Selectium Mod failed to save config: {}", e.getMessage());
        }
    }

    public static ConfigManager getInstance()
    {
        if (INSTANCE == null) throw new IllegalStateException("ConfigManager not initialized");
        return INSTANCE;
    }

    public String[] getNamesList() { return config.playerNameList.keySet().toArray(new String[0]); }

    public boolean getEnableNameInfo() { return config.enableNameColor; }

    public ConfigJsonObj.PlayerData getPlayerData(String playerName) { return getPlayerData(config.playerNameList.get(playerName)); }

    public ConfigJsonObj.PlayerData getPlayerData(UUID playerUUID) { return config.playerCatList.get(playerUUID); }

    public void updatePlayerData()
    {
        var connection = Minecraft.getInstance().getConnection();
        if (connection == null) return;

        boolean requiresSave = false;

        for (var playerObj : connection.getOnlinePlayers())
        {
            UUID playerUUID = playerObj.getProfile().getId();
            String playerName = playerObj.getProfile().getName();
            ConfigJsonObj.PlayerData player = config.playerCatList.get(playerUUID);

            if (player == null)
            {
                player = config.playerCatList.get(config.playerNameList.get(playerName));
            }
            else if (!playerUUID.equals(config.playerNameList.get(playerName)))
            {
                config.playerNameList.remove(config.getNameFromList(playerUUID));
                config.playerNameList.put(playerName, playerUUID);
                requiresSave = true;
            }

            if (player != null && player.nickname.isEmpty())
            {
                player.nickname = playerName;
                requiresSave = true;
            }
        }

        if (requiresSave) save();
    }

    public boolean addListPlayer(UUID playerUUID, boolean shared, ConfigJsonObj.Category type, String nickname)
    {
        if (config.playerCatList.putIfAbsent(playerUUID, new ConfigJsonObj.PlayerData(shared, type, nickname)) == null)
        {
            save();
            return true;
        }
        return false;
    }

    public boolean editListPlayer(String playerName, Boolean shared, ConfigJsonObj.Category type, String nickname) { return editListPlayer(config.playerNameList.get(playerName), shared, type, nickname); }

    public boolean editListPlayer(UUID playerUUID, Boolean shared, ConfigJsonObj.Category type, String nickname)
    {
        ConfigJsonObj.PlayerData player = config.playerCatList.get(playerUUID);
        if (player != null)
        {
            if (shared != null) player.shared = shared;
            if (type != null) player.type = type;
            if (nickname != null) player.nickname = nickname;
            save();
            return true;
        }
        return false;
    }

    public boolean removeListPlayer(String playerName) { return removeListPlayer(config.playerNameList.get(playerName)); }

    public boolean removeListPlayer(UUID playerUUID)
    {
        if (config.playerCatList.remove(playerUUID) != null | config.playerNameList.remove(config.getNameFromList(playerUUID)) != null)
        {
            save();
            return true;
        }
        return false;
    }

    public void resetList()
    {
        config.playerCatList.clear();
        config.playerNameList.clear();
        save();
    }

    public void setEnableNameInfo(boolean enable)
    {
        config.enableNameColor = enable;
        save();
    }
}
