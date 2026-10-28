package net.byws.selectiummod;

import com.google.gson.Gson;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.byws.selectiummod.command.SelectiumCommand;
import net.byws.selectiummod.config.ConfigManager;
import net.byws.selectiummod.websocket.DiscordConnection;
import net.byws.selectiummod.config.KillTracker;

public class SelectiumMod implements ClientModInitializer
{
	public static final String MOD_ID = "selectium-mod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final Gson GSON = new Gson();

	@Override
	public void onInitializeClient()
	{
		ConfigManager.init(LOGGER, GSON);
		KillTracker.init();
		DiscordConnection.init(LOGGER, GSON);

		ConfigManager configManager = ConfigManager.getInstance();
		KillTracker killTracker = KillTracker.getInstance();
		DiscordConnection discordConnection = DiscordConnection.getInstance();

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
				SelectiumCommand.register(dispatcher));

		ClientReceiveMessageEvents.GAME.register((component, overlay) ->
		{
			if (!overlay) killTracker.update(component.getString());
		});

		ClientTickEvents.START_CLIENT_TICK.register(client ->
		{
			discordConnection.updateWebsocket();
			configManager.updatePlayerData();
		});

		ClientLifecycleEvents.CLIENT_STOPPING.register(client ->
		{
			discordConnection.shutdown();
			configManager.flush();
		});
	}
}
