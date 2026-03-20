package net.byws.selectiummod.config;

import java.util.UUID;

public class PlayerListConfig
{
//    private static final File PLAYER_LIST_CONFIG_FILE =
//            FabricLoader.getInstance().getConfigDir().resolve("player_list_selectium-mod.json").toFile();
    private static UUID allyList[] = {};
    private static UUID targetList[] = {UUID.fromString("0422ee86-9842-43dd-8fd7-3665aeb87552")};
    private static final int allyColor = 0xFF00FF00;
    private static final int targetColor = 0xFFFF0000;

    public static boolean isAlly(UUID playerUUID)
    {
        for (UUID allyUUID : allyList)
        {
            if (allyUUID.equals(playerUUID)) return true;
        }
        return false;
    }
    public static boolean isTarget(UUID playerUUID)
    {
        for (UUID targetUUID : targetList)
        {
            if (targetUUID.equals(playerUUID)) return true;
        }
        return false;
    }

    public static Integer getPlayerOutlineColorRGBA(UUID playerUUID)
    {
        if (isAlly(playerUUID))
        {
            return allyColor;
        }
        else if (isTarget(playerUUID))
        {
            return targetColor;
        }
        return null;
    }
}
