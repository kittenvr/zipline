package com.claude;

import com.claude.commands.CommandHandler;
import com.claude.config.ModConfig;
import com.claude.pathfinding.TeleportPathExecutor;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import org.slf4j.Logger;

public class Zipline implements ClientModInitializer {
	public static final Logger LOGGER = LogUtils.getLogger();
	public static final TeleportPathExecutor EXECUTOR = new TeleportPathExecutor();
	public static final ModConfig CONFIG = new ModConfig();


	@Override
	public void onInitializeClient() {
		LOGGER.info("Zipline loaded.");
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			CommandHandler.register(dispatcher);
		});


	}
}
