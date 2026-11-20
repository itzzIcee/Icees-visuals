package com.icees.visuals;

import com.icees.visuals.config.IceesConfig;
import com.icees.visuals.cosmetics.CosmeticRenderer;
import com.icees.visuals.cosmetics.KnifeHudRenderer;
import com.icees.visuals.cpvp.MotionBlurRenderer;
import com.icees.visuals.effects.FallParticleRenderer;
import com.icees.visuals.gui.OrbitScreen;
import com.icees.visuals.keybind.IceesKeybinds;
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
		IceesConfig.load();
		IceesKeybinds.register();

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (IceesKeybinds.OPEN_ORBIT.consumeClick()) {
				if (client.screen == null) {
					client.setScreen(new OrbitScreen());
				}
			}
			FallParticleRenderer.tick(client);
			IceesConfig.applyFullbright(client);
		});

		WorldRenderEvents.AFTER_ENTITIES.register(CosmeticRenderer::render);
		WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register(BlockOverlayRenderer::render);

		HudElementRegistry.attachElementAfter(
			VanillaHudElements.MISC_OVERLAYS,
			Identifier.fromNamespaceAndPath(MOD_ID, "fall_themes"),
			(graphics, tickCounter) -> FallParticleRenderer.render(graphics, tickCounter.getGameTimeDeltaPartialTick(false))
		);
		HudElementRegistry.attachElementAfter(
			VanillaHudElements.MISC_OVERLAYS,
			Identifier.fromNamespaceAndPath(MOD_ID, "knife_hud"),
			(graphics, tickCounter) -> KnifeHudRenderer.render(graphics)
		);
		HudElementRegistry.attachElementAfter(
			VanillaHudElements.MISC_OVERLAYS,
			Identifier.fromNamespaceAndPath(MOD_ID, "motion_blur"),
			(graphics, tickCounter) -> MotionBlurRenderer.render(graphics)
		);

		LOGGER.info("Icee's Sakura Visuals 1.0.7 — Right Shift opens Orbit");
	}
}
