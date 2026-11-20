package com.icees.visuals;

import com.icees.visuals.config.IceesConfig;
import com.icees.visuals.effects.FallParticleRenderer;
import com.icees.visuals.gui.OrbitScreen;
import com.icees.visuals.keybind.IceesKeybinds;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
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
			while (IceesKeybinds.OPEN_ORBIT.consumeClick()) {
				if (client.screen == null) {
					client.setScreen(new OrbitScreen());
				}
			}
			FallParticleRenderer.tick(client);
		});

		HudRenderCallback.EVENT.register((graphics, tickCounter) -> {
			FallParticleRenderer.render(graphics, 0f);
		});

		LOGGER.info("Icee's Sakura Visuals 1.0.1 for Minecraft 1.21.11 — Right Shift opens Orbit");
	}
}
