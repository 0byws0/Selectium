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

        public final ChatFormatting CLIENT_COLOR_FORMAT, SERVER_COLOR_FORMAT;

        Category(ChatFormatting colorFormatting, ChatFormatting sharedColorFormatting)
        {
            this.CLIENT_COLOR_FORMAT = colorFormatting;
            this.SERVER_COLOR_FORMAT = sharedColorFormatting;
        }
    }

    public static class PlayerData
    {
        public boolean clientSide; // remove next update
        public Category type; // remove next update
        public Category clientType;
        public Category serverType;
        public String group;
        public String nickname;

        public PlayerData(Category clientType, Category serverType, String group, String nickname)
        {
            this.clientType = clientType;
            this.serverType = serverType;
            this.group = group;
            this.nickname = nickname;
        }
    }

    public String token = null; // remove next update
    public HashMap<UUID, String> tokens = new HashMap<>();
    public HashMap<UUID, PlayerData> playerCatList = null; // remove next update
    public HashMap<UUID, PlayerData> playerList = new HashMap<>();
    public HashMap<String, UUID> playerNameList = new HashMap<>();
    public int clientKills = 0;
    public int serverKills = 0;
    public boolean enableGroupInfo = true;
    public boolean enableNicknameInfo = true;
    public boolean enableInfo = true;
}
