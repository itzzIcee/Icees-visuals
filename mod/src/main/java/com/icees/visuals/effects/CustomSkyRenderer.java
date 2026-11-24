package com.icees.visuals.effects;

import com.icees.visuals.config.IceesConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

/**
 * Panoramic custom sky dome — world-locked, optional slow spin / UV drift when Animated is on.
 */
public final class CustomSkyRenderer {
	private static final int SEG_H = 48;
	private static final int SEG_V = 24;
	private static final float RADIUS = 90f;

	private CustomSkyRenderer() {}

	public static boolean isActive() {
		return IceesConfig.skyStyle != null && IceesConfig.skyStyle.isCustom();
	}

	public static void render(WorldRenderContext context) {
		if (!isActive()) return;
		Minecraft client = Minecraft.getInstance();
		if (client.level == null || client.player == null) return;

		Identifier tex = IceesConfig.skyStyle.textureId();
		if (tex == null) return;

		MultiBufferSource consumers = context.consumers();
		PoseStack matrices = context.matrices();
		if (consumers == null || matrices == null) return;

		Camera camera = client.gameRenderer.getMainCamera();
		float partial = client.getDeltaTracker().getGameTimeDeltaPartialTick(false);
		float time = client.level.getGameTime() + partial;
		float bright = 0.55f + IceesConfig.clamp10(IceesConfig.skyBrightness) * 0.05f;
		float speed = 0.15f + IceesConfig.clamp10(IceesConfig.skySpeed) * 0.12f;
		boolean animated = IceesConfig.skyAnimated;

		// Animated: swirl UV scroll + slow dome spin. Static: world-locked eyes layer.
		VertexConsumer vc = animated
			? consumers.getBuffer(RenderTypes.energySwirl(tex, time * 0.0007f * speed, time * 0.00025f * speed))
			: consumers.getBuffer(RenderTypes.eyes(tex));

		matrices.pushPose();
		// Counter camera so the dome stays world-locked
		matrices.mulPose(Axis.XP.rotationDegrees(camera.xRot()));
		matrices.mulPose(Axis.YP.rotationDegrees(-camera.yRot()));
		if (animated) {
			matrices.mulPose(Axis.YP.rotationDegrees(time * 0.45f * speed));
			matrices.mulPose(Axis.ZP.rotationDegrees(Mth.sin(time * 0.025f * speed) * 3.5f));
			matrices.mulPose(Axis.XP.rotationDegrees(Mth.sin(time * 0.018f * speed) * 2f));
		}

		drawDome(matrices, vc, bright);
		// Soft second pass when animated so motion reads clearly
		if (animated) {
			matrices.mulPose(Axis.YP.rotationDegrees(18f + time * 0.12f * speed));
			VertexConsumer soft = consumers.getBuffer(RenderTypes.eyes(tex));
			drawDome(matrices, soft, bright * 0.35f);
		}
		matrices.popPose();
	}

	/** Inward-facing sphere (equirectangular UV). */
	private static void drawDome(PoseStack matrices, VertexConsumer vc, float bright) {
		PoseStack.Pose pose = matrices.last();
		Matrix4f m = pose.pose();
		int light = LightTexture.FULL_BRIGHT;
		int overlay = OverlayTexture.NO_OVERLAY;

		for (int iv = 0; iv < SEG_V; iv++) {
			float v0 = iv / (float) SEG_V;
			float v1 = (iv + 1) / (float) SEG_V;
			float pitch0 = (float) Math.PI * (v0 - 0.5f);
			float pitch1 = (float) Math.PI * (v1 - 0.5f);
			float y0 = Mth.sin(pitch0) * RADIUS;
			float y1 = Mth.sin(pitch1) * RADIUS;
			float r0 = Mth.cos(pitch0) * RADIUS;
			float r1 = Mth.cos(pitch1) * RADIUS;

			for (int ih = 0; ih < SEG_H; ih++) {
				float u0 = ih / (float) SEG_H;
				float u1 = (ih + 1) / (float) SEG_H;
				float yaw0 = u0 * ((float) Math.PI * 2f);
				float yaw1 = u1 * ((float) Math.PI * 2f);

				float x00 = Mth.cos(yaw0) * r0, z00 = Mth.sin(yaw0) * r0;
				float x10 = Mth.cos(yaw1) * r0, z10 = Mth.sin(yaw1) * r0;
				float x01 = Mth.cos(yaw0) * r1, z01 = Mth.sin(yaw0) * r1;
				float x11 = Mth.cos(yaw1) * r1, z11 = Mth.sin(yaw1) * r1;

				// Flip winding so faces look inward; V flipped for panorama upright
				float tv0 = 1f - v0;
				float tv1 = 1f - v1;
				vert(vc, m, pose, x00, y0, z00, u0, tv0, bright, light, overlay);
				vert(vc, m, pose, x01, y1, z01, u0, tv1, bright, light, overlay);
				vert(vc, m, pose, x11, y1, z11, u1, tv1, bright, light, overlay);
				vert(vc, m, pose, x10, y0, z10, u1, tv0, bright, light, overlay);
			}
		}
	}

	private static void vert(VertexConsumer vc, Matrix4f m, PoseStack.Pose pose,
		float x, float y, float z, float u, float v, float bright, int light, int overlay) {
		vc.addVertex(m, x, y, z)
			.setColor(bright, bright, bright, 1f)
			.setUv(u, v)
			.setOverlay(overlay)
			.setLight(light)
			.setNormal(pose, 0f, 1f, 0f);
	}
}
