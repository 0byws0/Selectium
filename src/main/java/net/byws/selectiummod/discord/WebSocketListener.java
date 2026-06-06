package net.byws.selectiummod.discord;

import com.google.gson.Gson;
import net.byws.selectiummod.config.ConfigJsonObj;
import net.byws.selectiummod.config.ConfigManager;

import java.net.http.WebSocket;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentSkipListMap;

import net.byws.selectiummod.config.ConfigJsonObj;

public class WebSocketListener implements WebSocket.Listener
{
    private static Gson GSON = new Gson();

    private volatile ConcurrentSkipListMap<UUID, ConfigJsonObj.PlayerData> sharedPlayerData = new ConcurrentSkipListMap<UUID, ConfigJsonObj.PlayerData>();

    public void onOpen(WebSocket websocket)
    {
        System.out.println("Connection established");
        websocket.request(1);
    }

    public void updateConfig()
    {
//        if (sharedPlayerData.isEmpty()) return;
//
//        var playerEntry = sharedPlayerData.pollFirstEntry();
//        var player = ConfigManager.getPlayerData());
//
//        if ( == null)
//        {
//
//        }

    }

    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last)
    {
//        sharedPlayerData = new ConcurrentSkipListMap<>(GSON.fromJson(data.toString(), Map.class));
        webSocket.request(1);
        return null;
    }
}
