package com.icees.visuals.config;

/**
 * Named solid colour presets — distinct sat/val so they don't wash out the same on glow.
 */
public enum CosmeticColor {
	CRIMSON("Crimson", 355, 100, 78),
	RED("Red", 0, 100, 92),
	CORAL("Coral", 12, 85, 100),
	ORANGE("Orange", 28, 100, 100),
	AMBER("Amber", 40, 95, 95),
	GOLD("Gold", 48, 100, 88),
	YELLOW("Yellow", 55, 100, 100),
	LIME("Lime", 95, 100, 95),
	GREEN("Green", 130, 90, 72),
	MINT("Mint", 155, 55, 100),
	TEAL("Teal", 175, 85, 70),
	CYAN("Cyan", 185, 90, 100),
	SKY("Sky", 200, 70, 100),
	BLUE("Blue", 220, 100, 95),
	NAVY("Navy", 230, 85, 55),
	INDIGO("Indigo", 250, 80, 75),
	PURPLE("Purple", 275, 90, 90),
	VIOLET("Violet", 290, 75, 100),
	MAGENTA("Magenta", 305, 100, 95),
	PINK("Pink", 325, 70, 100),
	SAKURA("Sakura", 335, 55, 100),
	BROWN("Brown", 25, 70, 45),
	GRAY("Gray", 0, 0, 55),
	WHITE("White", 0, 0, 100),
	BLACK("Black", 0, 0, 14);

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
		tint.gradient = false;
		tint.hue = Math.floorMod(hue, 360);
		tint.sat = sat;
		tint.val = val;
		tint.hue2 = tint.hue;
		tint.sat2 = tint.sat;
		tint.val2 = tint.val;
		tint.gradientName = "OFF";
	}

	public CosmeticColor next() {
		CosmeticColor[] a = values();
		return a[(ordinal() + 1) % a.length];
	}

	public CosmeticColor prev() {
		CosmeticColor[] a = values();
		return a[(ordinal() + a.length - 1) % a.length];
	}

	/** Best-matching solid preset (ignores gradient pairs). */
	public static CosmeticColor matching(CosmeticTint tint) {
		if (tint == null || !tint.enabled) return SAKURA;
		if (tint.sat <= 12) {
			if (tint.val < 28) return BLACK;
			if (tint.val < 70) return GRAY;
			return WHITE;
		}
		CosmeticColor best = SAKURA;
		int bestScore = Integer.MAX_VALUE;
		for (CosmeticColor c : values()) {
			if (c.sat <= 12) continue;
			int dh = Math.abs(Math.floorMod(tint.hue - c.hue + 180, 360) - 180);
			int ds = Math.abs(tint.sat - c.sat);
			int dv = Math.abs(tint.val - c.val);
			int score = dh * 3 + ds + dv;
			if (score < bestScore) {
				bestScore = score;
				best = c;
			}
		}
		return best;
	}
}
