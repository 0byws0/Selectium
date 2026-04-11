package net.byws.selectiummod.config;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class ConfigManger
{
    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("config-selectium-mod.json");

    private static ConfigJsonObj config;

    public static Logger LOGGER;

    private static void load()
    {
        try {
            if (!Files.exists(CONFIG_PATH)) {
                resetList();
            }

            Gson gson = new Gson();
            config = gson.fromJson(Files.readString(CONFIG_PATH), ConfigJsonObj.class);
        }
        catch (IOException e)
        {
            LOGGER.error("Selectium Mod failed to load config: {}", e.getMessage());
        }
    }

    private static void save()
    {
        Gson gson = new Gson();

        try (FileWriter fw = new FileWriter(CONFIG_PATH.toFile()))
        {
            fw.write(gson.toJson(config));
        }
        catch (IOException e)
        {
            LOGGER.error("Selectium Mod failed to save config: {}", e.getMessage());
        }
    }

    public static void init(final String modID)
    {
        LOGGER = LoggerFactory.getLogger(modID);
        config = new ConfigJsonObj();
        load();
    }

    public static void resetList()
    {
        config.playerCatList.clear();
        config.playerNameList.clear();

        config.playerCatList.put(UUID.fromString("0422ee86-9842-43dd-8fd7-3665aeb87552"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("1577172a-e442-44f5-8555-8447570f18e2"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("c04d5245-1160-48a1-bd58-2df9de998fce"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("9ef3220b-f23c-46ef-b4dc-f63ed7626668"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("e1099b16-efef-43d5-b758-81a5b8175bf9"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("58dcf27c-bb00-4bfd-9c17-ccca3e11363a"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("69f44ac7-2e08-4d01-95aa-4d6fcec942ef"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("1166743e-fe51-4c7a-badd-f0cb17589662"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("065e2635-2578-4b2a-bb41-a7163989b906"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("d0d95c59-eae5-418b-b7da-74c90ecaa737"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("6842f34a-aecf-4084-8acc-7ea6d414c2ba"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("78f9fefc-94ec-45d3-8c6e-b114e906b47a"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("4b26ecc3-b74b-411c-98b0-3bf255aa9409"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("f87ba90e-70e4-4de7-8da8-139e6819fb76"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("ed1aecf6-64bb-459f-9fc6-4e9d9bdd508c"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("b59a9076-b132-4f38-9f3e-6396667787ef"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("1bf86964-6cf2-4a8e-b948-671303fe8aca"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("70ee8ffc-8f96-4243-823e-e28c72b12a02"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("350c7092-756a-4f94-977e-2ee220b33346"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("f6ec9621-b3e7-410a-87f3-d254798a3bb5"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("96367f00-447b-4e4d-9c3c-74360cbdf88f"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("84b228b1-0fff-47f6-a12a-8f472a0bf349"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("5908ed36-f8f8-43f2-8bff-d27fd82e6660"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("fd7431d9-008d-413b-adbf-b3c000ed3c60"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("7f691b2d-066c-4058-b64e-421885edd00e"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("71d31173-a5df-4505-9e6b-8c1cbbb1638e"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("9325bf75-2c90-4fe1-b015-0a9b17a4a2a0"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("2abc27bf-235a-4a74-bcb2-a577ca306230"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("191a57c7-b24b-463c-9c62-a47c92b92f52"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("1bdb8823-cb2c-4a38-ba7d-77be8db94691"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("d60950dc-dd62-412c-abe7-161a630e2a9b"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("3e9e6317-7d9b-4df4-adb4-b23f8faf0f69"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("2a8bae6d-0df6-4fad-b90b-7e5b5a118481"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("fa749d22-b395-4705-88b1-1fa92f990541"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("d2f0780a-54a8-4865-a473-740b514d0ebf"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("71425ca9-566d-4034-9be0-c72283c597a6"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("3a2c2d51-d3d5-42b0-b8ce-b304f6b6d234"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("002d6dd6-b4ff-4108-8dc3-3ac80a1bb5c6"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("df4ec4b8-fc37-42eb-bd19-86286719d9eb"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("8e2ba15c-4227-431b-aacd-6140493d0b08"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("b2c726ea-e6f2-434c-8fcc-fe167e7350f6"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("6fe82f2e-03e7-4e44-9607-203fb9496def"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("fdab1b4a-b926-4842-99a4-7c781d35d20a"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("b838276d-a3db-4a51-b9cc-1a7faaee8ce3"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("edf52cab-d42d-4e0c-8b57-5bf7714ac6f7"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("5b827bf1-4df0-4301-8503-6f6d6dd48ace"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("0b713dd3-4728-4fc5-b96b-e02507d083b4"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("8c95f6fc-633d-4d00-a778-34f980aefa77"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("23683fe3-e11a-4f93-af7b-b8f45904512e"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("88cb8aef-73f4-4d1e-bc82-9a4cec11a376"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("a6af75af-9657-4356-8e63-82dc33144555"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("7d3f0e49-033b-4843-af6b-537d89169560"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("44e8366b-dcc8-4270-a400-e50899df4a51"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("0cd2da19-468b-4fa8-ba42-d918618bf0cb"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("a5ccae4f-df52-43cf-9cfe-3c0917c08adc"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("b8cd98de-62d6-42f4-908b-5fe569f17cb5"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("a512be87-5178-4a48-8b2e-33e886221f5b"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("61e40c9a-601f-4c59-aaf5-42c2b2125b2d"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("3de7adbb-31fa-4a5a-8546-7973d5c81b30"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("8e05e555-f7f5-4cc4-a680-da3e8805e7e4"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("fa00accb-5d58-4a22-adfa-dba649935863"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("edf10ab9-6229-4a46-97a1-54fd81ac847b"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("1f3e233f-0d9a-47f3-a6ba-98b2250a0a9e"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("437cd667-14b8-43da-81bf-ea0ea94ea297"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("6782aa5d-5859-4dfc-8f48-81ebe908dd1c"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("c909d56b-0048-42b1-802f-fe95ad97c254"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("4fbc03c0-dd52-46f4-93e0-ad98fb80bbd4"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("440f0515-e23b-46ef-a550-75b94ec41392"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("b77f2d8f-a4f1-4656-9724-0ff1223e2b51"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("395a8062-e992-4a32-9b18-3b6ee8372a80"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("91193273-bf07-4edc-898b-2db80570f1b1"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("56b9b2f7-e57d-4bc3-88f9-7e2b909d7cf1"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("9073921c-19a3-4874-9baa-f303901cb207"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));
        config.playerCatList.put(UUID.fromString("8d24ad2a-caab-4c2c-8044-54ed45827a30"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.ALLY, ""));

        config.playerCatList.put(UUID.fromString("fd5e13a0-3293-4333-87bf-c9ba199c8d7e"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.NEUTRAL, ""));
        config.playerCatList.put(UUID.fromString("97462a86-b9b4-4ba8-91ae-dd986f1fb0d2"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.NEUTRAL, ""));
        config.playerCatList.put(UUID.fromString("290e2c36-1c1d-4e0c-b5ce-bc71d84a0455"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.NEUTRAL, ""));
        config.playerCatList.put(UUID.fromString("ea2b9eb2-7dda-42ae-8620-170588720141"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.NEUTRAL, ""));
        config.playerCatList.put(UUID.fromString("6de1781f-a531-40bd-9227-7484095886d0"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.NEUTRAL, ""));
        config.playerCatList.put(UUID.fromString("cf4dfe3f-f032-459f-967f-1c40d439b821"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.NEUTRAL, ""));
        config.playerCatList.put(UUID.fromString("ff8ca01d-d995-4697-bc19-b554ff6ba827"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.NEUTRAL, ""));
        config.playerCatList.put(UUID.fromString("1f7bcd31-ebf7-4320-bae6-121c4ca9d2ec"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.NEUTRAL, ""));
        config.playerCatList.put(UUID.fromString("7bff7f64-63cd-4bbb-90d3-17665bb85060"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.NEUTRAL, ""));
        config.playerCatList.put(UUID.fromString("33afe6c3-ee3d-425e-a3f2-79aff1de3521"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.NEUTRAL, ""));
        config.playerCatList.put(UUID.fromString("c04e2d38-5ff9-408d-9ed6-9bdc8e2c72b7"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.NEUTRAL, ""));
        config.playerCatList.put(UUID.fromString("2348d250-f2bf-4614-8010-e4b5900bf731"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.NEUTRAL, ""));
        config.playerCatList.put(UUID.fromString("e9b1fb92-a8e7-40ea-960c-d4dff8d1e2d6"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.NEUTRAL, ""));
        config.playerCatList.put(UUID.fromString("e60a8fc3-6011-4eb0-822a-4ab91cab9f53"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.NEUTRAL, ""));
        config.playerCatList.put(UUID.fromString("7dc59d45-f541-4b74-9156-768c5d15c03d"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.NEUTRAL, ""));

        config.playerCatList.put(UUID.fromString("6d536396-8766-4991-9230-3585f41384b2"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.TARGET, ""));
        config.playerCatList.put(UUID.fromString("72049abf-ec9e-4b23-8e9f-a61b703d5a73"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.TARGET, ""));
        config.playerCatList.put(UUID.fromString("97eb1dc9-82d3-458b-94f6-82188c5ab591"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.TARGET, ""));
        config.playerCatList.put(UUID.fromString("adfe551a-d4e5-44ae-800a-5c3aa431e2d8"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.TARGET, ""));
        config.playerCatList.put(UUID.fromString("7f489609-d6e1-485d-bcda-1f41aff8934f"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.TARGET, ""));
        config.playerCatList.put(UUID.fromString("4851487c-c4ce-4a74-856b-0748fcb33a23"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.TARGET, ""));
        config.playerCatList.put(UUID.fromString("21699e13-d173-4d5b-a25c-1271c13adb7a"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.TARGET, ""));
        config.playerCatList.put(UUID.fromString("a962dd80-edd0-4b5d-b1f5-279bd4a1d1b3"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.TARGET, ""));
        config.playerCatList.put(UUID.fromString("a8c0cf18-3dfd-4f60-bbe8-73c6875dca4c"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.TARGET, ""));
        config.playerCatList.put(UUID.fromString("356b8bf4-e1b6-4def-b276-6b800384d081"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.TARGET, ""));
        config.playerCatList.put(UUID.fromString("62c3e683-f187-42b2-b6d1-7ff867507204"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.TARGET, ""));
        config.playerCatList.put(UUID.fromString("f2ec0cde-6935-4b83-8835-f6cd14030691"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.TARGET, ""));
        config.playerCatList.put(UUID.fromString("1685ee86-969a-477a-bad1-de3c3d656aaf"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.TARGET, ""));
        config.playerCatList.put(UUID.fromString("df3098f6-9c75-4fb7-9176-f4d96326b941"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.TARGET, ""));
        config.playerCatList.put(UUID.fromString("6ac21828-0c50-4d42-aa0c-fe217bc6af46"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.TARGET, ""));
        config.playerCatList.put(UUID.fromString("16fa360e-6141-4564-98e5-2c296c7c1656"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.TARGET, ""));
        config.playerCatList.put(UUID.fromString("559269b7-3288-4010-bb1d-4ce057575f8c"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.TARGET, ""));
        config.playerCatList.put(UUID.fromString("a249c473-f071-4e07-b2e5-dc548f85df6d"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.TARGET, ""));
        config.playerCatList.put(UUID.fromString("b1fb8384-7919-4382-9f76-6eb555a7e4fb"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.TARGET, ""));
        config.playerCatList.put(UUID.fromString("0d23ff1d-5d45-4a6d-b320-2810a2309579"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.TARGET, ""));
        config.playerCatList.put(UUID.fromString("738793ad-5756-422b-b710-429bdc5951a8"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.TARGET, ""));
        config.playerCatList.put(UUID.fromString("c1a36e19-2c8f-4fd0-8e45-e83476eee021"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.TARGET, ""));
        config.playerCatList.put(UUID.fromString("5db4decb-ea56-4709-a0be-85987aa223d9"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.TARGET, ""));
        config.playerCatList.put(UUID.fromString("fc99a130-14d2-4c7a-bb12-f15a5fa592bd"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.TARGET, ""));
        config.playerCatList.put(UUID.fromString("f46605d7-ccf7-4ec9-97fb-fcbf874d8cd9"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.TARGET, ""));
        config.playerCatList.put(UUID.fromString("e71451a9-1b8f-4663-b291-6d5f429fd348"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.TARGET, ""));
        config.playerCatList.put(UUID.fromString("f4cbe007-f2b3-4e8d-b1f6-803625c76ecd"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.TARGET, ""));
        config.playerCatList.put(UUID.fromString("d5e8f4a8-437d-4f9f-8a25-d3e6d4a0a7f6"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.TARGET, ""));
        config.playerCatList.put(UUID.fromString("4efdfe03-678d-41db-b9ba-5eeb57bd281c"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.TARGET, ""));
        config.playerCatList.put(UUID.fromString("7c71da03-b580-4c87-b238-2a89c1a6525a"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.TARGET, ""));
        config.playerCatList.put(UUID.fromString("3923544c-91bd-4e67-965e-a7e50b5ca3d5"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.TARGET, ""));
        config.playerCatList.put(UUID.fromString("59e0ece3-1174-4291-b4df-d2ddb1a2fdad"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.TARGET, ""));
        config.playerCatList.put(UUID.fromString("32ef1dcf-a0b9-4528-b034-221243f1e0a8"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.TARGET, ""));
        config.playerCatList.put(UUID.fromString("0f74b2ee-7b7b-4a67-b8f1-da993615dd61"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.TARGET, ""));
        config.playerCatList.put(UUID.fromString("2c84a2e2-462d-46da-b309-46d00ed9d7b8"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.TARGET, ""));
        config.playerCatList.put(UUID.fromString("aad0dc1d-c090-455e-8963-37bbf10bb476"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.TARGET, ""));
        config.playerCatList.put(UUID.fromString("b5d3b800-bb06-47f5-80a1-9de6116154e5"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.TARGET, ""));
        config.playerCatList.put(UUID.fromString("0eddec06-f31d-4e2d-b3a6-d3b095a13e51"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.TARGET, ""));
        config.playerCatList.put(UUID.fromString("58fa9819-d620-4216-85c0-025de9069b6c"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.TARGET, ""));
        config.playerCatList.put(UUID.fromString("35acdc97-b4a5-4314-b0f3-2af6a49ff19e"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.TARGET, ""));
        config.playerCatList.put(UUID.fromString("5d240c76-917b-423f-89a8-14a656483fe7"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.TARGET, ""));
        config.playerCatList.put(UUID.fromString("1efe8be7-5e4d-409c-953c-e5ca78abb7d6"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.TARGET, ""));
        config.playerCatList.put(UUID.fromString("eabb8c32-9074-45a0-9cee-205b566ea89c"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.TARGET, ""));
        config.playerCatList.put(UUID.fromString("59ce450e-fc17-4b22-8b48-97b67b3bdacb"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.TARGET, ""));
        config.playerCatList.put(UUID.fromString("5ab8bd25-ba4d-451d-8f69-df23d03e5a25"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.TARGET, ""));
        config.playerCatList.put(UUID.fromString("7b0e1a12-f3fa-42fb-b780-7a02cf2e758e"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.TARGET, ""));
        config.playerCatList.put(UUID.fromString("a19eba69-d660-4543-a0da-33c83492de9b"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.TARGET, ""));
        config.playerCatList.put(UUID.fromString("d0b490cb-104d-4b3e-9448-4a7d001a993c"), new ConfigJsonObj.PlayerData(false, ConfigJsonObj.Category.TARGET, ""));

        save();
    }

    public static Boolean setPlayerNickname(String playerName, String playerNickname) { return setPlayerNickname(config.playerNameList.get(playerName), playerNickname); }

    public static Boolean setPlayerNickname(UUID playerUUID, String playerNickname)
    {
        ConfigJsonObj.PlayerData player = config.playerCatList.get(playerUUID);
        if (player != null)
        {
            player.nickname = playerNickname;
            return true;
        }
        return false;
    }

    public static ConfigJsonObj.PlayerData getPlayerData(String playerName, UUID playerUUID)
    {
        ConfigJsonObj.PlayerData player = config.playerCatList.get(playerUUID);
        boolean saveToFile = false;

        if (player == null)
        {
            player = config.playerCatList.get(config.playerNameList.get(playerName));
        }
        else if (config.playerNameList.get(playerName) != playerUUID)
        {
            config.playerNameList.remove(config.getNameFromList(playerUUID));
            config.playerNameList.put(playerName, playerUUID);
            saveToFile = true;
        }

        if (player != null && player.nickname.isEmpty())
        {
            player.nickname = playerName;
            saveToFile = true;
        }

        if (saveToFile) save();

        return player;
    }

    public static Integer getPlayerColorRGBA(ConfigJsonObj.Category playerType)
    {
        return switch(playerType)
        {
            case null -> null;
            case ALLY -> 0xFF00FF00;
            case NEUTRAL -> 0xFFFFFF00;
            case TARGET -> 0xFFFF0000;
        };
    }

    public static ChatFormatting getPlayerColorFormatting(ConfigJsonObj.Category playerType)
    {
        return switch(playerType)
        {
            case null -> null;
            case ALLY -> ChatFormatting.GREEN;
            case NEUTRAL -> ChatFormatting.YELLOW;
            case TARGET -> ChatFormatting.RED;
        };
    }

    public static String[] getNamesList() { return config.playerNameList.keySet().toArray(new String[0]); }

    public static String[] getNameUUIDs() { return config.playerNameList.values().toArray(new String[0]); }

    public static boolean getEnableOutline() { return config.enableOutline; }

    public static boolean getEnableNameInfo() { return config.enableNameColor; }

    public static boolean addListPlayer(UUID playerUUID, ConfigJsonObj.Category type)
    {
        if (config.playerCatList.containsKey(playerUUID))
        {
            config.playerCatList.get(playerUUID).type = type;
            save();
            return false;
        }
        config.playerCatList.put(playerUUID, new ConfigJsonObj.PlayerData(false, type, ""));
        save();
        return true;
    }

    public static boolean removeListPlayer(String playerName) { return removeListPlayer(config.playerNameList.get(playerName)); }

    public static boolean removeListPlayer(UUID playerUUID)
    {
        boolean containsPlayer = (config.playerCatList.remove(playerUUID) != null);
        if (config.playerNameList.remove(config.getNameFromList(playerUUID)) != null || containsPlayer) save();
        return containsPlayer;
    }

    public static void setEnableOutline(boolean enable)
    {
        config.enableOutline = enable;
        save();
    }

    public static void setEnableNameInfo(boolean enable)
    {
        config.enableNameColor = enable;
        save();
    }
}
