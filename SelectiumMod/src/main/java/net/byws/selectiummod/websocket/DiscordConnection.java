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
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import net.byws.selectiummod.config.ConfigManager;
import net.byws.selectiummod.config.ConfigJsonObj;
import net.byws.selectiummod.config.KillTracker;

public class DiscordConnection
{
    private class WebSocketSession implements WebSocket.Listener
    {
        private WebSocket socket;
        private CompletableFuture<Void> opened, closed;
        private final RateLimiter rateLimiter = RateLimiter.create(10);
        private final StringBuilder messageBuffer = new StringBuilder();

        private WebSocketSession()
        {
            this.opened = new CompletableFuture<>();
            this.closed = null;
        }

        @Override
        public void onOpen(WebSocket webSocket)
        {
            socket = webSocket;
            Minecraft.getInstance().execute(() -> handleOpen(this));
            webSocket.request(1);
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason)
        {
            Minecraft.getInstance().execute(() -> handleClose(this, statusCode, reason));
            return null;
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) { Minecraft.getInstance().execute(() -> handleFailure(this, error)); }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last)
        {
            if ((messageBuffer.length() + data.length()) * 2 > MAX_MSG_SIZE)
            {
                Minecraft.getInstance().execute(() ->
                        handleFailure(this, new IllegalStateException("WebSocket message exceeded the size limit")));
                return null;
            }

            messageBuffer.append(data);
            if (last)
            {
                if (!rateLimiter.tryAcquire())
                {
                    Minecraft.getInstance().execute(() ->
                            handleFailure(this, new IllegalStateException("WebSocket message rate limit exceeded")));
                    return null;
                }
                String fullMessage = messageBuffer.toString();
                messageBuffer.setLength(0);
                Minecraft.getInstance().execute(() -> handleMessage(this, fullMessage));
            }

            webSocket.request(1);
            return null;
        }
    }

    private static final String DISCORD_WS_URL = "wss://bywsdomain.xyz/ws";
    private static final int MAX_MSG_SIZE = 200000;
    private static final long WS_UPDATE_INTERVAL = 80000000000L;
    private static final TypeToken UPSERT_FORMAT = new TypeToken<Map<UUID, ConfigJsonObj.PlayerData>>(){};
    private static final TypeToken REMOVE_FORMAT = new TypeToken<Set<UUID>>(){};

    private static DiscordConnection instance;
    private final Logger logger;
    private final Gson gson;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private WebSocketSession activeSession;
    private String connUUID;
    private long lastPushNanos;

    private DiscordConnection(Logger modLogger, Gson gson)
    {
        this.logger = modLogger;
        this.gson = gson;
        this.activeSession = null;
        this.connUUID = "";
    }

    public static void init(Logger modLogger, Gson gson)
    {
        if (instance != null) return;
        instance = new DiscordConnection(modLogger, gson);
    }

    public static DiscordConnection getInstance()
    {
        if (instance == null) throw new IllegalStateException("Websocket not initialized");
        return instance;
    }

    public boolean isConnecting() { return activeSession != null && !activeSession.opened.isDone(); }

    public boolean isConnected() { return activeSession != null && activeSession.opened.isDone() && activeSession.closed == null; }

    public boolean isDisconnecting() { return activeSession != null && activeSession.closed != null; }

    public boolean isDisconnected() { return activeSession == null; }

    public CompletableFuture<Void> connect()
    {
        if (!isDisconnected()) return CompletableFuture.failedFuture(new IllegalStateException("Cannot connect in current state"));

        connUUID = Minecraft.getInstance().getUser().getProfileId().toString();

        String token = ConfigManager.getInstance().getToken();
        if (token == null) return CompletableFuture.failedFuture(new IllegalStateException("No discord token"));

        WebSocketSession session = new WebSocketSession();
        activeSession = session;

        httpClient.newWebSocketBuilder()
                .header("uuid", connUUID)
                .header("token", token)
                .buildAsync(URI.create(DISCORD_WS_URL), session)
                .whenComplete((webSocket, error) ->
                {
                    if (error != null) Minecraft.getInstance().execute(() -> handleFailure(session, error));
                });

        return session.opened;
    }

    public CompletableFuture<Void> disconnect()
    {
        if (!isConnected()) return CompletableFuture.failedFuture(new IllegalStateException("Not connected"));

        WebSocketSession session = activeSession;
        session.closed = new CompletableFuture<>();
        session.socket
                .sendClose(WebSocket.NORMAL_CLOSURE, "Normal closure")
                .whenComplete((webSocket, error) ->
                {
                    if (error != null) Minecraft.getInstance().execute(() -> handleFailure(session, error));
                });

        return session.closed;
    }

    public void updateWebsocket()
    {
        if (!Minecraft.getInstance().getUser().getProfileId().toString().equals(connUUID))
        {
            disconnect();
            connUUID = "";
        }
        if (!isConnected())
        {
            if (connUUID.isEmpty()) connect();
            return;
        }

        long currentNanos = System.nanoTime();

        if ((currentNanos - lastPushNanos) > WS_UPDATE_INTERVAL)
        {
            activeSession.socket.sendText(gson.toJson(Map.of(
                    "action", "update_player_data",
                    "payload", Map.of("kills", KillTracker.getInstance().serverSideKills())
            )), true);
            lastPushNanos = currentNanos;
        }
    }

    private void handleOpen(WebSocketSession session)
    {
        if (activeSession != session)
        {
            session.socket.abort();
            return;
        }

        lastPushNanos = System.nanoTime();
        session.opened.complete(null);
    }

    private void handleClose(WebSocketSession session, int statusCode, String reason)
    {
        if (activeSession != session) return;

        activeSession = null;

        if (!session.opened.isDone()) session.opened.completeExceptionally(new IllegalStateException("WebSocket closed before opening: " + reason));
        if (session.closed != null) session.closed.complete(null);

        logger.info("Discord websocket closed ({}): {}", statusCode, reason);
    }

    private void handleFailure(WebSocketSession session, Throwable error)
    {
        if (activeSession != session) return;

        activeSession = null;
        if (session.socket != null)
        {
            session.socket.abort();
            connUUID = "";
        }

        session.opened.completeExceptionally(error);
        if (session.closed != null) session.closed.completeExceptionally(error);

        logger.error("Discord websocket failed: {}", error.getMessage());
    }

    private void handleMessage(WebSocketSession session, String jsonMsg)
    {
        if (activeSession != session || !isConnected()) return;

        try
        {
            JsonObject message = gson.fromJson(jsonMsg, JsonObject.class);

            switch (message.get("action").getAsString())
            {
                case "update_list_players" -> handleListUpsert(message, true);
                case "add_list_player", "edit_list_player" -> handleListUpsert(message, false);
                case "remove_list_player" -> handleListRemove(message);
            }
        }
        catch (Exception e)
        {
            logger.error("Selectium Mod failed to parse websocket data: {}", e.getMessage());
        }
    }

    private void handleListUpsert(JsonObject message, boolean removeMissing)
    {
        Map<UUID, ConfigJsonObj.PlayerData> serverPlayerList = gson.fromJson(message.get("payload"), UPSERT_FORMAT.getType());
        ConfigManager configManager = ConfigManager.getInstance();
        var clientPlayerList = configManager.getPlayerList();

        if (removeMissing)
        {
            for (var playerEntry : clientPlayerList.entrySet())
            {
                UUID playerUUID = playerEntry.getKey();
                if (!serverPlayerList.containsKey(playerUUID)) configManager.removeListPlayer(playerUUID, false);
            }
        }

        serverPlayerList.forEach((playerUUID, updatedPlayer) ->
        {
            if (!configManager.addListPlayer(playerUUID, false, updatedPlayer.serverType, updatedPlayer.group, updatedPlayer.nickname))
            {
                configManager.editListPlayer(playerUUID, false, updatedPlayer.serverType, updatedPlayer.group, updatedPlayer.nickname);
            }
        });
    }

    private void handleListRemove(JsonObject message)
    {
        Set<UUID> serverPlayerList = gson.fromJson(message.get("payload"), REMOVE_FORMAT.getType());
        ConfigManager configManager = ConfigManager.getInstance();

        serverPlayerList.forEach((playerUUID) ->
        {
            configManager.removeListPlayer(playerUUID, false);
        });
    }
}
