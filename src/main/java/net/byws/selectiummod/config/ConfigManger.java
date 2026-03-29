package net.byws.selectiummod.config;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;

public class ConfigManger
{
    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("config-selectium-mod.json");

    public enum Category { ALLY, TARGET }

    private static class ConfigFileContents
    {
        private HashMap<UUID, Category> shrPlayerList = new HashMap<>();
        private HashMap<UUID, Category> pvtPlayerList = new HashMap<>();
        private boolean enableOutline = true;
        private boolean enableNameColor = true;
    }

    private static ConfigFileContents config = new ConfigFileContents();

    public static void load()
    {
        try
        {
            if (!Files.exists(CONFIG_PATH))
            {
                config.shrPlayerList.put(UUID.fromString("0422ee86-9842-43dd-8fd7-3665aeb87552"), Category.ALLY);
                config.shrPlayerList.put(UUID.fromString("1577172a-e442-44f5-8555-8447570f18e2"), Category.ALLY);
                config.shrPlayerList.put(UUID.fromString("c04d5245-1160-48a1-bd58-2df9de998fce"), Category.ALLY);
                config.shrPlayerList.put(UUID.fromString("9ef3220b-f23c-46ef-b4dc-f63ed7626668"), Category.ALLY);
                config.shrPlayerList.put(UUID.fromString("e1099b16-efef-43d5-b758-81a5b8175bf9"), Category.ALLY);
                config.shrPlayerList.put(UUID.fromString("58dcf27c-bb00-4bfd-9c17-ccca3e11363a"), Category.ALLY);
                config.shrPlayerList.put(UUID.fromString("69f44ac7-2e08-4d01-95aa-4d6fcec942ef"), Category.ALLY);
                config.shrPlayerList.put(UUID.fromString("1166743e-fe51-4c7a-badd-f0cb17589662"), Category.ALLY);
                config.shrPlayerList.put(UUID.fromString("065e2635-2578-4b2a-bb41-a7163989b906"), Category.ALLY);
                config.shrPlayerList.put(UUID.fromString("d0d95c59-eae5-418b-b7da-74c90ecaa737"), Category.ALLY);
                config.shrPlayerList.put(UUID.fromString("1efe8be7-5e4d-409c-953c-e5ca78abb7d6"), Category.ALLY);
                config.shrPlayerList.put(UUID.fromString("6842f34a-aecf-4084-8acc-7ea6d414c2ba"), Category.ALLY);
                config.shrPlayerList.put(UUID.fromString("78f9fefc-94ec-45d3-8c6e-b114e906b47a"), Category.ALLY);
                config.shrPlayerList.put(UUID.fromString("4b26ecc3-b74b-411c-98b0-3bf255aa9409"), Category.ALLY);
                config.shrPlayerList.put(UUID.fromString("d2f0780a-54a8-4865-a473-740b514d0eb"), Category.ALLY);
                config.shrPlayerList.put(UUID.fromString("e71451a9-1b8f-4663-b291-6d5f429fd348"), Category.ALLY);
                config.shrPlayerList.put(UUID.fromString("f87ba90e-70e4-4de7-8da8-139e6819fb76"), Category.ALLY);
                config.shrPlayerList.put(UUID.fromString("ed1aecf6-64bb-459f-9fc6-4e9d9bdd508c"), Category.ALLY);
                config.shrPlayerList.put(UUID.fromString("b59a9076-b132-4f38-9f3e-6396667787ef"), Category.ALLY);
                config.shrPlayerList.put(UUID.fromString("1bf86964-6cf2-4a8e-b948-671303fe8aca"), Category.ALLY);
                config.shrPlayerList.put(UUID.fromString("b5d3b800-bb06-47f5-80a1-9de6116154e5"), Category.ALLY);
                config.shrPlayerList.put(UUID.fromString("70ee8ffc-8f96-4243-823e-e28c72b12a02"), Category.ALLY);
                config.shrPlayerList.put(UUID.fromString("35acdc97-b4a5-4314-b0f3-2af6a49ff19e"), Category.ALLY);
                config.shrPlayerList.put(UUID.fromString("350c7092-756a-4f94-977e-2ee220b33346"), Category.ALLY);
                config.shrPlayerList.put(UUID.fromString("f6ec9621-b3e7-410a-87f3-d254798a3bb5"), Category.ALLY);
                config.shrPlayerList.put(UUID.fromString("96367f00-447b-4e4d-9c3c-74360cbdf88f"), Category.ALLY);
                config.shrPlayerList.put(UUID.fromString("84b228b1-0fff-47f6-a12a-8f472a0bf349"), Category.ALLY);
                config.shrPlayerList.put(UUID.fromString("58fa9819-d620-4216-85c0-025de9069b6c"), Category.ALLY);
                config.shrPlayerList.put(UUID.fromString("0eddec06-f31d-4e2d-b3a6-d3b095a13e51"), Category.ALLY);
                config.shrPlayerList.put(UUID.fromString("5908ed36-f8f8-43f2-8bff-d27fd82e6660"), Category.ALLY);
                config.shrPlayerList.put(UUID.fromString("fd7431d9-008d-413b-adbf-b3c000ed3c60"), Category.ALLY);
                config.shrPlayerList.put(UUID.fromString("7f691b2d-066c-4058-b64e-421885edd00e"), Category.ALLY);
                config.shrPlayerList.put(UUID.fromString("71d31173-a5df-4505-9e6b-8c1cbbb1638e"), Category.ALLY);
                config.shrPlayerList.put(UUID.fromString("3f96bec8-5bb0-4c7e-8c8d-ebf237f1d2db"), Category.ALLY);
                config.shrPlayerList.put(UUID.fromString("9325bf75-2c90-4fe1-b015-0a9b17a4a2a0"), Category.ALLY);
                config.shrPlayerList.put(UUID.fromString("2abc27bf-235a-4a74-bcb2-a577ca306230"), Category.ALLY);
                config.shrPlayerList.put(UUID.fromString("191a57c7-b24b-463c-9c62-a47c92b92f52"), Category.ALLY);
                config.shrPlayerList.put(UUID.fromString("1bdb8823-cb2c-4a38-ba7d-77be8db94691"), Category.ALLY);
                config.shrPlayerList.put(UUID.fromString("d60950dc-dd62-412c-abe7-161a630e2a9b"), Category.ALLY);
                config.shrPlayerList.put(UUID.fromString("3e9e6317-7d9b-4df4-adb4-b23f8faf0f69"), Category.ALLY);
                config.shrPlayerList.put(UUID.fromString("6d536396-8766-4991-9230-3585f41384b2"), Category.TARGET);
                config.shrPlayerList.put(UUID.fromString("72049abf-ec9e-4b23-8e9f-a61b703d5a73"), Category.TARGET);
                config.shrPlayerList.put(UUID.fromString("97eb1dc9-82d3-458b-94f6-82188c5ab591"), Category.TARGET);

                save();
            }

            Gson gson = new Gson();
            config = gson.fromJson(Files.readString(CONFIG_PATH), ConfigFileContents.class);
        }
        catch (Exception e)
        {

        }
    }

