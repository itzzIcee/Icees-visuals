package com.icees.visuals;

import com.icees.visuals.config.IceesConfig;
import com.icees.visuals.cosmetics.CosmeticRenderer;
import com.icees.visuals.gui.OrbitScreen;
import com.icees.visuals.keybind.IceesKeybinds;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IceesVisualsClient implements ClientModInitializer {
	public static final String MOD_ID = "icees-visuals";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
		IceesConfig.load();
		IceesKeybinds.register();

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (IceesKeybinds.OPEN_ORBIT.wasPressed()) {
				if (client.currentScreen == null) {
					client.setScreen(new OrbitScreen());
				}
			}
		});

		WorldRenderEvents.AFTER_ENTITIES.register(context -> {
			MinecraftClient client = MinecraftClient.getInstance();
			if (client.player == null || client.world == null) {
				return;
			}
			CosmeticRenderer.render(context, client.player);
		});

		LOGGER.info("Icees Visuals ready — sakura skies, cosmetics & Orbit (Right Shift)");
	}
}
