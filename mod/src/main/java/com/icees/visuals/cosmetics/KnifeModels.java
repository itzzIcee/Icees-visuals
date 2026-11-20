package com.icees.visuals.cosmetics;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

/**
 * Realistic CS-style 3D knife meshes — beveled blade, fuller, guard, wrapped grip, pommel.
 * Used by first-person and third-person renderers.
 */
public final class KnifeModels {
	private KnifeModels() {}

	public static void draw(PoseStack m, VertexConsumer vc, int light, KnifeStyle style,
		float r, float g, float b, float time) {
		float er = Math.min(1f, r + 0.22f);
		float eg = Math.min(1f, g + 0.22f);
		float eb = Math.min(1f, b + 0.18f);
		float shadeR = r * 0.55f, shadeG = g * 0.55f, shadeB = b * 0.6f;
		float hr = 0.12f, hg = 0.09f, hb = 0.08f;
		float wrap = 0.22f, wrapG = 0.16f, wrapB = 0.12f;
		float metal = 0.55f, metalG = 0.55f, metalB = 0.58f;

		switch (style) {
			case KARAMBIT -> karambit(m, vc, light, r, g, b, er, eg, eb, shadeR, shadeG, shadeB, hr, hg, hb, metal, metalG, metalB);
			case BUTTERFLY -> butterfly(m, vc, light, r, g, b, er, eg, eb, shadeR, shadeG, shadeB, hr, hg, hb, metal, metalG, metalB, time);
			case BAYONET -> bayonet(m, vc, light, r, g, b, er, eg, eb, shadeR, shadeG, shadeB, hr, hg, hb, wrap, wrapG, wrapB, metal, metalG, metalB);
			case STILETTO -> stiletto(m, vc, light, r, g, b, er, eg, eb, shadeR, shadeG, shadeB, hr, hg, hb, metal, metalG, metalB);
			case URSUS -> ursus(m, vc, light, r, g, b, er, eg, eb, shadeR, shadeG, shadeB, hr, hg, hb, wrap, wrapG, wrapB, metal, metalG, metalB);
			case LONG_BLADE -> longBlade(m, vc, light, r, g, b, er, eg, eb, shadeR, shadeG, shadeB, hr, hg, hb, wrap, wrapG, wrapB, metal, metalG, metalB);
			case SHORT_BLADE -> shortBlade(m, vc, light, r, g, b, er, eg, eb, shadeR, shadeG, shadeB, hr, hg, hb, wrap, wrapG, wrapB, metal, metalG, metalB);
			case CURVED -> curved(m, vc, light, r, g, b, er, eg, eb, shadeR, shadeG, shadeB, hr, hg, hb, wrap, wrapG, wrapB, metal, metalG, metalB);
		}
	}

	/* ===================== STYLES ===================== */

	private static void bayonet(PoseStack m, VertexConsumer vc, int light,
		float r, float g, float b, float er, float eg, float eb, float sr, float sg, float sb,
		float hr, float hg, float hb, float wr, float wg, float wb, float mr, float mg, float mb) {
		grip(m, vc, light, -0.032f, -0.30f, -0.028f, 0.032f, -0.02f, 0.028f, hr, hg, hb, wr, wg, wb);
		pommel(m, vc, light, 0.04f, -0.34f, mr, mg, mb);
		guard(m, vc, light, 0.09f, 0.045f, 0.035f, mr, mg, mb);
		// long tapered clip-point blade with bevel + fuller
		beveledBlade(m, vc, light, 0.02f, 0.68f, 0.055f, 0.018f, 0.014f, r, g, b, er, eg, eb, sr, sg, sb, true);
		tip(m, vc, light, 0.68f, 0.78f, 0.022f, er, eg, eb, sr, sg, sb);
	}

