package com.icees.visuals.cosmetics;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

/**
 * Realistic CS-style knives. Default silhouette is an M9 bayonet:
 * clip-point blade, spine serrations, tip hole, barrel-ring guard, grip.
 */
public final class KnifeModels {
	private KnifeModels() {}

	public static void draw(PoseStack m, VertexConsumer vc, int light, KnifeStyle style,
		float r, float g, float b, float time) {
		float er = Math.min(1f, r * 0.35f + 0.72f); // bright steel edge
		float eg = Math.min(1f, g * 0.35f + 0.74f);
		float eb = Math.min(1f, b * 0.35f + 0.78f);
		float sr = r * 0.45f, sg = g * 0.45f, sb = b * 0.5f;
		float hr = 0.1f, hg = 0.08f, hb = 0.07f;
		float mr = 0.28f, mg = 0.28f, mb = 0.3f;

		switch (style) {
			case KARAMBIT -> karambit(m, vc, light, r, g, b, er, eg, eb, sr, sg, sb, hr, hg, hb, mr, mg, mb);
			case BUTTERFLY -> butterfly(m, vc, light, r, g, b, er, eg, eb, sr, sg, sb, hr, hg, hb, mr, mg, mb, time);
			case STILETTO -> stiletto(m, vc, light, r, g, b, er, eg, eb, sr, sg, sb, hr, hg, hb, mr, mg, mb);
			case CURVED -> curved(m, vc, light, r, g, b, er, eg, eb, sr, sg, sb, hr, hg, hb, mr, mg, mb);
			case URSUS -> ursus(m, vc, light, r, g, b, er, eg, eb, sr, sg, sb, hr, hg, hb, mr, mg, mb);
			case SHORT_BLADE -> shortBlade(m, vc, light, r, g, b, er, eg, eb, sr, sg, sb, hr, hg, hb, mr, mg, mb);
			case LONG_BLADE -> m9Bayonet(m, vc, light, r, g, b, er, eg, eb, sr, sg, sb, hr, hg, hb, mr, mg, mb, 1.15f);
			case BAYONET -> m9Bayonet(m, vc, light, r, g, b, er, eg, eb, sr, sg, sb, hr, hg, hb, mr, mg, mb, 1.0f);
		}
	}

