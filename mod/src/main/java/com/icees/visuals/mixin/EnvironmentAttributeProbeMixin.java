package com.icees.visuals.mixin;

import com.icees.visuals.config.IceesConfig;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeProbe;
import net.minecraft.world.attribute.EnvironmentAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Force sky + fog (+ sunrise) colours. Clouds stay vanilla so they don't turn into
 * a stuck grey panoramic wash.
 */
@Mixin(EnvironmentAttributeProbe.class)
public class EnvironmentAttributeProbeMixin {
	@Inject(method = "getValue", at = @At("HEAD"), cancellable = true)
	private <Value> void icees$overrideVisuals(EnvironmentAttribute<Value> attribute, float partialTick, CallbackInfoReturnable<Value> cir) {
		if (!IceesConfig.customSky) return;

		if (attribute == EnvironmentAttributes.SKY_COLOR) {
			@SuppressWarnings("unchecked")
			Value v = (Value) (Object) IceesConfig.skyStyle.toSkyArgb();
			cir.setReturnValue(v);
			return;
		}
		if (attribute == EnvironmentAttributes.FOG_COLOR) {
			@SuppressWarnings("unchecked")
			Value v = (Value) (Object) IceesConfig.skyStyle.toFogArgb();
			cir.setReturnValue(v);
			return;
		}
		if (attribute == EnvironmentAttributes.SUNRISE_SUNSET_COLOR) {
			@SuppressWarnings("unchecked")
			Value v = (Value) (Object) IceesConfig.skyStyle.toSunriseArgb();
			cir.setReturnValue(v);
		}
	}
}
