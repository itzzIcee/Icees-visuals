package com.icees.visuals.gui;

import com.icees.visuals.config.IceesConfig;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Does NOT paint over the skin — only a clear “what’s on” strip under the preview.
 * Real cosmetics are drawn in 3D via {@link com.icees.visuals.mixin.GuiEntityRendererMixin}.
 */
final class CosmeticPreviewOverlay {
	private CosmeticPreviewOverlay() {}

	static void drawLabels(GuiGraphics g, Font font, int px, int py, int pw) {
		g.nextStratum();
		StringBuilder sb = new StringBuilder();
		append(sb, IceesConfig.wings, "Wings");
		append(sb, IceesConfig.halo, "Halo");
		append(sb, IceesConfig.hat, "Hat");
		append(sb, IceesConfig.cape, "Cape");
		append(sb, IceesConfig.aura, "Aura");
		append(sb, IceesConfig.shield, "Shield");
		append(sb, IceesConfig.jumpCircles, "Jump");
		append(sb, IceesConfig.trail, "Trail");
		String line = sb.isEmpty() ? "none on" : sb.toString();
		g.drawString(font, line, px + 4, py + 4, SakuraTheme.muted(), false);
	}

	private static void append(StringBuilder sb, boolean on, String name) {
		if (!on) return;
		if (!sb.isEmpty()) sb.append(" · ");
		sb.append(name);
	}
}
