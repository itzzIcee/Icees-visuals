package com.icees.visuals.mixin;

import com.icees.visuals.config.IceesConfig;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Low Particles (FPS) — drop most ambient client particles. */
@Mixin(ClientLevel.class)
public class ClientLevelMixin {
	@Inject(method = "addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V", at = @At("HEAD"), cancellable = true)
	private void icees$lowParticles(ParticleOptions options, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, CallbackInfo ci) {
		if (IceesConfig.lowParticles) ci.cancel();
	}
}
