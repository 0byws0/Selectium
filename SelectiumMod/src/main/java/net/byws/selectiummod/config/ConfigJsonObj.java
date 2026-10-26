package net.byws.selectiummod.config;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;

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
        public Boolean clientSide; // old data
        public Category type; // old data
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

        public ChatFormatting getDominantColor()
        {
            return (clientType != null && !clientType.equals(serverType)) ?
                    clientType.CLIENT_COLOR_FORMAT : serverType.SERVER_COLOR_FORMAT;
        }
    }

    public String token = null; // old data
    public HashMap<UUID, String> tokens = new HashMap<>();
    public HashMap<UUID, PlayerData> playerCatList = null; // old data
    public HashMap<UUID, PlayerData> playerList = new HashMap<>();
    public HashMap<String, UUID> playerNameList = new HashMap<>();
    public int clientKills = 0;
    public int serverKills = 0;
    public boolean enableGroupInfo = true;
    public boolean enableNicknameInfo = true;
    public boolean enableInfo = true;

    public void migrateData()
    {
        if (playerCatList != null)
        {
            playerList = playerCatList;
            playerCatList = null;
        }
        if (token != null)
        {
            tokens.put(Minecraft.getInstance().getUser().getProfileId(), token);
            token = null;
        }
        for (PlayerData playerData : playerList.values())
        {
            if (playerData.group == null) playerData.group = "";
            if (playerData.type != null)
            {
                if (playerData.clientSide == null || playerData.clientSide) playerData.clientType = playerData.type;
                else playerData.serverType = playerData.type;
                playerData.clientSide = null;
                playerData.type = null;
            }
        }
    }
}
