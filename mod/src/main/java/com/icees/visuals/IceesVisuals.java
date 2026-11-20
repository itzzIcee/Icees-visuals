package com.icees.visuals;

import com.icees.visuals.network.CosmeticNetwork;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Common / server entry — relays cosmetic sync between players who have the mod. */
public final class IceesVisuals implements ModInitializer {
	public static final String MOD_ID = "icees-visuals";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		CosmeticNetwork.registerCommon();
		CosmeticNetwork.registerServer();
		LOGGER.info("Icee's Sakura Visuals — cosmetic sync relay ready");
	}
}