	private static void longBlade(PoseStack m, VertexConsumer vc, int light,
		float r, float g, float b, float er, float eg, float eb, float sr, float sg, float sb,
		float hr, float hg, float hb, float wr, float wg, float wb, float mr, float mg, float mb) {
		grip(m, vc, light, -0.034f, -0.32f, -0.03f, 0.034f, -0.02f, 0.03f, hr, hg, hb, wr, wg, wb);
		pommel(m, vc, light, 0.042f, -0.36f, mr, mg, mb);
		guard(m, vc, light, 0.1f, 0.05f, 0.038f, mr, mg, mb);
		beveledBlade(m, vc, light, 0.02f, 0.82f, 0.06f, 0.02f, 0.015f, r, g, b, er, eg, eb, sr, sg, sb, true);
		tip(m, vc, light, 0.82f, 0.94f, 0.024f, er, eg, eb, sr, sg, sb);
	}

	private static void shortBlade(PoseStack m, VertexConsumer vc, int light,
		float r, float g, float b, float er, float eg, float eb, float sr, float sg, float sb,
		float hr, float hg, float hb, float wr, float wg, float wb, float mr, float mg, float mb) {
		grip(m, vc, light, -0.03f, -0.24f, -0.026f, 0.03f, -0.015f, 0.026f, hr, hg, hb, wr, wg, wb);
		pommel(m, vc, light, 0.036f, -0.28f, mr, mg, mb);
		guard(m, vc, light, 0.075f, 0.04f, 0.032f, mr, mg, mb);
		beveledBlade(m, vc, light, 0.015f, 0.42f, 0.05f, 0.016f, 0.013f, r, g, b, er, eg, eb, sr, sg, sb, false);
		tip(m, vc, light, 0.42f, 0.50f, 0.02f, er, eg, eb, sr, sg, sb);
	}

	private static void stiletto(PoseStack m, VertexConsumer vc, int light,
		float r, float g, float b, float er, float eg, float eb, float sr, float sg, float sb,
		float hr, float hg, float hb, float mr, float mg, float mb) {
		// slim roundish grip
		box(m, vc, light, -0.022f, -0.28f, -0.022f, 0.022f, -0.02f, 0.022f, hr, hg, hb, 1f);
		box(m, vc, light, -0.018f, -0.26f, -0.018f, 0.018f, -0.04f, 0.018f, hr * 1.35f, hg * 1.3f, hb * 1.2f, 1f);
		pommel(m, vc, light, 0.032f, -0.32f, mr, mg, mb);
		guard(m, vc, light, 0.07f, 0.032f, 0.028f, mr, mg, mb);
		// needle blade — diamond cross section, very thin
		beveledBlade(m, vc, light, 0.015f, 0.78f, 0.022f, 0.011f, 0.011f, r, g, b, er, eg, eb, sr, sg, sb, true);
		tip(m, vc, light, 0.78f, 0.88f, 0.012f, er, eg, eb, sr, sg, sb);
	}

	private static void ursus(PoseStack m, VertexConsumer vc, int light,
		float r, float g, float b, float er, float eg, float eb, float sr, float sg, float sb,
		float hr, float hg, float hb, float wr, float wg, float wb, float mr, float mg, float mb) {
		grip(m, vc, light, -0.033f, -0.28f, -0.028f, 0.033f, -0.02f, 0.028f, hr, hg, hb, wr, wg, wb);
		pommel(m, vc, light, 0.04f, -0.32f, mr, mg, mb);
		guard(m, vc, light, 0.085f, 0.042f, 0.034f, mr, mg, mb);
		// wide leaf blade
		beveledBlade(m, vc, light, 0.02f, 0.55f, 0.07f, 0.018f, 0.014f, r, g, b, er, eg, eb, sr, sg, sb, true);
		tip(m, vc, light, 0.55f, 0.66f, 0.028f, er, eg, eb, sr, sg, sb);
	}

