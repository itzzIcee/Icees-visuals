package com.icees.visuals.mixin;

import com.icees.visuals.effects.CustomSkyRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.world.level.MoonPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Hide vanilla sky disc / sun / stars while a custom sky is selected. */
@Mixin(SkyRenderer.class)
public class SkyRendererMixin {
	@Inject(method = "renderSkyDisc", at = @At("HEAD"), cancellable = true)
	private void icees$hideSkyDisc(int color, CallbackInfo ci) {
		if (CustomSkyRenderer.isActive()) ci.cancel();
	}

	@Inject(method = "renderDarkDisc", at = @At("HEAD"), cancellable = true)
	private void icees$hideDarkDisc(CallbackInfo ci) {
		if (CustomSkyRenderer.isActive()) ci.cancel();
	}

	@Inject(method = "renderSunMoonAndStars", at = @At("HEAD"), cancellable = true)
	private void icees$hideSunMoonStars(PoseStack poseStack, float sunAngle, float rain, float thunder,
		MoonPhase moonPhase, float starBrightness, float timeOfDay, CallbackInfo ci) {
		if (CustomSkyRenderer.isActive()) ci.cancel();
	}

	@Inject(method = "renderSunriseAndSunset", at = @At("HEAD"), cancellable = true)
	private void icees$hideSunrise(PoseStack poseStack, float sunAngle, int color, CallbackInfo ci) {
		if (CustomSkyRenderer.isActive()) ci.cancel();
	}

	@Inject(method = "renderEndSky", at = @At("HEAD"), cancellable = true)
	private void icees$hideEndSky(CallbackInfo ci) {
		if (CustomSkyRenderer.isActive()) ci.cancel();
	}
}
