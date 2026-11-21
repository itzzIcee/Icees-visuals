package com.icees.visuals.config;

/**
 * Two-colour gradient presets (Farbverläufe) for cosmetics.
 */
public enum CosmeticGradient {
	OFF("Off", false, 0, 0, 0, 0, 0, 0),
	SUNSET("Sunset", true, 20, 100, 100, 330, 80, 100),
	FIRE("Fire", true, 50, 100, 100, 0, 100, 85),
	OCEAN("Ocean", true, 185, 90, 100, 230, 100, 70),
	AURORA("Aurora", true, 140, 80, 95, 290, 75, 100),
	CANDY("Candy", true, 320, 70, 100, 275, 90, 100),
	GOLDEN("Golden", true, 48, 100, 95, 28, 100, 100),
	ICE("Ice", true, 190, 40, 100, 220, 70, 100),
	TOXIC("Toxic", true, 95, 100, 95, 160, 90, 70),
	MAGMA("Magma", true, 35, 100, 100, 355, 100, 70),
	RAINBOW("Rainbow", true, 0, 100, 100, 300, 100, 100),
	SAKURA_BLOOM("Sakura Bloom", true, 335, 50, 100, 310, 70, 100),
	MIDNIGHT("Midnight", true, 250, 70, 55, 300, 60, 85);

	public final String title;
	public final boolean on;
	public final int hue1, sat1, val1;
	public final int hue2, sat2, val2;

	CosmeticGradient(String title, boolean on, int h1, int s1, int v1, int h2, int s2, int v2) {
		this.title = title;
		this.on = on;
		this.hue1 = h1;
		this.sat1 = s1;
		this.val1 = v1;
		this.hue2 = h2;
		this.sat2 = s2;
		this.val2 = v2;
	}

	public void apply(CosmeticTint tint) {
		tint.enabled = true;
		tint.gradient = on;
		tint.gradientName = name();
		if (!on) {
			tint.hue2 = tint.hue;
			tint.sat2 = tint.sat;
			tint.val2 = tint.val;
			return;
		}
		tint.hue = Math.floorMod(hue1, 360);
		tint.sat = sat1;
		tint.val = val1;
		tint.hue2 = Math.floorMod(hue2, 360);
		tint.sat2 = sat2;
		tint.val2 = val2;
	}

	public CosmeticGradient next() {
		CosmeticGradient[] a = values();
		return a[(ordinal() + 1) % a.length];
	}

	public CosmeticGradient prev() {
		CosmeticGradient[] a = values();
		return a[(ordinal() + a.length - 1) % a.length];
	}

	public static CosmeticGradient matching(CosmeticTint tint) {
		if (tint == null || !tint.gradient) return OFF;
		if (tint.gradientName != null) {
			try {
				return valueOf(tint.gradientName);
			} catch (IllegalArgumentException ignored) {}
		}
		return SUNSET;
	}
}