	/** M9 bayonet — matches the reference: flat wide blade, serrations, hole, ring guard. */
	private static void m9Bayonet(PoseStack m, VertexConsumer vc, int light,
		float r, float g, float b, float er, float eg, float eb, float sr, float sg, float sb,
		float hr, float hg, float hb, float mr, float mg, float mb, float lenScale) {
		float L = lenScale;

		// —— Grip (dark textured cylinder-ish) ——
		box(m, vc, light, -0.034f, -0.34f * L, -0.03f, 0.034f, -0.02f, 0.03f, hr, hg, hb, 1f);
		for (int i = 0; i < 5; i++) {
			float y0 = -0.32f * L + i * 0.055f * L;
			box(m, vc, light, -0.037f, y0, -0.032f, 0.037f, y0 + 0.02f * L, 0.032f, hr * 1.35f, hg * 1.25f, hb * 1.15f, 1f);
		}
		// pommel
		box(m, vc, light, -0.04f, -0.38f * L, -0.034f, 0.04f, -0.34f * L, 0.034f, mr, mg, mb, 1f);

		// —— Crossguard + muzzle ring ——
		box(m, vc, light, -0.11f, -0.035f, -0.035f, 0.11f, 0.04f, 0.035f, mr, mg, mb, 1f);
		// barrel ring on spine side
		for (int i = 0; i < 8; i++) {
			float a0 = i / 8f * Mth.TWO_PI;
			float a1 = (i + 1) / 8f * Mth.TWO_PI;
			float rr = 0.038f;
			float cx = 0.0f, cy = 0.055f;
			float x0 = cx + Mth.cos(a0) * rr, y0 = cy + Mth.sin(a0) * rr;
			float x1 = cx + Mth.cos(a1) * rr, y1 = cy + Mth.sin(a1) * rr;
			float t = 0.012f;
			box(m, vc, light,
				Math.min(x0, x1) - t, Math.min(y0, y1) - t, 0.02f,
				Math.max(x0, x1) + t, Math.max(y0, y1) + t, 0.045f,
				mr * 1.15f, mg * 1.15f, mb * 1.1f, 1f);
		}

		// —— Blade body (wide, flat) ——
		float y0 = 0.04f;
		float y1 = 0.58f * L;
		float halfW = 0.07f;
		float halfT = 0.012f;
		// main face
		box(m, vc, light, -halfW * 0.15f, y0, -halfT, halfW, y1, halfT, r, g, b, 1f);
		// spine (thicker top)
		box(m, vc, light, -halfW * 0.05f, y0, halfT * 0.4f, halfW * 0.55f, y1, halfT * 1.4f, sr, sg, sb, 1f);
		// cutting edge (bright silver)
		box(m, vc, light, halfW * 0.55f, y0 + 0.02f, -halfT * 0.7f, halfW * 1.05f, y1 - 0.04f * L, halfT * 0.7f, er, eg, eb, 1f);
		// ricasso
		box(m, vc, light, -halfW * 0.2f, y0, -halfT * 1.2f, halfW * 0.85f, y0 + 0.06f * L, halfT * 1.2f, r * 0.85f, g * 0.85f, b * 0.88f, 1f);

		// —— Spine serrations ——
		for (int i = 0; i < 7; i++) {
			float sy = 0.12f * L + i * 0.05f * L;
			box(m, vc, light, -0.02f, sy, halfT * 0.8f, 0.02f, sy + 0.028f * L, halfT * 1.8f, sr * 0.8f, sg * 0.8f, sb * 0.85f, 1f);
			box(m, vc, light, -0.028f, sy + 0.01f * L, halfT, -0.005f, sy + 0.022f * L, halfT * 1.6f, mr, mg, mb, 1f);
		}

		// —— Clip tip ——
		float tip0 = y1 - 0.02f * L;
		float tip1 = 0.72f * L;
		box(m, vc, light, -0.01f, tip0, -halfT * 0.8f, halfW * 0.7f, tip0 + (tip1 - tip0) * 0.45f, halfT * 0.8f, r, g, b, 1f);
		box(m, vc, light, 0.01f, tip0 + (tip1 - tip0) * 0.35f, -halfT * 0.6f, halfW * 0.45f, tip0 + (tip1 - tip0) * 0.75f, halfT * 0.6f, er, eg, eb, 1f);
		box(m, vc, light, 0.02f, tip0 + (tip1 - tip0) * 0.7f, -halfT * 0.4f, halfW * 0.22f, tip1, halfT * 0.4f, er, eg, eb, 1f);

		// —— Oval hole near tip (M9 signature) ——
		float hx = halfW * 0.25f, hy = 0.52f * L;
		box(m, vc, light, hx - 0.018f, hy - 0.028f, -halfT * 1.5f, hx + 0.018f, hy + 0.028f, halfT * 1.5f, 0.02f, 0.02f, 0.025f, 1f);
	}

	private static void shortBlade(PoseStack m, VertexConsumer vc, int light,
		float r, float g, float b, float er, float eg, float eb, float sr, float sg, float sb,
		float hr, float hg, float hb, float mr, float mg, float mb) {
		m9Bayonet(m, vc, light, r, g, b, er, eg, eb, sr, sg, sb, hr, hg, hb, mr, mg, mb, 0.72f);
	}

	private static void ursus(PoseStack m, VertexConsumer vc, int light,
		float r, float g, float b, float er, float eg, float eb, float sr, float sg, float sb,
		float hr, float hg, float hb, float mr, float mg, float mb) {
		m9Bayonet(m, vc, light, r, g, b, er, eg, eb, sr, sg, sb, hr, hg, hb, mr, mg, mb, 0.88f);
		// wider leaf near tip
		box(m, vc, light, -0.02f, 0.35f, -0.014f, 0.09f, 0.52f, 0.014f, r, g, b, 1f);
	}

	private static void stiletto(PoseStack m, VertexConsumer vc, int light,
		float r, float g, float b, float er, float eg, float eb, float sr, float sg, float sb,
		float hr, float hg, float hb, float mr, float mg, float mb) {
		box(m, vc, light, -0.025f, -0.3f, -0.025f, 0.025f, -0.02f, 0.025f, hr, hg, hb, 1f);
		box(m, vc, light, -0.05f, -0.03f, -0.025f, 0.05f, 0.035f, 0.025f, mr, mg, mb, 1f);
		// diamond needle
		box(m, vc, light, -0.018f, 0.03f, -0.01f, 0.018f, 0.7f, 0.01f, r, g, b, 1f);
		box(m, vc, light, -0.008f, 0.05f, -0.016f, 0.008f, 0.68f, 0.016f, er, eg, eb, 0.95f);
		box(m, vc, light, -0.01f, 0.7f, -0.008f, 0.01f, 0.8f, 0.008f, er, eg, eb, 1f);
	}

