package net.byws.selectiummod.config;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import com.google.gson.*;
import org.apache.commons.lang3.tuple.Pair;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
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

    public Pair<Boolean, ConfigJsonObj.PlayerData> getPlayerData(String playerName) { return getPlayerData(config.playerNameList.get(playerName)); }

    public Pair<Boolean, ConfigJsonObj.PlayerData> getPlayerData(UUID playerUUID)
    {
        ConfigJsonObj.PlayerData player = config.clientPlayerList.get(playerUUID);
        if (player != null) return Pair.of(true, player);
        return Pair.of(false, (playerUUID == null) ? null : config.serverPlayerList.get(playerUUID));
    }

    public boolean getEnableNameInfo() { return config.enableNameColor; }

    public void updatePlayerData()
    {
        var connection = Minecraft.getInstance().getConnection();
        if (connection == null) return;

        boolean requiresSave = false;

        for (var playerObj : connection.getOnlinePlayers())
        {
            UUID playerUUID = playerObj.getProfile().getId();
            String playerName = playerObj.getProfile().getName();

            ConfigJsonObj.PlayerData player = getPlayerData(playerUUID).getRight();

            if (player == null)
            {
                player = getPlayerData(playerName).getRight();
            }
            else if (!playerUUID.equals(config.playerNameList.get(playerName)))
            {
                config.playerNameList.values().remove(playerUUID);
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

    public boolean addClientListPlayer(UUID playerUUID, ConfigJsonObj.Category type, String nickname)
    {
        if (config.clientPlayerList.putIfAbsent(playerUUID, new ConfigJsonObj.PlayerData(type, nickname)) == null)
        {
            save();
            return true;
        }
        return false;
    }

    public boolean editClientListPlayer(String playerName, ConfigJsonObj.Category type, String nickname) { return editClientListPlayer(config.playerNameList.get(playerName), type, nickname); }

    public boolean editClientListPlayer(UUID playerUUID, ConfigJsonObj.Category type, String nickname)
    {
        ConfigJsonObj.PlayerData player = config.clientPlayerList.get(playerUUID);
        if (player != null)
        {
            if (type != null) player.type = type;
            if (nickname != null) player.nickname = nickname;
            save();
            return true;
        }
        return false;
    }

    public boolean removeClientListPlayer(String playerName) { return removeClientListPlayer(config.playerNameList.get(playerName)); }

    public boolean removeClientListPlayer(UUID playerUUID)
    {
        if (config.clientPlayerList.remove(playerUUID) != null | config.playerNameList.values().remove(playerUUID))
        {
            save();
            return true;
        }
        return false;
    }

    public void setServerList(Map<UUID, ConfigJsonObj.PlayerData> playerList)
    {
        config.serverPlayerList.clear();
        config.serverPlayerList.putAll(playerList);

    }

    public void resetList()
    {
        config.clientPlayerList.clear();
        config.playerNameList.clear();
        save();
    }

    public void setEnableNameInfo(boolean enable)
    {
        config.enableNameColor = enable;
        save();
    }
}
