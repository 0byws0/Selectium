package net.byws.selectiummod.config;

import net.minecraft.ChatFormatting;

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
        public boolean clientSide;
        public Category type;
        public String nickname;

        public PlayerData(boolean clientSide, Category type, String nickname)
        {
            this.clientSide = clientSide;
            this.type = type;
            this.nickname = nickname;
        }
    }

    public ConcurrentHashMap<UUID, PlayerData> playerList = new ConcurrentHashMap<>();
    public ConcurrentHashMap<String, UUID> playerNameList = new ConcurrentHashMap<>();
    public boolean enableNameColor = true;
}
