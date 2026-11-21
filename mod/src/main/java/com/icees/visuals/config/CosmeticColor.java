package com.icees.visuals.config;

/**
 * Named colour presets for cosmetics — Red, Orange, Yellow, …
 */
public enum CosmeticColor {
	RED("Red", 0, 92, 100),
	ORANGE("Orange", 28, 92, 100),
	YELLOW("Yellow", 52, 90, 100),
	LIME("Lime", 95, 88, 100),
	GREEN("Green", 130, 80, 95),
	CYAN("Cyan", 180, 82, 100),
	BLUE("Blue", 220, 88, 100),
	PURPLE("Purple", 275, 78, 100),
	PINK("Pink", 320, 72, 100),
	SAKURA("Sakura", 330, 70, 100),
	WHITE("White", 0, 0, 100),
	BLACK("Black", 0, 0, 18);

	public final String title;
	public final int hue;
	public final int sat;
	public final int val;

	CosmeticColor(String title, int hue, int sat, int val) {
		this.title = title;
		this.hue = hue;
		this.sat = sat;
		this.val = val;
	}

	public void apply(CosmeticTint tint) {
		tint.enabled = true;
		tint.hue = Math.floorMod(hue, 360);
		tint.sat = sat;
		tint.val = val;
	}

	public CosmeticColor next() {
		CosmeticColor[] a = values();
		return a[(ordinal() + 1) % a.length];
	}

	public CosmeticColor prev() {
		CosmeticColor[] a = values();
		return a[(ordinal() + a.length - 1) % a.length];
	}

	/** Best-matching preset for the current tint (for the Orbit label). */
	public static CosmeticColor matching(CosmeticTint tint) {
		if (tint == null || !tint.enabled) return SAKURA;
		if (tint.sat <= 12) {
			return tint.val < 40 ? BLACK : WHITE;
		}
		CosmeticColor best = SAKURA;
		int bestDist = Integer.MAX_VALUE;
		for (CosmeticColor c : values()) {
			if (c.sat <= 12) continue;
			int d = Math.abs(Math.floorMod(tint.hue - c.hue + 180, 360) - 180);
			if (d < bestDist) {
				bestDist = d;
				best = c;
			}
		}
		return best;
	}
}
