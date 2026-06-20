package net.byws.selectiummod.discord;

import net.minecraft.client.Minecraft;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.UUID;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import net.byws.selectiummod.config.ConfigManager;
import net.byws.selectiummod.config.ConfigJsonObj;

public class WebSocketListener implements WebSocket.Listener
{
    private static volatile WebSocketListener INSTANCE;
    private final Logger LOGGER;
    private final Gson GSON;

    private volatile WebSocket discord_conn;
    private final StringBuilder messageBuffer;

    private WebSocketListener(Logger modLogger, Gson gson)
    {
        this.LOGGER = modLogger;
        this.GSON = gson;
        this.messageBuffer = new StringBuilder();
    }

    public static void init(Logger modLogger, Gson gson)
    {
        if (INSTANCE != null) return;

        synchronized (WebSocketListener.class)
        {
            if (INSTANCE == null) INSTANCE = new WebSocketListener(modLogger, gson);
        }
    }

    public static WebSocketListener getInstance()
    {
        if (INSTANCE == null) throw new IllegalStateException("Websocket not initialized");
        return INSTANCE;
    }

    public CompletableFuture<WebSocket> connect()
    {
        if (discord_conn != null) throw new IllegalStateException("Connection already established");

        return HttpClient.newHttpClient().newWebSocketBuilder()
                .header("uuid", "0422ee86-9842-43dd-8fd7-3665aeb87552") // Minecraft.getInstance().getUser().getProfileId().toString()
                .header("token", ConfigManager.getInstance().getToken())
                .buildAsync(URI.create("ws://localhost:8000/ws"), this)
                .thenApply(ws ->
                {
                    this.discord_conn = ws;
                    return ws;
                })
                .exceptionally(ex ->
                {
                    LOGGER.error("Selectium Mod failed to connect to WebSocket: {}", ex.getMessage());
                    throw new RuntimeException(ex);
                });
    }

    @Override
    public void onOpen(WebSocket websocket)
    {
        LOGGER.info("Selectium Mod WebSocket connection established");
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
        messageBuffer.append(data);

        if (last)
        {
            try
            {
                JsonObject message = GSON.fromJson(messageBuffer.toString(), JsonObject.class);
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
            finally
            {
                messageBuffer.setLength(0);
            }
        }

        webSocket.request(1);
        return null;
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason)
    {
        LOGGER.info("Selectium Mod WebSocket closed with status code {} and reason: {}", statusCode, reason);
        discord_conn = null;
        return null;
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error)
    {
        LOGGER.error("Selectium Mod WebSocket error details: ", error);
        discord_conn = null;
    }
}
