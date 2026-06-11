package net.byws.selectiummod.discord;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.UUID;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import net.byws.selectiummod.config.ConfigManager;
import net.byws.selectiummod.config.ConfigJsonObj;

public class WebSocketListener implements WebSocket.Listener
{
    private static WebSocketListener INSTANCE;
    private final Logger LOGGER;
    private final Gson GSON;

    private WebSocket discord_conn;

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

    private void updateList(Map<UUID, ConfigJsonObj.PlayerData> serverList)
    {
        ConfigManager configManager = ConfigManager.getInstance();
        if (serverList == null) return;

        for (var playerEntry : configManager.getPlayerList())
        {
            UUID playerUUID = playerEntry.getKey();
            if (!playerEntry.getValue().clientSide && !serverList.containsKey(playerUUID)) configManager.removeListPlayer(playerUUID);
        }

        serverList.forEach((playerUUID, updatedPlayer) ->
        {
            ConfigJsonObj.PlayerData existingPlayer = configManager.getPlayerData(playerUUID);

            if (existingPlayer == null)
            {
                configManager.addListPlayer(playerUUID, updatedPlayer.clientSide, updatedPlayer.type, updatedPlayer.nickname);
            }
            else if (!existingPlayer.clientSide)
            {
                configManager.editListPlayer(playerUUID, updatedPlayer.clientSide, updatedPlayer.type, updatedPlayer.nickname);
            }
        });
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last)
    {
        try
        {
            JsonObject message = GSON.fromJson(data.toString(), JsonObject.class);
            switch(message.get("action").getAsString())
            {
                case "update_all_players":
                    updateList(GSON.fromJson(message.get("payload"), new TypeToken<Map<UUID, ConfigJsonObj.PlayerData>>(){}.getType()));
                    break;
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
