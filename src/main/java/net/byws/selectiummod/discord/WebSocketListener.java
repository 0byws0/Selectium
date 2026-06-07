package net.byws.selectiummod.discord;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.UUID;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentSkipListMap;

import net.byws.selectiummod.config.ConfigJsonObj;
import net.byws.selectiummod.config.ConfigManager;

public class WebSocketListener implements WebSocket.Listener
{
    private static WebSocketListener INSTANCE;
    private final Logger LOGGER;
    private final Gson GSON;

    private WebSocket discord_conn;
    private ConcurrentSkipListMap<UUID, ConfigJsonObj.PlayerData> sharedPlayerData = new ConcurrentSkipListMap<>();

    private WebSocketListener(Logger modLogger, Gson gson)
    {
        this.LOGGER = modLogger;
        this.GSON = gson;
    }

    public static synchronized void init(Logger modLogger, Gson gson)
    {
        if (INSTANCE != null) return;

        WebSocketListener listener = new WebSocketListener(modLogger, gson);

            HttpClient.newHttpClient().newWebSocketBuilder()
                    .buildAsync(URI.create("ws://localhost:8000/ws"), listener)
                    .thenAccept(ws ->
                    {
                        INSTANCE = listener;
                        INSTANCE.discord_conn = ws;
                    })
                    .exceptionally(ex ->
                    {
                        modLogger.error("Selectium Mod failed to connect to websocket: {}", ex.getMessage());
                        return null;
                    });
    }

    public static WebSocketListener getInstance()
    {
        if (INSTANCE == null) throw new IllegalStateException("Websocket not initialized");
        return INSTANCE;
    }

    @Override
    public void onOpen(WebSocket websocket)
    {
        LOGGER.info("Discord connection established");
        websocket.request(1);
    }

    public void updateConfig()
    {
        var playerEntry = sharedPlayerData.pollFirstEntry();
        if (playerEntry == null) return;

        ConfigManager configManager = ConfigManager.getInstance();
        UUID playerUUID = playerEntry.getKey();
        ConfigJsonObj.PlayerData updatedPlayer = playerEntry.getValue();

        ConfigJsonObj.PlayerData player = configManager.getPlayerData(playerUUID);

        if (player == null)
        {
            configManager.addListPlayer(playerUUID, updatedPlayer.shared, updatedPlayer.type, updatedPlayer.nickname);
        }
        else if (player.shared)
        {
            configManager.editListPlayer(playerUUID, updatedPlayer.shared, updatedPlayer.type, updatedPlayer.nickname);
        }
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last)
    {
        try
        {
            Map<UUID, ConfigJsonObj.PlayerData> incomingData = GSON.fromJson(data.toString(),
                    new TypeToken<Map<UUID, ConfigJsonObj.PlayerData>>(){}.getType());

            if (incomingData != null)
            {
                sharedPlayerData.clear();
                sharedPlayerData.putAll(incomingData);
            }
        }
        catch (Exception e)
        {
            LOGGER.error("Selectium Mod failed to parse websocket data: {}", e.getMessage());
        }

        webSocket.request(1);
        return null;
    }
}
