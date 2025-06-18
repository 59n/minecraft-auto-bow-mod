package com.jacktheape.autobow;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoBowMod implements ModInitializer {
	public static final String MOD_ID = "autobow";
	private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Auto Bow Mod initialized!");
	}
}
