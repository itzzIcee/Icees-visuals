package com.icees.visuals.mixin;

import com.icees.visuals.config.IceesConfig;
import com.icees.visuals.sky.SakuraSkyRenderer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
	@Inject(method = "renderSky(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V", at = @At("HEAD"), cancellable = true, require = 0)
	private void icees$renderSky(Matrix4f positionMatrix, Matrix4f projectionMatrix, float tickDelta, net.minecraft.client.render.Camera camera, boolean thickFog, Runnable fogCallback, CallbackInfo ci) {
		if (!IceesConfig.customSky) {
			return;
		}
		MatrixStack matrices = new MatrixStack();
		matrices.multiplyPositionMatrix(positionMatrix);
		fogCallback.run();
		SakuraSkyRenderer.render(matrices, tickDelta);
		ci.cancel();
	}
}
