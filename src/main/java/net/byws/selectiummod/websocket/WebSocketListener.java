package net.byws.selectiummod.websocket;

import net.minecraft.client.Minecraft;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;

import net.byws.selectiummod.config.ConfigManager;
import net.byws.selectiummod.config.ConfigJsonObj;

public class WebSocketListener implements WebSocket.Listener
{
    private static final int PING_INTERVAL_MS = 60000;
    private static volatile WebSocketListener INSTANCE;
    private final Logger LOGGER;
    private final Gson GSON;

    private final Map<String, BiConsumer<ConfigManager, JsonObject>> actionHandlers;
    private volatile WebSocket discord_conn;
    private volatile boolean isConnecting, isDisconnecting;
    private long lastPingMillis;
    private final StringBuilder messageBuffer;
    private final ConcurrentLinkedQueue<Runnable> updateQueue;

    private WebSocketListener(Logger modLogger, Gson gson)
    {
        this.LOGGER = modLogger;
        this.GSON = gson;
        this.isConnecting = false;
        this.isDisconnecting = false;
        this.lastPingMillis = 0;
        this.messageBuffer = new StringBuilder();
        this.updateQueue = new ConcurrentLinkedQueue<>();

        this.actionHandlers = new HashMap<>();
        this.actionHandlers.put("update_list_players", this::handleListUpdate);
        this.actionHandlers.put("add_list_player", this::handleListAdd);
        this.actionHandlers.put("edit_list_player", this::handleListEdit);
        this.actionHandlers.put("remove_list_player", this::handleListRemove);
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
    public void onOpen(WebSocket websocket)
    {
        LOGGER.info("Selectium Mod WebSocket connection established");
        websocket.request(1);
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason)
    {
        LOGGER.info("Selectium Mod WebSocket closed with status code {} and reason: {}", statusCode, reason);
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
            if (isConnecting || isDisconnecting || discord_conn != null) return CompletableFuture.failedFuture(new IllegalStateException("Already connecting"));
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

    public CompletableFuture<WebSocket> close()
    {
        synchronized (this)
        {
            if (isConnecting || isDisconnecting || discord_conn == null) return CompletableFuture.failedFuture(new IllegalStateException("Not connected"));
            isDisconnecting = true;
        }
        return discord_conn.sendClose(1000, "Normal closure");
    }

    public void update()
    {
        Runnable nextUpdate = updateQueue.poll();
        if (nextUpdate != null) nextUpdate.run();

        long currentMillis = System.currentTimeMillis();
        if (discord_conn != null && (currentMillis - lastPingMillis) > PING_INTERVAL_MS)
        {
            discord_conn.sendPong(StandardCharsets.UTF_8.encode("heartbeat"));
            lastPingMillis = currentMillis;
        }
    }

    private void handleListUpdate(ConfigManager configManager, JsonObject message)
    {
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
    }

    private void handleListAdd(ConfigManager configManager, JsonObject message)
    {
        Map<UUID, ConfigJsonObj.PlayerData> serverPlayerList = GSON.fromJson(message.get("payload"),
                new TypeToken<Map<UUID, ConfigJsonObj.PlayerData>>(){}.getType());
        var clientPlayerList = Minecraft.getInstance().submit(() -> configManager.getPlayerList()).join();

        serverPlayerList.forEach((playerUUID, updatedPlayer) ->
        {
            final boolean clientSide = updatedPlayer.clientSide;
            final ConfigJsonObj.Category type = updatedPlayer.type;
            final String nickname = updatedPlayer.nickname;

            if (clientPlayerList.get(playerUUID) == null) updateQueue.add(() -> configManager.addListPlayer(playerUUID, clientSide, type, nickname));
        });
    }

    private void handleListEdit(ConfigManager configManager, JsonObject message)
    {
        Map<UUID, ConfigJsonObj.PlayerData> serverPlayerList = GSON.fromJson(message.get("payload"),
                new TypeToken<Map<UUID, ConfigJsonObj.PlayerData>>(){}.getType());
        var clientPlayerList = Minecraft.getInstance().submit(() -> configManager.getPlayerList()).join();

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
    }

    private void handleListRemove(ConfigManager configManager, JsonObject message)
    {
        Map<UUID, ConfigJsonObj.PlayerData> serverPlayerList = GSON.fromJson(message.get("payload"),
                new TypeToken<Map<UUID, ConfigJsonObj.PlayerData>>(){}.getType());
        var clientPlayerList = Minecraft.getInstance().submit(() -> configManager.getPlayerList()).join();

        serverPlayerList.forEach((playerUUID, updatedPlayer) ->
        {
            ConfigJsonObj.PlayerData existingPlayer =  clientPlayerList.get(playerUUID);
            if (existingPlayer != null && !existingPlayer.clientSide) updateQueue.add(() -> configManager.removeListPlayer(playerUUID));
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
            try
            {
                JsonObject message = GSON.fromJson(fullMessage, JsonObject.class);
                actionHandlers.get(message.get("action").getAsString()).accept(ConfigManager.getInstance(), message);
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
