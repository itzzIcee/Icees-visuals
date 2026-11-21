package com.icees.visuals.gui;

import com.icees.visuals.config.IceesConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

/**
 * Safe 2D cosmetic hints around the Orbit skin preview.
 * (No PIP / entity mixins — those crashed some setups with owo.)
 */
final class CosmeticPreviewOverlay {
	private static final Identifier TEX_WING = Identifier.fromNamespaceAndPath("icees-visuals", "textures/cosmetic/wing_soft.png");
	private static final Identifier TEX_HALO = Identifier.fromNamespaceAndPath("icees-visuals", "textures/cosmetic/halo.png");
	private static final Identifier TEX_AURA = Identifier.fromNamespaceAndPath("icees-visuals", "textures/cosmetic/aura.png");
	private static final Identifier TEX_CAPE = Identifier.fromNamespaceAndPath("icees-visuals", "textures/cosmetic/cape.png");

	private CosmeticPreviewOverlay() {}

	static void draw(GuiGraphics g, int px, int py, int pw, int ph, float time) {
		int cx = px + pw / 2;
		int bodyY = py + 70;
		int headY = py + 36;
		float pulse = 0.85f + 0.15f * Mth.sin(time * 0.08f);

		if (IceesConfig.aura) {
			float[] c = IceesConfig.color(IceesConfig.auraTint, 0.9f, 0.55f, 0.75f);
			int col = ARGB.colorFromFloat(0.28f * pulse, c[0], c[1], c[2]);
			blit(g, TEX_AURA, cx - 48, bodyY - 10, 96, 96, col);
		}
		if (IceesConfig.cape) {
			float[] c = IceesConfig.color(IceesConfig.capeTint, 1f, 0.55f, 0.72f);
			int col = ARGB.colorFromFloat(0.55f, c[0], c[1], c[2]);
			blit(g, TEX_CAPE, cx - 18, bodyY + 8, 36, 70, col);
		}
		if (IceesConfig.wings) {
			float[] c = IceesConfig.color(IceesConfig.wingTint, 1f, 0.7f, 0.85f);
			int col = ARGB.colorFromFloat(0.7f * pulse, c[0], c[1], c[2]);
			int wingW = 52;
			int wingH = 70;
			blit(g, TEX_WING, cx - wingW - 8, bodyY - 8, wingW, wingH, col);
			// flip via negative width not available — draw mirrored by second blit offset
			blit(g, TEX_WING, cx + 8, bodyY - 8, wingW, wingH, col);
		}
		if (IceesConfig.halo) {
			float[] c = IceesConfig.color(IceesConfig.haloTint, 1f, 0.85f, 0.95f);
			int col = ARGB.colorFromFloat(0.75f, c[0], c[1], c[2]);
			blit(g, TEX_HALO, cx - 22, headY - 18, 44, 28, col);
		}
		if (IceesConfig.hat) {
			int col = ARGB.colorFromFloat(0.9f, 0.85f, 0.65f, 0.35f);
			g.fill(cx - 16, headY - 8, cx + 16, headY - 2, col);
			g.fill(cx - 10, headY - 18, cx + 10, headY - 8, col);
		}
		if (IceesConfig.shield) {
			float[] c = IceesConfig.color(IceesConfig.shieldTint, 1f, 0.55f, 0.72f);
			int col = ARGB.colorFromFloat(0.65f, c[0], c[1], c[2]);
			g.fill(cx - 40, bodyY + 10, cx - 22, bodyY + 48, col);
		}
		if (IceesConfig.trail) {
			float[] c = IceesConfig.color(IceesConfig.auraTint, 0.55f, 0.85f, 1f);
			for (int i = 0; i < 4; i++) {
				float a = 0.15f + i * 0.08f;
				int col = ARGB.colorFromFloat(a, c[0], c[1], c[2]);
				int ox = cx - 6 - i * 10;
				int oy = bodyY + 70 + (i % 2) * 4;
				blit(g, TEX_AURA, ox, oy, 14, 14, col);
			}
		}
		if (IceesConfig.jumpCircles) {
			float[] c = IceesConfig.color(IceesConfig.auraTint, 0.7f, 0.9f, 1f);
			int col = ARGB.colorFromFloat(0.35f, c[0], c[1], c[2]);
			blit(g, TEX_AURA, cx - 36, py + ph - 48, 72, 28, col);
		}
	}

	private static void blit(GuiGraphics g, Identifier tex, int x, int y, int w, int h, int color) {
		g.blit(RenderPipelines.GUI_TEXTURED, tex, x, y, 0f, 0f, w, h, 64, 64, color);
	}
}