	private static void curved(PoseStack m, VertexConsumer vc, int light,
		float r, float g, float b, float er, float eg, float eb, float sr, float sg, float sb,
		float hr, float hg, float hb, float wr, float wg, float wb, float mr, float mg, float mb) {
		grip(m, vc, light, -0.03f, -0.26f, -0.026f, 0.03f, -0.015f, 0.026f, hr, hg, hb, wr, wg, wb);
		pommel(m, vc, light, 0.036f, -0.30f, mr, mg, mb);
		guard(m, vc, light, 0.08f, 0.04f, 0.032f, mr, mg, mb);
		// curved segments sweeping forward
		float[][] segs = {
			{0.00f, 0.12f, 0.00f, 0.055f},
			{0.10f, 0.24f, 0.04f, 0.052f},
			{0.20f, 0.36f, 0.09f, 0.048f},
			{0.30f, 0.46f, 0.14f, 0.04f},
			{0.40f, 0.54f, 0.19f, 0.03f}
		};
		for (float[] s : segs) {
			m.pushPose();
			m.translate(s[2], 0, 0);
			beveledBlade(m, vc, light, s[0], s[1], s[3], 0.015f, 0.012f, r, g, b, er, eg, eb, sr, sg, sb, false);
			m.popPose();
		}
		m.pushPose();
		m.translate(0.22f, 0, 0);
		tip(m, vc, light, 0.54f, 0.62f, 0.02f, er, eg, eb, sr, sg, sb);
		m.popPose();
	}

	private static void karambit(PoseStack m, VertexConsumer vc, int light,
		float r, float g, float b, float er, float eg, float eb, float sr, float sg, float sb,
		float hr, float hg, float hb, float mr, float mg, float mb) {
		// grip + finger ring
		box(m, vc, light, -0.028f, -0.16f, -0.026f, 0.04f, 0.05f, 0.026f, hr, hg, hb, 1f);
		// ring torus approx
		for (int i = 0; i < 10; i++) {
			float a0 = i / 10f * Mth.TWO_PI;
			float a1 = (i + 1) / 10f * Mth.TWO_PI;
			float rr = 0.055f;
			float cx = 0.07f, cy = 0.0f;
			float x0 = cx + Mth.cos(a0) * rr, y0 = cy + Mth.sin(a0) * rr;
			float x1 = cx + Mth.cos(a1) * rr, y1 = cy + Mth.sin(a1) * rr;
			float t = 0.014f;
			box(m, vc, light, Math.min(x0, x1) - t, Math.min(y0, y1) - t, -0.02f,
				Math.max(x0, x1) + t, Math.max(y0, y1) + t, 0.02f, mr, mg, mb, 1f);
		}
		box(m, vc, light, 0.04f, -0.02f, -0.018f, 0.1f, 0.04f, 0.018f, hr * 1.2f, hg * 1.15f, hb, 1f);
		// reverse curved claw blade
		float[][] claw = {
			{-0.02f, 0.08f, 0.04f, 0.05f},
			{-0.08f, 0.16f, -0.02f, 0.048f},
			{-0.15f, 0.26f, -0.08f, 0.042f},
			{-0.22f, 0.34f, -0.14f, 0.034f},
			{-0.27f, 0.42f, -0.20f, 0.024f}
		};
		for (float[] s : claw) {
			m.pushPose();
			m.translate((s[0] + s[2]) * 0.5f, 0, 0);
			float half = Math.abs(s[2] - s[0]) * 0.5f + s[3];
			beveledBlade(m, vc, light, s[1] - 0.02f, s[1] + (s[3] + 0.06f), half * 0.9f, 0.014f, 0.011f, r, g, b, er, eg, eb, sr, sg, sb, false);
			m.popPose();
		}
		box(m, vc, light, -0.30f, 0.42f, -0.01f, -0.22f, 0.50f, 0.01f, er, eg, eb, 1f);
	}

