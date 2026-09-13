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
import java.util.UUID;

public class ConfigManager
{
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("config-selectium-mod.json");

    private static ConfigManager instance;
    private final Logger logger;
    private final Gson gson;
    
    private ConfigJsonObj config;
    private boolean requiresSave;
    private final Map<String, UUID> onlinePlayers = new HashMap<>();

    private ConfigManager(Logger modLogger, Gson gson)
    {
        this.logger = modLogger;
        this.gson = gson;
        this.config = new ConfigJsonObj();
        this.requiresSave = false;
    }

    public static void init(final Logger modLogger, final Gson gson)
    {
        if (instance != null) return;
        instance = new ConfigManager(modLogger, gson);
        instance.load();
    }

    public static ConfigManager getInstance()
    {
        if (instance == null) throw new IllegalStateException("ConfigManager not initialized");
        return instance;
    }

    private void load()
    {
        if (!Files.exists(CONFIG_PATH)) save();

        try
        {
            ConfigJsonObj loaded = gson.fromJson(Files.readString(CONFIG_PATH), ConfigJsonObj.class);
            if (loaded != null) config = loaded;

            if (config.playerCatList != null) // remove next update
            {
                config.playerList = config.playerCatList;
                config.playerCatList = null;
            }
            if (config.token != null) // remove next update
            {
                config.tokens.put(Minecraft.getInstance().getUser().getProfileId(), config.token);
                config.token = null;
            }
            for (ConfigJsonObj.PlayerData playerData : config.playerList.values()) // remove next update
            {
                if (playerData.group == null) playerData.group = "";
                if (playerData.type == null) continue;
                if (playerData.clientSide) playerData.clientType = playerData.type;
                else playerData.serverType = playerData.type;
                playerData.type = null;
            }
        }
        catch (IOException e)
        {
            logger.error("Selectium Mod failed to load config: {}", e.getMessage());
        }
    }

    private void save()
    {
        try
        {
            Files.writeString(CONFIG_PATH, gson.toJson(config));
        }
        catch (IOException e)
        {
            logger.error("Selectium Mod failed to save config: {}", e.getMessage());
        }
    }

    public Map<String, UUID> getOnlinePlayers() { return new HashMap<>(onlinePlayers); }

    public Map<UUID, ConfigJsonObj.PlayerData> getPlayerList() { return new HashMap<>(config.playerList); }

    public Map<String, UUID> getPlayerNameList() { return new HashMap<>(config.playerNameList); }

    public ConfigJsonObj.PlayerData getPlayerData(String playerName) { return getPlayerData(config.playerNameList.get(playerName)); }

    public ConfigJsonObj.PlayerData getPlayerData(UUID playerUUID) { return config.playerList.get(playerUUID); }

    public int getKills(boolean clientSide) { return (clientSide) ? config.clientKills : config.serverKills; }

    public boolean getEnableGroupInfo() { return config.enableGroupInfo; }

    public boolean getEnableNicknameInfo() { return config.enableNicknameInfo; }

    public boolean getEnableInfo() { return config.enableInfo; }

    public String getToken() { return config.tokens.get(Minecraft.getInstance().getUser().getProfileId()); }

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

    public boolean addListPlayer(UUID playerUUID, boolean clientSide, ConfigJsonObj.Category type, String group, String nickname)
    {
        if (playerUUID != null && config.playerList.putIfAbsent(playerUUID,
                new ConfigJsonObj.PlayerData((clientSide) ? type : null, (clientSide) ? null : type,
                        (group == null) ? "" : group, (nickname == null) ? "" : nickname)) == null)
        {
            requiresSave = true;
            return true;
        }

        return false;
    }

    public boolean editListPlayer(String playerName, boolean clientSide, ConfigJsonObj.Category type, String group, String nickname)
    {
        return editListPlayer(config.playerNameList.get(playerName), clientSide, type, group ,nickname);
    }

    public boolean editListPlayer(UUID playerUUID, boolean clientSide, ConfigJsonObj.Category type, String group, String nickname)
    {
        ConfigJsonObj.PlayerData player = config.playerList.get(playerUUID);
        if (player == null) return false;

        if (type != null)
        {
            if (clientSide) player.clientType = type;
            else player.serverType = type;
        }
        if (group != null) player.group = group;
        if (nickname != null) player.nickname = nickname;
        requiresSave = true;

        return true;
    }

    public boolean removeListPlayer(String playerName, boolean clientSide) { return removeListPlayer(config.playerNameList.get(playerName), clientSide); }

    public boolean removeListPlayer(UUID playerUUID, boolean clientSide)
    {
        ConfigJsonObj.PlayerData player = config.playerList.get(playerUUID);
        if (player == null) return false;

        if (clientSide) player.clientType = null;
        else player.serverType = null;

        if (player.clientType == null && player.serverType == null)
        {
            config.playerList.remove(playerUUID);
            config.playerNameList.values().remove(playerUUID);
            requiresSave = true;
        }

        return true;
    }

    public void clearList(boolean clientSide)
    {
        getPlayerList().forEach((playerUUID, playerData) -> removeListPlayer(playerUUID, clientSide));
        config.playerNameList.clear();
        requiresSave = true;
    }

    public boolean addKill(String playerName) { return addKill(config.playerNameList.get(playerName)); }

    public boolean addKill(UUID playerUUID)
    {
        ConfigJsonObj.PlayerData player = config.playerList.get(playerUUID);
        if (player == null) return false;

        if (ConfigJsonObj.Category.TARGET.equals(player.clientType)) ++config.clientKills;
        if (ConfigJsonObj.Category.TARGET.equals(player.serverType)) ++config.serverKills;
        requiresSave = true;

        return true;
    }

    public void setEnableGroupInfo(boolean enable)
    {
        config.enableGroupInfo = enable;
        requiresSave = true;
    }

    public void setEnableNicknameInfo(boolean enable)
    {
        config.enableNicknameInfo = enable;
        requiresSave = true;
    }

    public void setEnableInfo(boolean enable)
    {
        config.enableInfo = enable;
        requiresSave = true;
    }

    public void setToken(String token)
    {
        config.tokens.put(Minecraft.getInstance().getUser().getProfileId(), token);
        requiresSave = true;
    }
}
