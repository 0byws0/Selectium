package net.byws.selectiummod.websocket;

import net.minecraft.client.Minecraft;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.common.util.concurrent.RateLimiter;
import org.slf4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import net.byws.selectiummod.config.ConfigManager;
import net.byws.selectiummod.config.ConfigJsonObj;

public class WebSocketListener implements WebSocket.Listener
{
    private static final String DISCORD_WS_URL = "wss://bywsdomain.xyz/ws";
    private static final int WS_UPDATE_INTERVAL = 60000;
    private static volatile WebSocketListener INSTANCE;
    private final Logger LOGGER;
    private final Gson GSON;

    private volatile WebSocket discord_conn;
    private volatile boolean isConnecting, isDisconnecting;
    private long lastPushMillis;
    private final RateLimiter rateLimiter;
    private final StringBuilder messageBuffer;

    private WebSocketListener(Logger modLogger, Gson gson)
    {
        this.LOGGER = modLogger;
        this.GSON = gson;
        this.isConnecting = false;
        this.isDisconnecting = false;
        this.lastPushMillis = 0;
        this.rateLimiter = RateLimiter.create(5);
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

    public boolean isConnecting() { return isConnecting; }

    public boolean isDisconnecting() { return isDisconnecting; }

    public boolean isConnected() { return discord_conn != null; }

    @Override
    public void onOpen(WebSocket websocket) { websocket.request(1); }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason)
    {
        synchronized (this)
        {
            discord_conn = null;
            isConnecting = false;
            isDisconnecting = false;
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
            isDisconnecting = false;
        }
    }

    public CompletableFuture<WebSocket> connect()
    {
        synchronized (this)
        {
            if (isConnecting || isDisconnecting || discord_conn != null)
                return CompletableFuture.failedFuture(new IllegalStateException("Already connecting or connected"));
            isConnecting = true;
            messageBuffer.setLength(0);
        }

        return HttpClient.newHttpClient().newWebSocketBuilder()
                .header("uuid", "0422ee86-9842-43dd-8fd7-3665aeb87552") //Minecraft.getInstance().getUser().getProfileId().toString()
                .header("token", ConfigManager.getInstance().getToken())
                .buildAsync(URI.create(DISCORD_WS_URL), this)
                .thenApply(ws ->
                {
                    synchronized (this)
                    {
                        discord_conn = ws;
                        isConnecting = false;
                    }
                    return ws;
                })
                .exceptionally(ex ->
                {
                    synchronized (this)
                    {
                        discord_conn = null;
                        isConnecting = false;
                    }
                    throw new RuntimeException(ex);
                });
    }

    public CompletableFuture<WebSocket> close()
    {
        WebSocket ws;
        synchronized (this)
        {
            if (isConnecting || isDisconnecting || discord_conn == null)
                return CompletableFuture.failedFuture(new IllegalStateException("Not connected"));
            isDisconnecting = true;
            ws = discord_conn;
        }

        return ws.sendClose(1000, "Normal closure");
    }

    public void update()
    {
        WebSocket ws = discord_conn;
        long currentMillis = System.currentTimeMillis();

        if (ws != null && (currentMillis - lastPushMillis) > WS_UPDATE_INTERVAL)
        {
            ws.sendPong(StandardCharsets.UTF_8.encode(""));
            lastPushMillis = currentMillis;
        }
    }

