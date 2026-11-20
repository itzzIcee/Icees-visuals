package com.icees.visuals.mixin;

import com.icees.visuals.config.IceesConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Tints atmospheric fog to match the selected Icees sky. */
@Mixin(AtmosphericFogEnvironment.class)
public class AtmosphericFogMixin {
	@Inject(method = "getBaseColor", at = @At("RETURN"), cancellable = true)
	private void icees$tintFog(ClientLevel level, Camera camera, int i, float f, CallbackInfoReturnable<Integer> cir) {
		if (!IceesConfig.customSky) return;
		cir.setReturnValue(IceesConfig.skyStyle.toFogArgb());
	}
}
