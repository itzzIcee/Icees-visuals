package com.icees.visuals.config;

/**
 * Per-cosmetic colour channel (Hue / Sat / Val).
 */
public final class CosmeticTint {
	public boolean enabled;
	public int hue;
	public int sat;
	public int val;

	public CosmeticTint() {
		this(false, 330, 70, 100);
	}

	public CosmeticTint(boolean enabled, int hue, int sat, int val) {
		this.enabled = enabled;
		this.hue = Math.floorMod(hue, 360);
		this.sat = clamp(sat, 0, 100);
		this.val = clamp(val, 0, 100);
	}

	public float[] apply(float baseR, float baseG, float baseB) {
		if (!enabled) return new float[]{baseR, baseG, baseB};
		return IceesConfig.hsvToRgb(hue, sat / 100f, val / 100f);
	}

	public void cycleHue() {
		adjustHue(5);
	}

	public void adjustHue(int delta) {
		hue = Math.floorMod(hue + delta, 360);
		enabled = true;
	}

	public void cycleSat() {
		adjustSat(5);
	}

	public void adjustSat(int delta) {
		sat = clamp(sat + delta, 0, 100);
		enabled = true;
	}

	public void cycleVal() {
		adjustVal(5);
	}

	public void adjustVal(int delta) {
		val = clamp(val + delta, 0, 100);
		enabled = true;
	}

	public void toggle() {
		enabled = !enabled;
	}

	public CosmeticTint copy() {
		return new CosmeticTint(enabled, hue, sat, val);
	}

	public void loadFrom(CosmeticTint other, CosmeticTint fallback) {
		CosmeticTint src = other != null ? other : fallback;
		enabled = src.enabled;
		hue = Math.floorMod(src.hue, 360);
		sat = clamp(src.sat, 0, 100);
		val = clamp(src.val, 0, 100);
	}

	private static int clamp(int v, int lo, int hi) {
		return Math.max(lo, Math.min(hi, v));
	}
}
