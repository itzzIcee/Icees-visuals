package com.icees.visuals.mixin;

import com.icees.visuals.config.IceesConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.SkyRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Overrides extracted sky colour so every Icees sky style actually changes the sky disc.
 */
@Mixin(SkyRenderer.class)
public class SkyRendererMixin {
	@Inject(method = "extractRenderState", at = @At("RETURN"))
	private void icees$applySkyStyle(ClientLevel level, float partialTick, Camera camera, SkyRenderState state, CallbackInfo ci) {
		if (!IceesConfig.customSky) {
			return;
		}
		state.skyColor = IceesConfig.skyStyle.toSkyArgb();
		state.sunriseAndSunsetColor = IceesConfig.skyStyle.toSunriseArgb();
		// Keep stars visible on dark skies
		if (IceesConfig.skyStyle == com.icees.visuals.sky.SkyStyle.GALAXY
			|| IceesConfig.skyStyle == com.icees.visuals.sky.SkyStyle.NOCTURNE
			|| IceesConfig.skyStyle == com.icees.visuals.sky.SkyStyle.AMETHYST) {
			state.starBrightness = Math.max(state.starBrightness, 0.85f);
		}
	}
}
