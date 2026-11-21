package com.icees.visuals.gui;

import com.icees.visuals.config.IceesConfig;
import net.minecraft.util.ARGB;

/** Soft glass Orbit palette — driven by GUI colour settings. */
public final class SakuraTheme {
	private SakuraTheme() {}

	public static int scrim() {
		float[] a = IceesConfig.guiAccentRgb();
		return ARGB.colorFromFloat(0.72f, a[0] * 0.08f, a[1] * 0.04f, a[2] * 0.1f);
	}

	public static int text() {
		float[] a = IceesConfig.guiAccentRgb();
		return ARGB.colorFromFloat(1f,
			Math.min(1f, 0.92f + a[0] * 0.08f),
			Math.min(1f, 0.90f + a[1] * 0.08f),
			Math.min(1f, 0.94f + a[2] * 0.06f));
	}

	public static int muted() {
		float[] a = IceesConfig.guiAccentRgb();
		return ARGB.colorFromFloat(1f,
			0.55f + a[0] * 0.35f,
			0.45f + a[1] * 0.35f,
			0.55f + a[2] * 0.35f);
	}

	public static int accent() {
		float[] a = IceesConfig.guiAccentRgb();
		return ARGB.colorFromFloat(1f, a[0], a[1], a[2]);
	}

	public static int accentHot() {
		float[] a = IceesConfig.guiAccentRgb();
		return ARGB.colorFromFloat(1f,
			Math.min(1f, a[0] + 0.15f),
			Math.min(1f, a[1] + 0.15f),
			Math.min(1f, a[2] + 0.12f));
	}

	public static int title() {
		float[] a = IceesConfig.guiAccentRgb();
		return ARGB.colorFromFloat(1f,
			Math.min(1f, 0.85f + a[0] * 0.15f),
			Math.min(1f, 0.82f + a[1] * 0.15f),
			Math.min(1f, 0.88f + a[2] * 0.12f));
	}

	public static int titleGlow() {
		float[] a = IceesConfig.guiAccentRgb();
		return ARGB.colorFromFloat(0.55f, a[0], a[1], a[2]);
	}

	public static int scrimTint() {
		float[] a = IceesConfig.guiAccentRgb();
		return ARGB.colorFromFloat(0.55f, a[0], a[1] * 0.75f, a[2] * 0.9f);
	}

	/* Legacy constants kept for any leftover references — prefer methods above. */
	public static final int SCRIM = 0xBB080510;
	public static final int TEXT = 0xFFFFF7FA;
	public static final int MUTED = 0xFFD4A8BA;
	public static final int ACCENT = 0xFFFF9FBF;
	public static final int ACCENT_HOT = 0xFFFFC8D6;
	public static final int TITLE = 0xFFFFEAF0;
	public static final int TITLE_GLOW = 0x88FF8FB8;
}
