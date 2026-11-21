package com.icees.visuals.gui;

import com.icees.visuals.config.IceesConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

/**
 * Cosmetics drawn on a higher GUI stratum so they sit ABOVE the deferred skin PIP.
 */
final class CosmeticPreviewOverlay {
	private static final Identifier TEX_WING = Identifier.fromNamespaceAndPath("icees-visuals", "textures/cosmetic/wing_soft.png");
	private static final Identifier TEX_HALO = Identifier.fromNamespaceAndPath("icees-visuals", "textures/cosmetic/halo.png");
	private static final Identifier TEX_AURA = Identifier.fromNamespaceAndPath("icees-visuals", "textures/cosmetic/aura.png");
	private static final Identifier TEX_CAPE = Identifier.fromNamespaceAndPath("icees-visuals", "textures/cosmetic/cape.png");

	private CosmeticPreviewOverlay() {}

	static void draw(GuiGraphics g, int px, int py, int pw, int ph, float time) {
		// Entity preview is a picture-in-picture submitted earlier — jump stratum so we paint on top
		g.nextStratum();

		int cx = px + pw / 2;
		int bodyY = py + 58;
		int headY = py + 28;
		float pulse = 0.9f + 0.1f * Mth.sin(time * 0.12f);

		if (IceesConfig.aura) {
			float[] c = IceesConfig.color(IceesConfig.auraTint, 0.9f, 0.55f, 0.75f);
			blit(g, TEX_AURA, cx - 50, bodyY - 4, 100, 100, ARGB.colorFromFloat(0.45f * pulse, c[0], c[1], c[2]));
		}
		if (IceesConfig.cape) {
			float[] c = IceesConfig.color(IceesConfig.capeTint, 1f, 0.55f, 0.72f);
			blit(g, TEX_CAPE, cx - 20, bodyY + 4, 40, 78, ARGB.colorFromFloat(0.85f, c[0], c[1], c[2]));
		}
		if (IceesConfig.wings) {
			float[] c = IceesConfig.color(IceesConfig.wingTint, 1f, 0.7f, 0.85f);
			int col = ARGB.colorFromFloat(0.9f * pulse, c[0], c[1], c[2]);
			blit(g, TEX_WING, cx - 64, bodyY - 12, 56, 78, col);
			blit(g, TEX_WING, cx + 8, bodyY - 12, 56, 78, col);
		}
		if (IceesConfig.halo) {
			float[] c = IceesConfig.color(IceesConfig.haloTint, 1f, 0.85f, 0.95f);
			blit(g, TEX_HALO, cx - 26, headY - 22, 52, 34, ARGB.colorFromFloat(0.95f, c[0], c[1], c[2]));
		}
		if (IceesConfig.hat) {
			float[] c = IceesConfig.color(IceesConfig.hatTint, 0.9f, 0.7f, 0.4f);
			int col = ARGB.colorFromFloat(0.95f, c[0], c[1], c[2]);
			g.fill(cx - 18, headY - 6, cx + 18, headY + 2, col);
			g.fill(cx - 12, headY - 20, cx + 12, headY - 6, col);
		}
		if (IceesConfig.shield) {
			float[] c = IceesConfig.color(IceesConfig.shieldTint, 1f, 0.55f, 0.72f);
			int col = ARGB.colorFromFloat(0.9f, c[0], c[1], c[2]);
			// Tight on the left arm — matches in-world attach
			g.fill(cx - 34, bodyY + 8, cx - 16, bodyY + 46, col);
			g.fill(cx - 32, bodyY + 12, cx - 18, bodyY + 42, ARGB.colorFromFloat(0.55f, c[0] * 0.7f, c[1] * 0.7f, c[2] * 0.75f));
		}
		if (IceesConfig.trail) {
			float[] c = IceesConfig.color(IceesConfig.auraTint, 0.55f, 0.85f, 1f);
			for (int i = 0; i < 5; i++) {
				blit(g, TEX_AURA, cx - 8 - i * 12, bodyY + 78 + (i % 2) * 3, 16, 16,
					ARGB.colorFromFloat(0.55f - i * 0.08f, c[0], c[1], c[2]));
			}
		}
		if (IceesConfig.jumpCircles) {
			float[] c = IceesConfig.color(IceesConfig.auraTint, 0.7f, 0.9f, 1f);
			blit(g, TEX_AURA, cx - 40, py + ph - 44, 80, 32, ARGB.colorFromFloat(0.55f, c[0], c[1], c[2]));
		}
	}

	private static void blit(GuiGraphics g, Identifier tex, int x, int y, int w, int h, int color) {
		g.blit(RenderPipelines.GUI_TEXTURED, tex, x, y, 0f, 0f, w, h, 64, 64, color);
	}
}