	private static void butterfly(PoseStack m, VertexConsumer vc, int light,
		float r, float g, float b, float er, float eg, float eb, float sr, float sg, float sb,
		float hr, float hg, float hb, float mr, float mg, float mb, float time) {
		boolean open = Mth.sin(time * 0.32f) > -0.15f;
		// handles
		box(m, vc, light, -0.038f, -0.22f, -0.028f, -0.008f, 0.1f, 0.028f, hr, hg, hb, 1f);
		box(m, vc, light, 0.008f, -0.22f, -0.028f, 0.038f, 0.1f, 0.028f, hr, hg, hb, 1f);
		box(m, vc, light, -0.02f, 0.02f, -0.02f, 0.02f, 0.06f, 0.02f, mr, mg, mb, 1f); // pivot
		if (open) {
			beveledBlade(m, vc, light, 0.08f, 0.58f, 0.048f, 0.015f, 0.012f, r, g, b, er, eg, eb, sr, sg, sb, true);
			tip(m, vc, light, 0.58f, 0.68f, 0.02f, er, eg, eb, sr, sg, sb);
			// open handles angled slightly
			m.pushPose();
			m.translate(-0.05f, -0.05f, 0);
			m.mulPose(Axis.ZP.rotationDegrees(-18f));
			box(m, vc, light, -0.03f, -0.2f, -0.025f, 0.0f, 0.08f, 0.025f, hr * 1.1f, hg * 1.05f, hb, 1f);
			m.popPose();
			m.pushPose();
			m.translate(0.05f, -0.05f, 0);
			m.mulPose(Axis.ZP.rotationDegrees(18f));
			box(m, vc, light, 0.0f, -0.2f, -0.025f, 0.03f, 0.08f, 0.025f, hr * 1.1f, hg * 1.05f, hb, 1f);
			m.popPose();
		} else {
			beveledBlade(m, vc, light, 0.08f, 0.45f, 0.04f, 0.014f, 0.011f, r, g, b, er, eg, eb, sr, sg, sb, false);
		}
	}

	/* ===================== PARTS ===================== */

	private static void grip(PoseStack m, VertexConsumer vc, int light,
		float x0, float y0, float z0, float x1, float y1, float z1,
		float hr, float hg, float hb, float wr, float wg, float wb) {
		box(m, vc, light, x0, y0, z0, x1, y1, z1, hr, hg, hb, 1f);
		// wrap rings
		float h = y1 - y0;
		for (int i = 0; i < 4; i++) {
			float t0 = y0 + h * (0.12f + i * 0.2f);
			float t1 = t0 + h * 0.08f;
			box(m, vc, light, x0 - 0.004f, t0, z0 - 0.004f, x1 + 0.004f, t1, z1 + 0.004f, wr, wg, wb, 1f);
		}
		// highlight edge
		box(m, vc, light, x0 + 0.004f, y0 + 0.02f, z1 - 0.002f, x0 + 0.012f, y1 - 0.02f, z1 + 0.004f,
			1f, 1f, 1f, 0.18f);
	}

	private static void pommel(PoseStack m, VertexConsumer vc, int light, float size, float y, float mr, float mg, float mb) {
		box(m, vc, light, -size * 0.55f, y, -size * 0.45f, size * 0.55f, y + size * 0.7f, size * 0.45f, mr, mg, mb, 1f);
		box(m, vc, light, -size * 0.35f, y - size * 0.25f, -size * 0.3f, size * 0.35f, y, size * 0.3f, mr * 0.85f, mg * 0.85f, mb * 0.9f, 1f);
	}

	private static void guard(PoseStack m, VertexConsumer vc, int light, float halfW, float halfH, float halfD, float mr, float mg, float mb) {
		box(m, vc, light, -halfW, -halfH * 0.35f, -halfD, halfW, halfH * 0.65f, halfD, mr, mg, mb, 1f);
		// quillons flare
		box(m, vc, light, -halfW - 0.01f, -halfH * 0.15f, -halfD * 0.7f, -halfW + 0.02f, halfH * 0.5f, halfD * 0.7f, mr * 1.1f, mg * 1.1f, mb * 1.05f, 1f);
		box(m, vc, light, halfW - 0.02f, -halfH * 0.15f, -halfD * 0.7f, halfW + 0.01f, halfH * 0.5f, halfD * 0.7f, mr * 1.1f, mg * 1.1f, mb * 1.05f, 1f);
	}

