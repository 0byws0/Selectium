package net.byws.selectiummod.config;

import java.util.HashMap;
import java.util.UUID;

public class PlayerListConfig
{
    public enum Category { ALLY, TARGET }
//    private static final File PLAYER_LIST_CONFIG_FILE =
//            FabricLoader.getInstance().getConfigDir().resolve("player_list_selectium-mod.json").toFile();
    private static HashMap<UUID, Category> playerList = new HashMap<>();
    private static final int allyColor = 0xFF00FF00;
    private static final int targetColor = 0xFFFF0000;

    public static void load()
    {
        playerList.put(UUID.fromString("0422ee86-9842-43dd-8fd7-3665aeb87552"), Category.ALLY);
    }

    public static Integer getPlayerOutlineColorRGBA(UUID playerUUID)
    {
        return switch(playerList.get(playerUUID))
        {
            case null -> null;
            case ALLY -> allyColor;
            case TARGET -> targetColor;
        };
    }
}
