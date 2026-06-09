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

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last)
    {
        try
        {
            Map<UUID, ConfigJsonObj.PlayerData> incomingData = GSON.fromJson(data.toString(),
                    new TypeToken<Map<UUID, ConfigJsonObj.PlayerData>>(){}.getType());

            if (incomingData != null) ConfigManager.getInstance().setServerList(incomingData);
        }
        catch (Exception e)
        {
            LOGGER.error("Selectium Mod failed to parse websocket data: {}", e.getMessage());
        }

        webSocket.request(1);
        return null;
    }
}
