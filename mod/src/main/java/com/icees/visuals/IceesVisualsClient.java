package com.icees.visuals;

import com.icees.visuals.config.IceesConfig;
import com.icees.visuals.cosmetics.CosmeticRenderer;
import com.icees.visuals.cosmetics.KnifeHudRenderer;
import com.icees.visuals.effects.FallParticleRenderer;
import com.icees.visuals.gui.OrbitScreen;
import com.icees.visuals.keybind.IceesKeybinds;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.resources.Identifier;
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

		WorldRenderEvents.AFTER_ENTITIES.register(CosmeticRenderer::render);

		// Reliable HUD path for 1.21.11 (deprecated HudRenderCallback often does not fire)
		HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "fall_themes"), (graphics, tickCounter) -> {
			FallParticleRenderer.render(graphics, tickCounter.getGameTimeDeltaPartialTick(false));
		});
		HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "knife_hud"), (graphics, tickCounter) -> {
			KnifeHudRenderer.render(graphics);
		});

		LOGGER.info("Icee's Sakura Visuals 1.0.3 — Right Shift opens Orbit");
	}
}
