package com.icees.visuals.effects;

import com.icees.visuals.config.IceesConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

/**
 * Upper-hemisphere sky dome in world space (around the camera, depth-tested).
 * Terrain occludes it — looks like sky in the air, not a screen overlay.
 * Animated: slow spin. Static: same dome, fixed orientation.
 */
public final class CustomSkyRenderer {
	private static final int SEG_H = 64;
	private static final int SEG_V = 24;
	/** Far enough that normal terrain stays in front of the dome. */
	private static final float RADIUS = 512f;
	/** Dip slightly below horizon so the seam doesn’t flash. */
	private static final float HORIZON_DIP = 0.08f;

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

		float partial = client.getDeltaTracker().getGameTimeDeltaPartialTick(false);
		float time = client.level.getGameTime() + partial;
		float bright = 0.55f + IceesConfig.clamp10(IceesConfig.skyBrightness) * 0.05f;
		float speed = 0.15f + IceesConfig.clamp10(IceesConfig.skySpeed) * 0.12f;
		boolean animated = IceesConfig.skyAnimated;

		// Same solid fullbright layer for both modes — static was invisible with eyes()/energySwirl.
		VertexConsumer vc = consumers.getBuffer(RenderTypes.entityTranslucentEmissive(tex));

		matrices.pushPose();
		// Pose stack is already camera-relative with world axes — do NOT counter-rotate
		// (that stuck the texture to the screen). Only spin when Animated is on.
		if (animated) {
			matrices.mulPose(Axis.YP.rotationDegrees(time * 0.4f * speed));
		}

		drawDome(matrices, vc, bright);
		matrices.popPose();
	}

	/**
	 * Inward-facing upper dome only (horizon → zenith).
	 * Looking down shows the world; looking up / at the horizon shows the sky.
	 */
	private static void drawDome(PoseStack matrices, VertexConsumer vc, float bright) {
		PoseStack.Pose pose = matrices.last();
		Matrix4f m = pose.pose();
		int light = LightTexture.FULL_BRIGHT;
		int overlay = OverlayTexture.NO_OVERLAY;

		for (int iv = 0; iv < SEG_V; iv++) {
			float v0 = iv / (float) SEG_V;
			float v1 = (iv + 1) / (float) SEG_V;
			// pitch: -dip at horizon → +π/2 at zenith
			float pitch0 = -HORIZON_DIP + v0 * ((float) Math.PI * 0.5f + HORIZON_DIP);
			float pitch1 = -HORIZON_DIP + v1 * ((float) Math.PI * 0.5f + HORIZON_DIP);
			float y0 = Mth.sin(pitch0) * RADIUS;
			float y1 = Mth.sin(pitch1) * RADIUS;
			float r0 = Mth.cos(pitch0) * RADIUS;
			float r1 = Mth.cos(pitch1) * RADIUS;

			// Map dome V to upper half of panorama (sky content)
			float tv0 = 0.5f - v0 * 0.5f;
			float tv1 = 0.5f - v1 * 0.5f;

			for (int ih = 0; ih < SEG_H; ih++) {
				float u0 = ih / (float) SEG_H;
				float u1 = (ih + 1) / (float) SEG_H;
				float yaw0 = u0 * ((float) Math.PI * 2f);
				float yaw1 = u1 * ((float) Math.PI * 2f);

				float x00 = Mth.cos(yaw0) * r0, z00 = Mth.sin(yaw0) * r0;
				float x10 = Mth.cos(yaw1) * r0, z10 = Mth.sin(yaw1) * r0;
				float x01 = Mth.cos(yaw0) * r1, z01 = Mth.sin(yaw0) * r1;
				float x11 = Mth.cos(yaw1) * r1, z11 = Mth.sin(yaw1) * r1;

				// Inward winding
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
			.setNormal(pose, 0f, -1f, 0f);
	}
}
