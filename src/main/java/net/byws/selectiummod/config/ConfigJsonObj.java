package net.byws.selectiummod.config;

import net.minecraft.ChatFormatting;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigJsonObj
{
    public enum Category
    {
        ALLY(ChatFormatting.GREEN, ChatFormatting.DARK_GREEN),
        NEUTRAL(ChatFormatting.YELLOW, ChatFormatting.GOLD),
        TARGET(ChatFormatting.RED, ChatFormatting.DARK_RED);

        private final ChatFormatting CLIENT_COLOR_FORMAT, SERVER_COLOR_FORMAT;

        Category(ChatFormatting colorFormatting, ChatFormatting sharedColorFormatting)
        {
            this.CLIENT_COLOR_FORMAT = colorFormatting;
            this.SERVER_COLOR_FORMAT = sharedColorFormatting;
        }

        public ChatFormatting getClientColorFormatting() { return CLIENT_COLOR_FORMAT; }

        public ChatFormatting getServerColorFormatting() { return SERVER_COLOR_FORMAT; }
    }

    public static class PlayerData
    {
        public Category type;
        public String nickname;

        public PlayerData(Category type, String nickname)
        {
            this.type = type;
            this.nickname = nickname;
        }
    }

    public HashMap<UUID, PlayerData> clientPlayerList = new HashMap<>();
    public ConcurrentHashMap<UUID, PlayerData> serverPlayerList = new ConcurrentHashMap<>();
    public HashMap<String, UUID> playerNameList = new HashMap<>();
    public boolean enableNameColor = true;
}