	/**
	 * Beveled blade: core + two edge bevels + optional fuller groove.
	 * Width tapers slightly toward tip via tip() call separately.
	 */
	private static void beveledBlade(PoseStack m, VertexConsumer vc, int light,
		float y0, float y1, float halfW, float halfT, float edgeT,
		float r, float g, float b, float er, float eg, float eb, float sr, float sg, float sb,
		boolean fuller) {
		float mid = (y0 + y1) * 0.5f;
		float len = y1 - y0;
		// main flat
		box(m, vc, light, -halfW * 0.55f, y0, -halfT, halfW * 0.55f, y1, halfT, r, g, b, 1f);
		// edge bevels (catch light)
		box(m, vc, light, halfW * 0.4f, y0 + 0.01f, -edgeT, halfW, y1 - 0.01f, edgeT, er, eg, eb, 1f);
		box(m, vc, light, -halfW, y0 + 0.01f, -edgeT, -halfW * 0.4f, y1 - 0.01f, edgeT, sr, sg, sb, 1f);
		// spine ridge
		box(m, vc, light, -halfW * 0.2f, y0, halfT * 0.6f, halfW * 0.2f, y1, halfT * 1.35f, er * 0.95f, eg * 0.95f, eb * 0.95f, 0.95f);
		if (fuller) {
			box(m, vc, light, -halfW * 0.18f, mid - len * 0.28f, halfT * 0.9f, halfW * 0.18f, mid + len * 0.28f, halfT * 1.5f,
				sr * 0.9f, sg * 0.9f, sb * 0.95f, 0.85f);
		}
		// ricasso (thick near guard)
		box(m, vc, light, -halfW * 0.65f, y0, -halfT * 1.1f, halfW * 0.65f, y0 + len * 0.08f, halfT * 1.1f, r * 0.9f, g * 0.9f, b * 0.92f, 1f);
	}

	private static void tip(PoseStack m, VertexConsumer vc, int light,
		float y0, float y1, float halfW, float er, float eg, float eb, float sr, float sg, float sb) {
		box(m, vc, light, -halfW, y0, -0.01f, halfW, y0 + (y1 - y0) * 0.45f, 0.01f, er, eg, eb, 1f);
		box(m, vc, light, -halfW * 0.55f, y0 + (y1 - y0) * 0.4f, -0.008f, halfW * 0.55f, y0 + (y1 - y0) * 0.75f, 0.008f, er, eg, eb, 1f);
		box(m, vc, light, -halfW * 0.22f, y0 + (y1 - y0) * 0.7f, -0.006f, halfW * 0.22f, y1, 0.006f, er, eg, eb, 1f);
		box(m, vc, light, -halfW * 0.9f, y0, -0.006f, -halfW * 0.3f, y0 + (y1 - y0) * 0.5f, 0.006f, sr, sg, sb, 1f);
	}

	/* ===================== PRIMITIVES ===================== */

	static void box(PoseStack matrices, VertexConsumer vc, int light,
		float x0, float y0, float z0, float x1, float y1, float z1,
		float r, float g, float b, float a) {
		PoseStack.Pose pose = matrices.last();
		Matrix4f m = pose.pose();
		int overlay = OverlayTexture.NO_OVERLAY;
		float aa = Mth.clamp(a, 0f, 1f);
		quad(m, pose, vc, light, overlay, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, r, g, b, aa);
		quad(m, pose, vc, light, overlay, x1, y0, z0, x0, y0, z0, x0, y1, z0, x1, y1, z0, r, g, b, aa);
		quad(m, pose, vc, light, overlay, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0, r, g, b, aa);
		quad(m, pose, vc, light, overlay, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1, r, g, b, aa);
		quad(m, pose, vc, light, overlay, x1, y0, z1, x1, y0, z0, x1, y1, z0, x1, y1, z1, r, g, b, aa);
		quad(m, pose, vc, light, overlay, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0, r, g, b, aa);
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
