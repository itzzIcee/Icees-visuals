package com.icees.visuals.mixin;

import com.icees.visuals.IceesVisualsClient;
import com.icees.visuals.config.IceesConfig;
import com.icees.visuals.sky.SakuraSkyRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Draws sakura skies after vanilla sky — does not cancel vanilla (avoids fog/depth crashes).
 */
@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
	@Inject(
		method = "renderSky(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V",
		at = @At("RETURN")
	)
	private void icees$afterSky(Matrix4f positionMatrix, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean thickFog, Runnable fogCallback, CallbackInfo ci) {
		if (!IceesConfig.customSky || thickFog) {
			return;
		}
		try {
			MatrixStack matrices = new MatrixStack();
			matrices.multiplyPositionMatrix(positionMatrix);
			SakuraSkyRenderer.render(matrices, tickDelta);
		} catch (Throwable t) {
			IceesConfig.customSky = false;
			IceesVisualsClient.LOGGER.error("Sakura sky crashed — disabled custom sky. Open Orbit (Right Shift) to re-enable after fixing.", t);
		}
	}
}