    public static void save()
    {
        Gson gson = new Gson();

        try (FileWriter fw = new FileWriter(CONFIG_PATH.toFile()))
        {
            fw.write(gson.toJson(config));
        }
        catch (Exception e)
        {

        }
    }

    private static Category checkLists(UUID playerUUID)
    {
        Category playerCat = config.shrPlayerList.get(playerUUID);
        if (playerCat == null) playerCat = config.pvtPlayerList.get(playerUUID);
        return playerCat;
    }

    public static Integer getPlayerColorRGBA(UUID playerUUID)
    {
        return switch(checkLists(playerUUID))
        {
            case null -> null;
            case ALLY -> 0xFF00FF00;
            case TARGET -> 0xFFFF0000;
        };
    }

    public static ChatFormatting getPlayerColorFormatting(UUID playerUUID)
    {
        return switch(checkLists(playerUUID))
        {
            case null -> null;
            case ALLY -> ChatFormatting.GREEN;
            case TARGET -> ChatFormatting.RED;
        };
    }

    public static HashMap<UUID, Category> getPvtList() { return config.pvtPlayerList; }

    public static boolean getEnableOutline() { return config.enableOutline; }

    public static boolean getEnableNameColor() { return config.enableNameColor; }

    public static boolean addPvtList(UUID playerUUID, Category type)
    {
        boolean containsPlayer = (config.pvtPlayerList.put(playerUUID, type) != null);
        save();
        return !containsPlayer;
    }

    public static boolean removePvtList(UUID playerUUID)
    {
        boolean containsPlayer = (config.pvtPlayerList.remove(playerUUID) != null);
        save();
        return containsPlayer;
    }

    public static void setEnableOutline(boolean enable)
    {
        config.enableOutline = enable;
        save();
    }

    public static void setEnableNameColor(boolean enable)
    {
        config.enableNameColor = enable;
        save();
    }
}
