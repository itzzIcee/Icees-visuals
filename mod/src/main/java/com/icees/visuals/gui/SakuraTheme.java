package com.icees.visuals.gui;

/**
 * Sakura design tokens for the Orbit menu — soft blossom pinks on deep night.
 */
public final class SakuraTheme {
	public static final int BG = 0xE60C0814;
	public static final int PANEL = 0xF2140E1C;
	public static final int PANEL_SOFT = 0xCC1A1224;
	public static final int LINE = 0x33FFFFFF;
	public static final int TEXT = 0xFFFFF0F5;
	public static final int MUTED = 0xFFD4A5B8;
	public static final int ACCENT = 0xFFFF8FB8;
	public static final int ACCENT_HOT = 0xFFFFB7C5;
	public static final int ACCENT_DEEP = 0xFFE85A8C;
	public static final int CHIP_ON = 0x55FF8FB8;
	public static final int CHIP_OFF = 0x22FFFFFF;
	public static final int SUCCESS = 0xFFFFC0CB;

	private SakuraTheme() {
	}

	public static int withAlpha(int rgb, int alpha) {
		return (alpha << 24) | (rgb & 0x00FFFFFF);
	}
}
