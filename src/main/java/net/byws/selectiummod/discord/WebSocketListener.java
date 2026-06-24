package net.byws.selectiummod.discord;

import net.minecraft.client.Minecraft;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.byws.selectiummod.config.ConfigManager;
import net.byws.selectiummod.config.ConfigJsonObj;

public class WebSocketListener implements WebSocket.Listener
{
    private static volatile WebSocketListener INSTANCE;
    private final Logger LOGGER;
    private final Gson GSON;

    private volatile WebSocket discord_conn;
    private volatile boolean isConnecting;
    private final StringBuilder messageBuffer;
    private ConcurrentLinkedQueue<Runnable> updateQueue;

    private WebSocketListener(Logger modLogger, Gson gson)
    {
        this.LOGGER = modLogger;
        this.GSON = gson;
        this.messageBuffer = new StringBuilder();
        this.updateQueue = new ConcurrentLinkedQueue();
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

    public boolean isConnecting() { return isConnecting; }

    public boolean isConnected() { return discord_conn != null; }

    public CompletableFuture<WebSocket> connect()
    {
        synchronized (this)
        {
            if (isConnecting || discord_conn != null) throw new IllegalStateException("Already connecting");
            isConnecting = true;
        }

        return HttpClient.newHttpClient().newWebSocketBuilder()
                .header("uuid", "0422ee86-9842-43dd-8fd7-3665aeb87552") // Minecraft.getInstance().getUser().getProfileId().toString()
                .header("token", ConfigManager.getInstance().getToken())
                .buildAsync(URI.create("ws://localhost:8000/ws"), this)
                .thenApply(ws ->
                {
                    synchronized (this)
                    {
                        discord_conn = ws;
                        isConnecting = false;
                        messageBuffer.setLength(0);
                    }
                    return ws;
                })
                .exceptionally(ex ->
                {
                    LOGGER.error("Selectium Mod failed to connect to WebSocket: {}", ex.getMessage());
                    synchronized (this)
                    {
                        discord_conn = null;
                        isConnecting = false;
                    }
                    throw new RuntimeException(ex);
                });
    }

    @Override
    public void onOpen(WebSocket websocket)
    {
        LOGGER.info("Selectium Mod WebSocket connection established");
        websocket.request(1);
    }

    public void update()
    {
        Runnable nextUpdate = updateQueue.poll();
        if (nextUpdate != null) nextUpdate.run();
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last)
    {
        String fullMessage = null;

        synchronized (this)
        {
            messageBuffer.append(data);
            if (last)
            {
                fullMessage = messageBuffer.toString();
                messageBuffer.setLength(0);
            }
        }

        if (fullMessage != null)
        {
            ConfigManager configManager = ConfigManager.getInstance();

            try
            {
                JsonObject message = GSON.fromJson(fullMessage, JsonObject.class);
                switch(message.get("action").getAsString())
                {
                    case "update_all_players":
                        Map<UUID, ConfigJsonObj.PlayerData> serverPlayerList = GSON.fromJson(message.get("payload"),
                                new TypeToken<Map<UUID, ConfigJsonObj.PlayerData>>(){}.getType());
                        var clientPlayerList = Minecraft.getInstance().submit(() -> configManager.getPlayerList()).join();

                        for (var playerEntry : clientPlayerList.entrySet())
                        {
                            UUID playerUUID = playerEntry.getKey();
                            if (!playerEntry.getValue().clientSide && !serverPlayerList.containsKey(playerUUID)) updateQueue.add(() ->
                                    configManager.removeListPlayer(playerUUID));
                        }

                        serverPlayerList.forEach((playerUUID, updatedPlayer) ->
                        {
                            ConfigJsonObj.PlayerData existingPlayer =  clientPlayerList.get(playerUUID);
                            final boolean clientSide = updatedPlayer.clientSide;
                            final ConfigJsonObj.Category type = updatedPlayer.type;
                            final String nickname = updatedPlayer.nickname;

                            if (existingPlayer == null)
                            {
                                updateQueue.add(() -> configManager.addListPlayer(playerUUID, clientSide, type, nickname));

                            }
                            else if (!existingPlayer.clientSide)
                            {
                                updateQueue.add(() -> configManager.editListPlayer(playerUUID, clientSide, type, nickname));
                            }
                        });
                        break;
                }
            }
            catch (Exception e)
            {
                LOGGER.error("Selectium Mod failed to parse websocket data: {}", e.getMessage());
            }
        }

        webSocket.request(1);
        return null;
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason)
    {
        LOGGER.info("Selectium Mod WebSocket closed with status code {} and reason: {}", statusCode, reason);
        synchronized (this)
        {
            discord_conn = null;
            isConnecting = false;
        }
        return null;
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error)
    {
        LOGGER.error("Selectium Mod WebSocket error details: ", error);
        synchronized (this)
        {
            discord_conn = null;
            isConnecting = false;
        }
    }
}
