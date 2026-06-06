package net.byws.selectiummod.config;

import java.util.HashMap;
import java.util.UUID;

public class ConfigJsonObj
{
    public enum Category { ALLY, NEUTRAL, TARGET }

    public static class PlayerData
    {
        public boolean shared;
        public Category type;
        public String nickname;

        public PlayerData(boolean shared, Category type, String nickname)
        {
            this.shared = shared;
            this.type = type;
            this.nickname = nickname;
        }
    }

    public HashMap<UUID, PlayerData> playerCatList = new HashMap<>();
    public HashMap<String, UUID> playerNameList = new HashMap<>();
    public boolean enableNameColor = true;

    public String getNameFromList(UUID playerUUID)
    {
        for (HashMap.Entry<String, UUID> entry : playerNameList.entrySet())
        {
            if (entry.getValue().equals(playerUUID)) return entry.getKey();
        }
        return null;
    }
}
