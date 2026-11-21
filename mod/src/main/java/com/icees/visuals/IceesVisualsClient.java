package com.icees.visuals;

import com.icees.visuals.config.IceesConfig;
import com.icees.visuals.config.VisualPresets;
import com.icees.visuals.cosmetics.CosmeticRenderer;
import com.icees.visuals.cosmetics.MovementTrailRenderer;
import com.icees.visuals.cpvp.MotionBlurRenderer;
import com.icees.visuals.effects.FallParticleRenderer;
import com.icees.visuals.gui.OrbitScreen;
import com.icees.visuals.keybind.IceesKeybinds;
import com.icees.visuals.network.CosmeticClientSync;
import com.icees.visuals.overlay.BlockOverlayRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IceesVisualsClient implements ClientModInitializer {
	public static final String MOD_ID = "icees-visuals";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
		try {
			IceesConfig.load();
		} catch (Throwable t) {
			LOGGER.error("Config load failed — using defaults", t);
		}
		try {
			VisualPresets.load();
		} catch (Throwable t) {
			LOGGER.error("Presets load failed", t);
		}
		try {
			IceesKeybinds.register();
		} catch (Throwable t) {
			LOGGER.error("Keybind register failed", t);
		}
		try {
			CosmeticClientSync.register();
		} catch (Throwable t) {
			LOGGER.error("Cosmetic sync register failed", t);
		}

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			try {
				if (IceesKeybinds.OPEN_ORBIT != null) {
					while (IceesKeybinds.OPEN_ORBIT.consumeClick()) {
						if (client.screen == null) {
							client.setScreen(new OrbitScreen());
						}
					}
				}
				FallParticleRenderer.tick(client);
				MovementTrailRenderer.tick(client);
				CosmeticClientSync.tick(client);
				IceesConfig.applyFullbright(client);
			} catch (Throwable t) {
				LOGGER.warn("Client tick visuals failed", t);
			}
		});

		try {
			WorldRenderEvents.AFTER_ENTITIES.register(CosmeticRenderer::render);
			WorldRenderEvents.AFTER_ENTITIES.register(MovementTrailRenderer::render);
			WorldRenderEvents.AFTER_ENTITIES.register(FallParticleRenderer::render);
			WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register(BlockOverlayRenderer::render);
		} catch (Throwable t) {
			LOGGER.error("World render hooks failed", t);
		}

		try {
			HudElementRegistry.attachElementAfter(
				VanillaHudElements.MISC_OVERLAYS,
				Identifier.fromNamespaceAndPath(MOD_ID, "motion_blur"),
				(graphics, tickCounter) -> MotionBlurRenderer.render(graphics)
			);
		} catch (Throwable t) {
			LOGGER.error("HUD hook failed", t);
		}

		LOGGER.info("Icee's Visuals 1.0.52 — cosmetics sync between players");
	}
}
