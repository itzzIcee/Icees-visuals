package com.icees.visuals.config;

/**
 * Per-cosmetic colour: solid HSV and optional two-stop gradient.
 */
public final class CosmeticTint {
	public boolean enabled;
	public int hue;
	public int sat;
	public int val;
	/** When true, lerp between (hue,sat,val) and (hue2,sat2,val2). */
	public boolean gradient;
	public int hue2;
	public int sat2;
	public int val2;
	/** Saved gradient preset name, e.g. SUNSET / OFF. */
	public String gradientName = "OFF";

	public CosmeticTint() {
		this(false, 330, 70, 100);
	}

	public CosmeticTint(boolean enabled, int hue, int sat, int val) {
		this.enabled = enabled;
		this.hue = Math.floorMod(hue, 360);
		this.sat = clamp(sat, 0, 100);
		this.val = clamp(val, 0, 100);
		this.hue2 = this.hue;
		this.sat2 = this.sat;
		this.val2 = this.val;
		this.gradient = false;
		this.gradientName = "OFF";
	}

	/** Solid colour (or gradient midpoint). Keeps some base shading so styles stay readable. */
	public float[] apply(float baseR, float baseG, float baseB) {
		return apply(baseR, baseG, baseB, 0.5f);
	}

	/**
	 * @param t 0..1 blend along the gradient (ignored when gradient is off)
	 */
	public float[] apply(float baseR, float baseG, float baseB, float t) {
		if (!enabled) return new float[]{baseR, baseG, baseB};
		float[] solid = colorAt(t);
		// Multiply with base luminance so hat bands / wing layers keep contrast
		float lum = (baseR * 0.3f + baseG * 0.5f + baseB * 0.2f);
		float keep = 0.45f + lum * 0.55f;
		return new float[]{
			Math.min(1f, solid[0] * keep * 1.15f),
			Math.min(1f, solid[1] * keep * 1.15f),
			Math.min(1f, solid[2] * keep * 1.15f)
		};
	}

	/** Raw RGB at gradient position t (0..1). Rainbow sweeps hue over time when name is RAINBOW. */
	public float[] colorAt(float t) {
		t = Math.max(0f, Math.min(1f, t));
		if (!gradient) {
			return IceesConfig.hsvToRgb(hue, sat / 100f, val / 100f);
		}
		if ("RAINBOW".equals(gradientName)) {
			float h = (hue + t * 360f) % 360f;
			return IceesConfig.hsvToRgb(h, Math.max(sat, sat2) / 100f, Math.max(val, val2) / 100f);
		}
		float[] a = IceesConfig.hsvToRgb(hue, sat / 100f, val / 100f);
		float[] b = IceesConfig.hsvToRgb(hue2, sat2 / 100f, val2 / 100f);
		return new float[]{
			a[0] + (b[0] - a[0]) * t,
			a[1] + (b[1] - a[1]) * t,
			a[2] + (b[2] - a[2]) * t
		};
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
		CosmeticTint c = new CosmeticTint(enabled, hue, sat, val);
		c.gradient = gradient;
		c.hue2 = hue2;
		c.sat2 = sat2;
		c.val2 = val2;
		c.gradientName = gradientName != null ? gradientName : "OFF";
		return c;
	}

	public void loadFrom(CosmeticTint other, CosmeticTint fallback) {
		CosmeticTint src = other != null ? other : fallback;
		enabled = src.enabled;
		hue = Math.floorMod(src.hue, 360);
		sat = clamp(src.sat, 0, 100);
		val = clamp(src.val, 0, 100);
		gradient = src.gradient;
		hue2 = Math.floorMod(src.hue2, 360);
		sat2 = clamp(src.sat2, 0, 100);
		val2 = clamp(src.val2, 0, 100);
		gradientName = src.gradientName != null ? src.gradientName : (gradient ? "SUNSET" : "OFF");
	}

	private static int clamp(int v, int lo, int hi) {
		return Math.max(lo, Math.min(hi, v));
	}
}
