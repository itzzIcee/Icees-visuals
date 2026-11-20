package com.icees.visuals.cosmetics;

import com.icees.visuals.config.IceesConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.joml.Matrix3x2fStack;

/**
 * First-person knife viewmodel — distinct shapes per KnifeStyle, tintable size/colour.
 * Does NOT spin a giant aura (that looked like a light beam).
 */
public final class KnifeHudRenderer {
	private KnifeHudRenderer() {}

	public static void render(GuiGraphics graphics) {
		if (!IceesConfig.knife) return;
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || client.level == null) return;
		if (client.screen != null) return;
		if (!client.options.getCameraType().isFirstPerson()) return;
		if (client.options.hideGui) return;

		float time = client.player.tickCount + client.getDeltaTracker().getGameTimeDeltaPartialTick(false);
		int sw = client.getWindow().getGuiScaledWidth();
		int sh = client.getWindow().getGuiScaledHeight();
		float size = IceesConfig.sizeFactor();
		float bright = IceesConfig.brightnessFactor();

		float[] rgb = IceesConfig.color(1f, 0.85f, 0.92f);
		int blade = ARGB.colorFromFloat(Mth.clamp(0.95f * bright, 0.4f, 1f), rgb[0], rgb[1], rgb[2]);
		int edge = ARGB.colorFromFloat(Mth.clamp(0.75f * bright, 0.3f, 1f), Math.min(1f, rgb[0] + 0.15f), Math.min(1f, rgb[1] + 0.15f), Math.min(1f, rgb[2] + 0.1f));
		int handle = ARGB.colorFromFloat(0.95f, 0.18f, 0.12f, 0.16f);
		int glow = ARGB.colorFromFloat(Mth.clamp(0.28f * bright, 0.08f, 0.45f), rgb[0], rgb[1], rgb[2]);

		// Subtle inspect motion — NOT a full 360 blur spin
		float bob = Mth.sin(time * 0.15f) * 4f;
		float tip = Mth.sin(time * 0.22f) * 6f;
		float inspect = switch (IceesConfig.knifeStyle) {
			case BUTTERFLY, KARAMBIT -> Mth.sin(time * 0.35f) * 18f;
			case BAYONET, LONG_BLADE -> Mth.sin(time * 0.12f) * 8f;
			case STILETTO, SHORT_BLADE -> Mth.sin(time * 0.28f) * 12f;
			case URSUS, CURVED -> Mth.sin(time * 0.2f) * 14f;
		};

		float scale = 0.85f + size * 0.35f;
		float cx = sw * 0.78f + tip;
		float cy = sh * 0.70f + bob;

		Matrix3x2fStack pose = graphics.pose();
		pose.pushMatrix();
		pose.translate(cx, cy);
		pose.rotate((float) Math.toRadians(-25f + inspect));
		pose.scale(scale, scale);

		// soft local glow (small, not a screen-long beam)
		fill(graphics, -28, -70, 28, 20, glow);

		switch (IceesConfig.knifeStyle) {
			case KARAMBIT -> drawKarambit(graphics, blade, edge, handle);
			case BUTTERFLY -> drawButterfly(graphics, blade, edge, handle, time);
			case BAYONET -> drawBayonet(graphics, blade, edge, handle);
			case STILETTO -> drawStiletto(graphics, blade, edge, handle);
			case URSUS -> drawUrsus(graphics, blade, edge, handle);
			case LONG_BLADE -> drawLongBlade(graphics, blade, edge, handle);
			case SHORT_BLADE -> drawShortBlade(graphics, blade, edge, handle);
			case CURVED -> drawCurved(graphics, blade, edge, handle);
		}

