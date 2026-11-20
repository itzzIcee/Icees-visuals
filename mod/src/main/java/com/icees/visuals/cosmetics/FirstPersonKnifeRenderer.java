package com.icees.visuals.cosmetics;

import com.icees.visuals.config.IceesConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

/**
 * 3D first-person knife — drawn in the held-item PoseStack so it inherits
 * vanilla arm swing / attack transforms.
 */
public final class FirstPersonKnifeRenderer {
	private static final Identifier TEX = Identifier.fromNamespaceAndPath("icees-visuals", "textures/cosmetic/knife.png");

	private FirstPersonKnifeRenderer() {}

	public static void render(PoseStack matrices, int light, float swingProgress) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null) return;

		float time = client.player.tickCount + client.getDeltaTracker().getGameTimeDeltaPartialTick(false);
		float[] rgb = IceesConfig.knifeBladeColor(time);
		float size = IceesConfig.knifeSizeFactor();
		float bright = IceesConfig.brightnessFactor();

		MultiBufferSource.BufferSource buffers = client.renderBuffers().bufferSource();
		VertexConsumer vc = buffers.getBuffer(RenderTypes.eyes(TEX));

		matrices.pushPose();
		// Sit in the grip like a sword tip pointing up-forward
		matrices.translate(0.0f, 0.06f, 0.02f);
		matrices.mulPose(Axis.XP.rotationDegrees(-10f - swingProgress * 8f));
		matrices.mulPose(Axis.ZP.rotationDegrees(5f));
		matrices.scale(size * 0.95f, size * 0.95f, size * 0.95f);

		float r = Math.min(1f, rgb[0] * bright);
		float g = Math.min(1f, rgb[1] * bright);
		float b = Math.min(1f, rgb[2] * bright);
		float edgeR = Math.min(1f, r + 0.2f);
		float edgeG = Math.min(1f, g + 0.2f);
		float edgeB = Math.min(1f, b + 0.15f);
		float handleR = 0.14f, handleG = 0.1f, handleB = 0.09f;

		switch (IceesConfig.knifeStyle) {
			case KARAMBIT -> drawKarambit(matrices, vc, light, r, g, b, edgeR, edgeG, edgeB, handleR, handleG, handleB);
			case BUTTERFLY -> drawButterfly(matrices, vc, light, r, g, b, edgeR, edgeG, edgeB, handleR, handleG, handleB, time);
			case STILETTO -> drawStiletto(matrices, vc, light, r, g, b, edgeR, edgeG, edgeB, handleR, handleG, handleB);
			case CURVED -> drawCurved(matrices, vc, light, r, g, b, edgeR, edgeG, edgeB, handleR, handleG, handleB);
			default -> drawJagged(matrices, vc, light, r, g, b, edgeR, edgeG, edgeB, handleR, handleG, handleB);
		}

		matrices.popPose();
		buffers.endLastBatch();
	}

	/** Crystal / dualprac-style serrated blade (matches reference look). */
	private static void drawJagged(PoseStack m, VertexConsumer vc, int light,
		float r, float g, float b, float er, float eg, float eb, float hr, float hg, float hb) {
		// handle
		box(m, vc, light, -0.03f, -0.28f, -0.03f, 0.03f, 0.02f, 0.03f, hr, hg, hb, 1f);
		box(m, vc, light, -0.045f, -0.02f, -0.04f, 0.045f, 0.04f, 0.04f, hr * 1.2f, hg * 1.2f, hb * 1.1f, 1f); // guard
		// diamond segments along the blade
		float[][] segs = {
			{0.00f, 0.06f, 0.07f},
			{0.07f, 0.14f, 0.08f},
			{0.14f, 0.22f, 0.075f},
			{0.22f, 0.30f, 0.07f},
			{0.30f, 0.38f, 0.06f},
			{0.38f, 0.46f, 0.05f},
			{0.46f, 0.54f, 0.035f}
		};
		for (int i = 0; i < segs.length; i++) {
			float y0 = segs[i][0], y1 = segs[i][1], w = segs[i][2];
			float shade = 1f - i * 0.04f;
			// diamond cross-section via two wedges
			box(m, vc, light, -w, y0, -0.012f, w, y1, 0.012f, r * shade, g * shade, b * shade, 1f);
			box(m, vc, light, -w * 0.55f, y0 + 0.01f, -0.02f, w * 0.55f, y1 - 0.01f, 0.02f, er * shade, eg * shade, eb * shade, 0.95f);
		}
		// tip
		box(m, vc, light, -0.02f, 0.54f, -0.01f, 0.02f, 0.62f, 0.01f, er, eg, eb, 1f);
	}

	private static void drawStiletto(PoseStack m, VertexConsumer vc, int light,
		float r, float g, float b, float er, float eg, float eb, float hr, float hg, float hb) {
		box(m, vc, light, -0.025f, -0.26f, -0.025f, 0.025f, 0.02f, 0.025f, hr, hg, hb, 1f);
		box(m, vc, light, -0.05f, -0.02f, -0.02f, 0.05f, 0.03f, 0.02f, hr * 1.3f, hg * 1.3f, hb, 1f);
		box(m, vc, light, -0.018f, 0.02f, -0.012f, 0.018f, 0.72f, 0.012f, r, g, b, 1f);
		box(m, vc, light, -0.008f, 0.05f, -0.018f, 0.008f, 0.68f, 0.018f, er, eg, eb, 0.9f);
		box(m, vc, light, -0.01f, 0.72f, -0.008f, 0.01f, 0.80f, 0.008f, er, eg, eb, 1f);
	}

	private static void drawButterfly(PoseStack m, VertexConsumer vc, int light,
		float r, float g, float b, float er, float eg, float eb, float hr, float hg, float hb, float time) {
		boolean open = Mth.sin(time * 0.35f) > -0.2f;
		box(m, vc, light, -0.035f, -0.22f, -0.03f, 0.035f, 0.08f, 0.03f, hr, hg, hb, 1f);
		if (open) {
			box(m, vc, light, -0.09f, 0.05f, -0.012f, -0.02f, 0.55f, 0.012f, r, g, b, 1f);
			box(m, vc, light, 0.02f, 0.05f, -0.012f, 0.09f, 0.55f, 0.012f, r, g, b, 1f);
			box(m, vc, light, -0.07f, 0.55f, -0.01f, -0.04f, 0.62f, 0.01f, er, eg, eb, 1f);
			box(m, vc, light, 0.04f, 0.55f, -0.01f, 0.07f, 0.62f, 0.01f, er, eg, eb, 1f);
		} else {
			box(m, vc, light, -0.04f, 0.05f, -0.012f, 0.04f, 0.48f, 0.012f, r, g, b, 1f);
		}
	}

	private static void drawKarambit(PoseStack m, VertexConsumer vc, int light,
		float r, float g, float b, float er, float eg, float eb, float hr, float hg, float hb) {
		box(m, vc, light, -0.03f, -0.18f, -0.03f, 0.04f, 0.06f, 0.03f, hr, hg, hb, 1f);
		box(m, vc, light, 0.02f, -0.05f, -0.035f, 0.09f, 0.08f, 0.035f, hr, hg, hb, 1f); // ring
		float[][] segs = {
			{-0.02f, 0.08f, 0.06f, 0.16f},
			{-0.08f, 0.14f, 0.0f, 0.26f},
			{-0.16f, 0.22f, -0.06f, 0.34f},
			{-0.22f, 0.30f, -0.12f, 0.40f},
			{-0.26f, 0.36f, -0.18f, 0.46f}
		};
		for (float[] s : segs) {
			box(m, vc, light, s[0], s[1], -0.015f, s[2], s[3], 0.015f, r, g, b, 1f);
		}
		box(m, vc, light, -0.28f, 0.42f, -0.01f, -0.2f, 0.50f, 0.01f, er, eg, eb, 1f);
	}

	private static void drawCurved(PoseStack m, VertexConsumer vc, int light,
		float r, float g, float b, float er, float eg, float eb, float hr, float hg, float hb) {
		box(m, vc, light, -0.03f, -0.24f, -0.03f, 0.03f, 0.02f, 0.03f, hr, hg, hb, 1f);
		float[][] segs = {
			{-0.02f, 0.02f, 0.06f, 0.14f},
			{0.02f, 0.12f, 0.12f, 0.26f},
			{0.08f, 0.24f, 0.18f, 0.38f},
			{0.14f, 0.36f, 0.22f, 0.48f}
		};
		for (float[] s : segs) {
			box(m, vc, light, s[0], s[1], -0.015f, s[2], s[3], 0.015f, r, g, b, 1f);
		}
		box(m, vc, light, 0.18f, 0.48f, -0.01f, 0.24f, 0.55f, 0.01f, er, eg, eb, 1f);
	}

	private static void box(PoseStack matrices, VertexConsumer vc, int light,
		float x0, float y0, float z0, float x1, float y1, float z1,
		float r, float g, float b, float a) {
		PoseStack.Pose pose = matrices.last();
		Matrix4f m = pose.pose();
		int overlay = OverlayTexture.NO_OVERLAY;
		// 6 faces
		quad(m, pose, vc, light, overlay, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, r, g, b, a); // +Z
		quad(m, pose, vc, light, overlay, x1, y0, z0, x0, y0, z0, x0, y1, z0, x1, y1, z0, r, g, b, a); // -Z
		quad(m, pose, vc, light, overlay, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0, r, g, b, a); // +Y
		quad(m, pose, vc, light, overlay, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1, r, g, b, a); // -Y
		quad(m, pose, vc, light, overlay, x1, y0, z1, x1, y0, z0, x1, y1, z0, x1, y1, z1, r, g, b, a); // +X
		quad(m, pose, vc, light, overlay, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0, r, g, b, a); // -X
	}

	private static void quad(Matrix4f m, PoseStack.Pose pose, VertexConsumer vc, int light, int overlay,
		float x0, float y0, float z0, float x1, float y1, float z1,
		float x2, float y2, float z2, float x3, float y3, float z3,
		float r, float g, float b, float a) {
		vc.addVertex(m, x0, y0, z0).setColor(r, g, b, a).setUv(0f, 1f).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
		vc.addVertex(m, x1, y1, z1).setColor(r, g, b, a).setUv(1f, 1f).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
		vc.addVertex(m, x2, y2, z2).setColor(r, g, b, a).setUv(1f, 0f).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
		vc.addVertex(m, x3, y3, z3).setColor(r, g, b, a).setUv(0f, 0f).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
	}
}
