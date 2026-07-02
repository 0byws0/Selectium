package net.byws.selectiummod.config;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class ConfigManager
{
    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("config-selectium-mod.json");
    private static volatile ConfigManager INSTANCE;
    private final Logger LOGGER;
    private final Gson GSON;
    
    private ConfigJsonObj config;
    private boolean requiresSave;
    private final Map<String, UUID> onlinePlayers;

    private ConfigManager(Logger modLogger, Gson gson)
    {
        this.LOGGER = modLogger;
        this.GSON = gson;
        this.config = new ConfigJsonObj();
        this.requiresSave = false;
        this.onlinePlayers = new HashMap<>();
    }

    public static void init(final Logger modLogger, final Gson gson)
    {
        if (INSTANCE != null) return;

        synchronized (ConfigManager.class)
        {
            if (INSTANCE == null)
            {
                INSTANCE = new ConfigManager(modLogger, gson);
                INSTANCE.load();
            }
        }
    }

    public static ConfigManager getInstance()
    {
        if (INSTANCE == null) throw new IllegalStateException("ConfigManager not initialized");
        return INSTANCE;
    }

    private void load()
    {
        if (!Files.exists(CONFIG_PATH)) save();

        try
        {
            ConfigJsonObj loaded = GSON.fromJson(Files.readString(CONFIG_PATH), ConfigJsonObj.class);
            if (loaded != null) config = loaded;

            if (config.playerCatList != null) // remove next update
            {
                config.playerList = config.playerCatList;
                config.playerCatList = null;
            }
        }
        catch (IOException e)
        {
            LOGGER.error("Selectium Mod failed to load config: {}", e.getMessage());
        }
    }

    private void save()
    {
        try
        {
            Files.writeString(CONFIG_PATH, GSON.toJson(config));
        }
        catch (IOException e)
        {
            LOGGER.error("Selectium Mod failed to save config: {}", e.getMessage());
        }
    }

    public Map<UUID, ConfigJsonObj.PlayerData> getPlayerList() { return new HashMap<>(config.playerList); }

    public Map<String, UUID> getOnlinePlayers() { return onlinePlayers; }

    public Set<String> getListNames() { return config.playerNameList.keySet(); }

    public ConfigJsonObj.PlayerData getPlayerData(String playerName) { return getPlayerData(config.playerNameList.get(playerName)); }

    public ConfigJsonObj.PlayerData getPlayerData(UUID playerUUID) { return config.playerList.get(playerUUID); }

    public boolean getEnableNameInfo() { return config.enableNameColor; }

    public String getToken() { return config.token; }

    public void updatePlayerData()
    {
        var connection = Minecraft.getInstance().getConnection();
        if (connection == null) return;

        onlinePlayers.clear();

        for (var playerInfo : connection.getOnlinePlayers())
        {
            UUID playerUUID = playerInfo.getProfile().getId();
            String playerName = playerInfo.getProfile().getName();
            onlinePlayers.put(playerName, playerUUID);
            ConfigJsonObj.PlayerData player = config.playerList.get(playerUUID);

            if (player == null)
            {
                player = config.playerList.get(config.playerNameList.get(playerName));
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
        requiresSave = false;
    }

    public boolean addListPlayer(UUID playerUUID, boolean clientSide, ConfigJsonObj.Category type, String nickname)
    {
        if (playerUUID != null &&
                config.playerList.putIfAbsent(playerUUID, new ConfigJsonObj.PlayerData(clientSide, type, (nickname == null) ? "" : nickname)) == null)
        {
            requiresSave = true;
            return true;
        }
        return false;
    }

    public boolean editListPlayer(String playerName, Boolean clientSide, ConfigJsonObj.Category type, String nickname)  { return editListPlayer(config.playerNameList.get(playerName), clientSide, type, nickname); }

    public boolean editListPlayer(UUID playerUUID, Boolean clientSide, ConfigJsonObj.Category type, String nickname)
    {
        ConfigJsonObj.PlayerData player = config.playerList.get(playerUUID);
        if (player != null)
        {
            if (clientSide != null) player.clientSide = clientSide;
            if (type != null) player.type = type;
            if (nickname != null) player.nickname = nickname;
            requiresSave = true;
            return true;
        }
        return false;
    }

    public boolean removeListPlayer(String playerName) { return removeListPlayer(config.playerNameList.get(playerName)); }

    public boolean removeListPlayer(UUID playerUUID)
    {
        if (config.playerList.remove(playerUUID) != null | config.playerNameList.values().remove(playerUUID))
        {
            requiresSave = true;
            return true;
        }
        return false;
    }

    public void clearList()
    {
        config.playerList.clear();
        config.playerNameList.clear();
        requiresSave = true;
    }

    public void setEnableNameInfo(boolean enable)
    {
        config.enableNameColor = enable;
        requiresSave = true;
    }

    public void setToken(String token)
    {
        config.token = token;
        requiresSave = true;
    }
}
