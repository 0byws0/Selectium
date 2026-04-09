package net.byws.selectiummod;

import net.byws.selectiummod.command.SelectiumCommand;
import net.byws.selectiummod.config.ConfigManger;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SelectiumMod implements ModInitializer
{

	public static final String MOD_ID = "selectium-mod";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize()
	{
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		ConfigManger.init(MOD_ID);

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
				SelectiumCommand.register(dispatcher));
	}
}