    private void handleListUpdate(ConfigManager configManager, JsonObject message)
    {
        Map<UUID, ConfigJsonObj.PlayerData> serverPlayerList = GSON.fromJson(message.get("payload"),
                new TypeToken<Map<UUID, ConfigJsonObj.PlayerData>>(){}.getType());

        Minecraft.getInstance().execute(() ->
        {
            var clientPlayerList = configManager.getPlayerList();

            for (var playerEntry : clientPlayerList.entrySet())
            {
                UUID playerUUID = playerEntry.getKey();
                if (!playerEntry.getValue().clientSide && !serverPlayerList.containsKey(playerUUID))
                {
                    configManager.removeListPlayer(playerUUID);
                }
            }

            serverPlayerList.forEach((playerUUID, updatedPlayer) ->
            {
                ConfigJsonObj.PlayerData existingPlayer = clientPlayerList.get(playerUUID);
                if (existingPlayer == null)
                {
                    configManager.addListPlayer(playerUUID, updatedPlayer.clientSide, updatedPlayer.type, updatedPlayer.nickname);
                }
                else if (!existingPlayer.clientSide)
                {
                    configManager.editListPlayer(playerUUID, updatedPlayer.clientSide, updatedPlayer.type, updatedPlayer.nickname);
                }
            });
        });
    }

    private void handleListAdd(ConfigManager configManager, JsonObject message)
    {
        Map<UUID, ConfigJsonObj.PlayerData> serverPlayerList = GSON.fromJson(message.get("payload"),
                new TypeToken<Map<UUID, ConfigJsonObj.PlayerData>>(){}.getType());

        Minecraft.getInstance().execute(() ->
        {
            var clientPlayerList = configManager.getPlayerList();
            serverPlayerList.forEach((playerUUID, updatedPlayer) ->
            {
                if (clientPlayerList.get(playerUUID) == null) configManager.addListPlayer(playerUUID, updatedPlayer.clientSide, updatedPlayer.type, updatedPlayer.nickname);
            });
        });
    }

    private void handleListEdit(ConfigManager configManager, JsonObject message)
    {
        Map<UUID, ConfigJsonObj.PlayerData> serverPlayerList = GSON.fromJson(message.get("payload"),
                new TypeToken<Map<UUID, ConfigJsonObj.PlayerData>>(){}.getType());

        Minecraft.getInstance().execute(() ->
        {
            var clientPlayerList = configManager.getPlayerList();
            serverPlayerList.forEach((playerUUID, updatedPlayer) ->
            {
                ConfigJsonObj.PlayerData existingPlayer = clientPlayerList.get(playerUUID);
                if (existingPlayer == null)
                {
                    configManager.addListPlayer(playerUUID, updatedPlayer.clientSide, updatedPlayer.type, updatedPlayer.nickname);
                }
                else if (!existingPlayer.clientSide)
                {
                    configManager.editListPlayer(playerUUID, updatedPlayer.clientSide, updatedPlayer.type, updatedPlayer.nickname);
                }
            });
        });
    }

    private void handleListRemove(ConfigManager configManager, JsonObject message)
    {
        Map<UUID, ConfigJsonObj.PlayerData> serverPlayerList = GSON.fromJson(message.get("payload"),
                new TypeToken<Map<UUID, ConfigJsonObj.PlayerData>>(){}.getType());

        Minecraft.getInstance().execute(() ->
        {
            var clientPlayerList = configManager.getPlayerList();
            serverPlayerList.forEach((playerUUID, updatedPlayer) ->
            {
                ConfigJsonObj.PlayerData existingPlayer = clientPlayerList.get(playerUUID);
                if (existingPlayer != null && !existingPlayer.clientSide) configManager.removeListPlayer(playerUUID);
            });
        });
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
            if (!rateLimiter.tryAcquire())
            {
                LOGGER.warn("Selectium Mod WebSocket exceeded message rate limit. Closing connection.");
                close();
                return null;
            }

            try
            {
                JsonObject message = GSON.fromJson(fullMessage, JsonObject.class);

                switch (message.get("action").getAsString())
                {
                    case "update_list_players" -> handleListUpdate(ConfigManager.getInstance(), message);
                    case "add_list_player" -> handleListAdd(ConfigManager.getInstance(), message);
                    case "edit_list_player" -> handleListEdit(ConfigManager.getInstance(), message);
                    case "remove_list_player" -> handleListRemove(ConfigManager.getInstance(), message);
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
} 