	private static void curved(PoseStack m, VertexConsumer vc, int light,
		float r, float g, float b, float er, float eg, float eb, float sr, float sg, float sb,
		float hr, float hg, float hb, float mr, float mg, float mb) {
		box(m, vc, light, -0.03f, -0.28f, -0.028f, 0.03f, -0.02f, 0.028f, hr, hg, hb, 1f);
		box(m, vc, light, -0.08f, -0.03f, -0.03f, 0.08f, 0.035f, 0.03f, mr, mg, mb, 1f);
		float[][] segs = {
			{0.00f, 0.12f, 0.00f, 0.06f},
			{0.08f, 0.24f, 0.035f, 0.058f},
			{0.16f, 0.36f, 0.08f, 0.05f},
			{0.24f, 0.46f, 0.13f, 0.04f},
			{0.32f, 0.54f, 0.18f, 0.028f}
		};
		for (float[] s : segs) {
			box(m, vc, light, s[2] - s[3] * 0.3f, s[0], -0.012f, s[2] + s[3], s[1], 0.012f, r, g, b, 1f);
			box(m, vc, light, s[2] + s[3] * 0.55f, s[0] + 0.01f, -0.008f, s[2] + s[3] * 1.15f, s[1] - 0.01f, 0.008f, er, eg, eb, 1f);
		}
	}

	private static void karambit(PoseStack m, VertexConsumer vc, int light,
		float r, float g, float b, float er, float eg, float eb, float sr, float sg, float sb,
		float hr, float hg, float hb, float mr, float mg, float mb) {
		box(m, vc, light, -0.03f, -0.16f, -0.028f, 0.045f, 0.05f, 0.028f, hr, hg, hb, 1f);
		for (int i = 0; i < 10; i++) {
			float a0 = i / 10f * Mth.TWO_PI;
			float a1 = (i + 1) / 10f * Mth.TWO_PI;
			float rr = 0.055f, cx = 0.08f, cy = 0.0f;
			float x0 = cx + Mth.cos(a0) * rr, y0 = cy + Mth.sin(a0) * rr;
			float x1 = cx + Mth.cos(a1) * rr, y1 = cy + Mth.sin(a1) * rr;
			float t = 0.014f;
			box(m, vc, light, Math.min(x0, x1) - t, Math.min(y0, y1) - t, -0.02f,
				Math.max(x0, x1) + t, Math.max(y0, y1) + t, 0.02f, mr, mg, mb, 1f);
		}
		float[][] claw = {
			{-0.02f, 0.08f, 0.05f, 0.16f},
			{-0.08f, 0.14f, 0.0f, 0.26f},
			{-0.15f, 0.24f, -0.06f, 0.34f},
			{-0.22f, 0.32f, -0.12f, 0.42f},
			{-0.27f, 0.4f, -0.18f, 0.48f}
		};
		for (float[] s : claw) {
			box(m, vc, light, s[0], s[1], -0.014f, s[2], s[3], 0.014f, r, g, b, 1f);
			box(m, vc, light, s[0] - 0.01f, s[1] + 0.01f, -0.008f, s[0] + 0.02f, s[3] - 0.01f, 0.008f, er, eg, eb, 1f);
		}
	}

	private static void butterfly(PoseStack m, VertexConsumer vc, int light,
		float r, float g, float b, float er, float eg, float eb, float sr, float sg, float sb,
		float hr, float hg, float hb, float mr, float mg, float mb, float time) {
		boolean open = Mth.sin(time * 0.32f) > -0.15f;
		box(m, vc, light, -0.04f, -0.22f, -0.028f, -0.008f, 0.1f, 0.028f, hr, hg, hb, 1f);
		box(m, vc, light, 0.008f, -0.22f, -0.028f, 0.04f, 0.1f, 0.028f, hr, hg, hb, 1f);
		box(m, vc, light, -0.02f, 0.02f, -0.02f, 0.02f, 0.06f, 0.02f, mr, mg, mb, 1f);
		if (open) {
			box(m, vc, light, -0.055f, 0.08f, -0.012f, 0.055f, 0.55f, 0.012f, r, g, b, 1f);
			box(m, vc, light, 0.03f, 0.1f, -0.008f, 0.065f, 0.52f, 0.008f, er, eg, eb, 1f);
			box(m, vc, light, -0.02f, 0.55f, -0.01f, 0.02f, 0.66f, 0.01f, er, eg, eb, 1f);
		} else {
			box(m, vc, light, -0.04f, 0.08f, -0.012f, 0.04f, 0.42f, 0.012f, r, g, b, 1f);
		}
	}

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
