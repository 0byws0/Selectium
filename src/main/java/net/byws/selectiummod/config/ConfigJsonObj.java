package net.byws.selectiummod.config;

import net.minecraft.ChatFormatting;

import java.util.HashMap;
import java.util.UUID;

public class ConfigJsonObj
{
    public enum Category
    {
        ALLY(ChatFormatting.GREEN, ChatFormatting.DARK_GREEN),
        NEUTRAL(ChatFormatting.YELLOW, ChatFormatting.GOLD),
        TARGET(ChatFormatting.RED, ChatFormatting.DARK_RED);

        private final ChatFormatting COLOR_FORMAT, SHARED_COLOR_FORMAT;

        Category(ChatFormatting colorFormatting, ChatFormatting sharedColorFormatting)
        {
            this.COLOR_FORMAT = colorFormatting;
            this.SHARED_COLOR_FORMAT = sharedColorFormatting;
        }

        public ChatFormatting getColorFormatting() { return COLOR_FORMAT; }

        public ChatFormatting getSharedColorFormatting() { return SHARED_COLOR_FORMAT; }
    }

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
