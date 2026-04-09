package net.byws.selectiummod.config;

import com.google.common.collect.HashBiMap;

import java.util.HashMap;
import java.util.UUID;

public class ConfigJsonObj
{
    public enum Category { ALLY, NEUTRAL, TARGET }

    public static class PlayerData
    {
        public Boolean shared;
        public Category type;
        public String nickname;

        public PlayerData(Boolean shared, Category type, String nickname)
        {
            this.shared = shared;
            this.type = type;
            this.nickname = nickname;
        }
    }

    public HashMap<UUID, PlayerData> playerCatList;
    public HashBiMap<String, UUID> playerNameList;
    public boolean enableOutline;
    public boolean enableNameColor;

    public ConfigJsonObj()
    {
        playerCatList = new HashMap<>();
        playerNameList = HashBiMap.create();
        enableOutline = true;
        enableNameColor = true;
    }
}
