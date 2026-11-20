package com.icees.visuals.mixin;

import com.icees.visuals.config.IceesConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.SkyRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Forces Icees sky colours onto the sky disc + extracted render state.
 */
@Mixin(SkyRenderer.class)
public class SkyRendererMixin {
	@Inject(method = "extractRenderState", at = @At("RETURN"))
	private void icees$applySkyStyle(ClientLevel level, float partialTick, Camera camera, SkyRenderState state, CallbackInfo ci) {
		if (!IceesConfig.customSky) return;
		state.skyColor = IceesConfig.skyStyle.toSkyArgb();
		state.sunriseAndSunsetColor = IceesConfig.skyStyle.toSunriseArgb();
		// Keep disc visible (don't let horizon dark-disc eat the colour)
		state.shouldRenderDarkDisc = false;
		switch (IceesConfig.skyStyle) {
			case GALAXY, NOCTURNE, AMETHYST -> state.starBrightness = Math.max(state.starBrightness, 0.95f);
			case SAKURA, FIRE, OPALINE -> state.starBrightness = Math.min(state.starBrightness, 0.15f);
			default -> {}
		}
	}

	/** Belt-and-suspenders: override the colour argument right before the disc draws. */
	@ModifyVariable(method = "renderSkyDisc", at = @At("HEAD"), argsOnly = true)
	private int icees$forceSkyDiscColor(int color) {
		if (!IceesConfig.customSky) return color;
		return IceesConfig.skyStyle.toSkyArgb();
	}
}