		pose.popMatrix();
	}

	private static void fill(GuiGraphics g, int x0, int y0, int x1, int y1, int argb) {
		g.fill(Math.min(x0, x1), Math.min(y0, y1), Math.max(x0, x1), Math.max(y0, y1), argb);
	}

	/** Approximate polygons with stacked fills */
	private static void drawButterfly(GuiGraphics g, int blade, int edge, int handle, float time) {
		boolean open = Mth.sin(time * 0.35f) > 0;
		// handle
		fill(g, -6, -8, 6, 28, handle);
		fill(g, -4, -6, 4, 24, ARGB.colorFromFloat(1f, 0.28f, 0.2f, 0.24f));
		if (open) {
			// two mirrored blades
			fill(g, -22, -55, -4, -5, blade);
			fill(g, 4, -55, 22, -5, blade);
			fill(g, -18, -52, -7, -10, edge);
			fill(g, 7, -52, 18, -10, edge);
			// tips
			fill(g, -16, -62, -8, -52, blade);
			fill(g, 8, -62, 16, -52, blade);
		} else {
			fill(g, -10, -48, 10, -6, blade);
			fill(g, -7, -44, 7, -10, edge);
			fill(g, -5, -54, 5, -46, blade);
		}
	}

	private static void drawKarambit(GuiGraphics g, int blade, int edge, int handle) {
		fill(g, -8, -5, 10, 22, handle);
		fill(g, 4, -8, 14, 8, handle); // finger ring
		fill(g, 6, -6, 12, 6, ARGB.colorFromFloat(0.5f, 0.1f, 0.1f, 0.12f));
		// curved blade (steps)
		fill(g, -6, -18, 8, -4, blade);
		fill(g, -14, -32, 2, -14, blade);
		fill(g, -22, -44, -4, -28, blade);
		fill(g, -28, -52, -10, -40, blade);
		fill(g, -30, -58, -16, -48, edge);
		fill(g, -12, -28, 0, -16, edge);
	}

	private static void drawBayonet(GuiGraphics g, int blade, int edge, int handle) {
		fill(g, -7, -4, 7, 26, handle);
		fill(g, -12, -8, 12, -2, handle); // crossguard
		fill(g, -8, -70, 8, -6, blade);
		fill(g, -5, -66, 5, -10, edge);
		fill(g, -4, -78, 4, -68, blade); // tip
	}

	private static void drawStiletto(GuiGraphics g, int blade, int edge, int handle) {
		fill(g, -5, -2, 5, 24, handle);
		fill(g, -10, -6, 10, 0, handle);
		fill(g, -3, -75, 3, -4, blade);
		fill(g, -2, -72, 2, -8, edge);
		fill(g, -2, -82, 2, -74, blade);
	}

	private static void drawUrsus(GuiGraphics g, int blade, int edge, int handle) {
		fill(g, -7, -4, 7, 22, handle);
		fill(g, -11, -10, 11, -2, blade); // wide base
		fill(g, -9, -40, 9, -8, blade);
		fill(g, -6, -55, 6, -36, blade);
		fill(g, -3, -64, 3, -52, edge);
	}

	private static void drawLongBlade(GuiGraphics g, int blade, int edge, int handle) {
		fill(g, -6, 0, 6, 20, handle);
		fill(g, -14, -6, 14, 2, handle);
		fill(g, -7, -88, 7, -4, blade);
		fill(g, -4, -84, 4, -8, edge);
		fill(g, -3, -96, 3, -86, blade);
	}

	private static void drawShortBlade(GuiGraphics g, int blade, int edge, int handle) {
		fill(g, -6, -2, 6, 20, handle);
		fill(g, -9, -6, 9, 0, handle);
		fill(g, -8, -42, 8, -4, blade);
		fill(g, -5, -38, 5, -8, edge);
		fill(g, -3, -48, 3, -40, blade);
	}

	private static void drawCurved(GuiGraphics g, int blade, int edge, int handle) {
		fill(g, -6, -2, 6, 22, handle);
		fill(g, -4, -20, 10, -2, blade);
		fill(g, 0, -38, 16, -16, blade);
		fill(g, 6, -52, 22, -34, blade);
		fill(g, 12, -60, 24, -48, edge);
		fill(g, 2, -30, 12, -14, edge);
	}
}
