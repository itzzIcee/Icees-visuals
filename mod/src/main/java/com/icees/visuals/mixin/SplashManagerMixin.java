package com.icees.visuals.mixin;

import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.client.resources.SplashManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Title-screen yellow splash → "Visuals by Icee" while the mod is loaded. */
@Mixin(SplashManager.class)
public class SplashManagerMixin {
	private static final SplashRenderer ICEE_SPLASH = new SplashRenderer(
		Component.literal("Visuals by Icee").withStyle(Style.EMPTY.withColor(-256))
	);

	@Inject(method = "getSplash", at = @At("HEAD"), cancellable = true)
	private void icees$splash(CallbackInfoReturnable<SplashRenderer> cir) {
		cir.setReturnValue(ICEE_SPLASH);
	}
}
