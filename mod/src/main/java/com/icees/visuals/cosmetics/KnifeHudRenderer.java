package com.icees.visuals.cosmetics;

import com.icees.visuals.config.IceesConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.joml.Matrix3x2fStack;

/**
 * First-person knife viewmodel — sits in the hand when a sword is equipped.
 * Distinct silhouettes per style, size / HSV / material (chrome, rainbow, …).
 */
public final class KnifeHudRenderer {
	private KnifeHudRenderer() {}

	public static void render(GuiGraphics graphics) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || client.level == null) return;
		if (!IceesConfig.knifeActive(client.player)) return;
		if (client.screen != null) return;
		if (!client.options.getCameraType().isFirstPerson()) return;
		if (client.options.hideGui) return;

		float time = client.player.tickCount + client.getDeltaTracker().getGameTimeDeltaPartialTick(false);
		int sw = client.getWindow().getGuiScaledWidth();
		int sh = client.getWindow().getGuiScaledHeight();
		float size = IceesConfig.knifeSizeFactor();

		float[] rgb = IceesConfig.knifeBladeColor(time);
		Palette pal = palette(rgb, time);

		float bob = Mth.sin(time * 0.12f) * 2.5f;
		float tip = Mth.sin(time * 0.18f) * 3f;
		float inspect = switch (IceesConfig.knifeStyle) {
			case BUTTERFLY, KARAMBIT -> Mth.sin(time * 0.28f) * 12f;
			case BAYONET, LONG_BLADE -> Mth.sin(time * 0.1f) * 6f;
			case STILETTO, SHORT_BLADE -> Mth.sin(time * 0.22f) * 9f;
			case URSUS, CURVED -> Mth.sin(time * 0.16f) * 10f;
		};

		// Sit in the right hand — lower-right, overlapping the FP arm
		float scale = 0.95f + size * 0.45f;
		float cx = sw * 0.70f + tip;
		float cy = sh * 0.78f + bob;

		Matrix3x2fStack pose = graphics.pose();
		pose.pushMatrix();
		pose.translate(cx, cy);
		pose.rotate((float) Math.toRadians(-38f + inspect));
		pose.scale(scale, scale);

		switch (IceesConfig.knifeStyle) {
			case KARAMBIT -> drawKarambit(graphics, pal, time);
			case BUTTERFLY -> drawButterfly(graphics, pal, time);
			case BAYONET -> drawBayonet(graphics, pal);
			case STILETTO -> drawStiletto(graphics, pal);
			case URSUS -> drawUrsus(graphics, pal);
			case LONG_BLADE -> drawLongBlade(graphics, pal);
			case SHORT_BLADE -> drawShortBlade(graphics, pal);
			case CURVED -> drawCurved(graphics, pal);
		}

		pose.popMatrix();
	}

	private record Palette(int blade, int edge, int shine, int shade, int guard, int grip, int ring) {}

	private static Palette palette(float[] rgb, float time) {
		KnifeMaterial mat = IceesConfig.knifeMaterial;
		float r = rgb[0], g = rgb[1], b = rgb[2];

		int blade = ARGB.colorFromFloat(1f, r, g, b);
		int edge = ARGB.colorFromFloat(1f,
			Math.max(0f, r * 0.55f), Math.max(0f, g * 0.55f), Math.max(0f, b * 0.58f));
		int shine = ARGB.colorFromFloat(0.95f,
			Math.min(1f, r + 0.35f), Math.min(1f, g + 0.35f), Math.min(1f, b + 0.38f));
		int shade = ARGB.colorFromFloat(1f,
			r * 0.35f, g * 0.35f, b * 0.4f);

		int grip = switch (mat) {
			case GOLD -> ARGB.colorFromFloat(1f, 0.28f, 0.16f, 0.08f);
			case SAKURA -> ARGB.colorFromFloat(1f, 0.32f, 0.14f, 0.22f);
			case OBSIDIAN -> ARGB.colorFromFloat(1f, 0.08f, 0.06f, 0.1f);
			default -> ARGB.colorFromFloat(1f, 0.14f, 0.11f, 0.1f);
		};
		int guard = switch (mat) {
			case GOLD -> ARGB.colorFromFloat(1f, 0.72f, 0.55f, 0.18f);
			case CHROME, RAINBOW -> ARGB.colorFromFloat(1f, 0.55f, 0.58f, 0.62f);
			case SAKURA -> ARGB.colorFromFloat(1f, 0.75f, 0.45f, 0.55f);
			case OBSIDIAN -> ARGB.colorFromFloat(1f, 0.35f, 0.28f, 0.4f);
			default -> ARGB.colorFromFloat(1f, 0.42f, 0.42f, 0.45f);
		};
		int ring = ARGB.colorFromFloat(1f, 0.2f, 0.18f, 0.16f);

		// Chrome / rainbow get a moving specular stripe
		if (mat == KnifeMaterial.CHROME || mat == KnifeMaterial.RAINBOW) {
			float pulse = 0.55f + 0.45f * (0.5f + 0.5f * Mth.sin(time * 0.4f));
			shine = ARGB.colorFromFloat(0.9f, pulse, pulse, Math.min(1f, pulse + 0.05f));
		}
		return new Palette(blade, edge, shine, shade, guard, grip, ring);
	}

	private static void fill(GuiGraphics g, int x0, int y0, int x1, int y1, int argb) {
		g.fill(Math.min(x0, x1), Math.min(y0, y1), Math.max(x0, x1), Math.max(y0, y1), argb);
	}

	/** Soft tapered tip — stacked narrowing rects */
	private static void tip(GuiGraphics g, int cx, int yBase, int halfW, int height, int col) {
		for (int i = 0; i < height; i++) {
			float t = i / (float) height;
			int w = Math.max(1, (int) (halfW * (1f - t)));
			fill(g, cx - w, yBase - i - 1, cx + w, yBase - i, col);
		}
	}

	/** Full knife: grip + bolster + blade body + edge bevel + tip + chrome stripe */
	private static void drawStraightBlade(GuiGraphics g, Palette p,
		int bladeHalf, int bladeLen, int tipLen, boolean clipPoint) {
		// grip
		fill(g, -5, 2, 5, 34, p.grip);
		fill(g, -4, 4, 4, 32, ARGB.colorFromFloat(1f, 0.22f, 0.18f, 0.16f));
		fill(g, -3, 8, -1, 28, ARGB.colorFromFloat(0.35f, 1f, 1f, 1f)); // grip highlight
		// bolster / guard
		fill(g, -bladeHalf - 4, -2, bladeHalf + 4, 4, p.guard);
		fill(g, -bladeHalf - 2, -1, bladeHalf + 2, 3, p.shine);
		// blade body
		fill(g, -bladeHalf, -bladeLen, bladeHalf, 2, p.blade);
		// edge bevel (spine left, edge right)
		fill(g, bladeHalf - 3, -bladeLen + 4, bladeHalf, 0, p.edge);
		fill(g, -bladeHalf, -bladeLen + 4, -bladeHalf + 2, 0, p.shade);
		// chrome / metal stripe down the center
		fill(g, -1, -bladeLen + 6, 2, -2, p.shine);
		// tip
		if (clipPoint) {
			tip(g, 1, -bladeLen, bladeHalf, tipLen, p.blade);
			tip(g, 1, -bladeLen, Math.max(1, bladeHalf - 2), tipLen - 2, p.shine);
		} else {
			tip(g, 0, -bladeLen, bladeHalf, tipLen, p.blade);
			tip(g, 0, -bladeLen, Math.max(1, bladeHalf - 2), tipLen - 2, p.edge);
		}
	}

	private static void drawButterfly(GuiGraphics g, Palette p, float time) {
		boolean open = Mth.sin(time * 0.28f) > -0.15f;
		// handle scales
		fill(g, -7, 0, 7, 30, p.grip);
		fill(g, -5, 2, 5, 28, ARGB.colorFromFloat(1f, 0.2f, 0.16f, 0.15f));
		fill(g, -6, 12, 6, 14, p.guard); // pivot
		if (open) {
			// left blade
			fill(g, -20, -52, -5, 2, p.blade);
			fill(g, -18, -48, -8, -2, p.shine);
			fill(g, -20, -52, -17, 0, p.edge);
			tip(g, -12, -52, 6, 10, p.blade);
			// right blade
			fill(g, 5, -52, 20, 2, p.blade);
			fill(g, 8, -48, 18, -2, p.shine);
			fill(g, 17, -52, 20, 0, p.edge);
			tip(g, 12, -52, 6, 10, p.blade);
		} else {
			fill(g, -8, -48, 8, 2, p.blade);
			fill(g, -5, -44, 5, -2, p.shine);
			tip(g, 0, -48, 7, 10, p.blade);
		}
	}

	private static void drawKarambit(GuiGraphics g, Palette p, float time) {
		// ring + grip
		fill(g, 2, 8, 16, 24, p.ring);
		fill(g, 5, 11, 13, 21, ARGB.colorFromFloat(0.55f, 0.05f, 0.05f, 0.06f)); // hole
		fill(g, -6, -4, 10, 18, p.grip);
		fill(g, -4, -2, 8, 16, ARGB.colorFromFloat(1f, 0.2f, 0.16f, 0.14f));
		// curved claw blade (stepped arc)
		int[][] segs = {
			{-4, -14, 8, -2},
			{-12, -26, 2, -10},
			{-22, -38, -6, -22},
			{-30, -48, -14, -34},
			{-34, -56, -20, -44},
			{-36, -62, -26, -52}
		};
		for (int[] s : segs) fill(g, s[0], s[1], s[2], s[3], p.blade);
		fill(g, -28, -50, -18, -36, p.shine);
		fill(g, -36, -62, -30, -48, p.edge);
		tip(g, -32, -62, 5, 8, p.blade);
	}

	private static void drawBayonet(GuiGraphics g, Palette p) {
		drawStraightBlade(g, p, 7, 58, 14, true);
		// rifle-style fuller
		fill(g, -2, -50, 2, -8, p.shade);
	}

	private static void drawStiletto(GuiGraphics g, Palette p) {
		fill(g, -4, 2, 4, 28, p.grip);
		fill(g, -3, 4, 3, 26, ARGB.colorFromFloat(1f, 0.2f, 0.16f, 0.14f));
		fill(g, -9, -2, 9, 3, p.guard);
		fill(g, -3, -72, 3, 2, p.blade);
		fill(g, -1, -68, 1, -4, p.shine);
		fill(g, 1, -70, 3, -2, p.edge);
		tip(g, 0, -72, 3, 12, p.blade);
	}

	private static void drawUrsus(GuiGraphics g, Palette p) {
		fill(g, -5, 2, 5, 26, p.grip);
		fill(g, -10, -4, 10, 4, p.guard);
		// wide leaf blade
		fill(g, -11, -36, 11, 0, p.blade);
		fill(g, -8, -50, 8, -32, p.blade);
		fill(g, -4, -58, 4, -48, p.blade);
		fill(g, -6, -44, 6, -8, p.shine);
		fill(g, 7, -36, 11, -4, p.edge);
		tip(g, 0, -58, 5, 10, p.blade);
	}

	private static void drawLongBlade(GuiGraphics g, Palette p) {
		drawStraightBlade(g, p, 6, 78, 16, false);
		fill(g, -12, -3, 12, 2, p.guard);
	}

	private static void drawShortBlade(GuiGraphics g, Palette p) {
		drawStraightBlade(g, p, 8, 36, 10, true);
	}

	private static void drawCurved(GuiGraphics g, Palette p) {
		fill(g, -5, 2, 5, 28, p.grip);
		fill(g, -4, 4, 4, 26, ARGB.colorFromFloat(1f, 0.2f, 0.16f, 0.14f));
		fill(g, -8, -2, 8, 3, p.guard);
		int[][] segs = {
			{-6, -18, 10, 2},
			{-2, -32, 16, -14},
			{4, -46, 22, -28},
			{10, -56, 26, -42},
			{16, -64, 28, -52}
		};
		for (int[] s : segs) fill(g, s[0], s[1], s[2], s[3], p.blade);
		fill(g, 2, -40, 14, -18, p.shine);
		fill(g, 20, -60, 28, -48, p.edge);
		tip(g, 24, -64, 5, 8, p.blade);
	}
}
