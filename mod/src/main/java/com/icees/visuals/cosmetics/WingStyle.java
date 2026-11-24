package com.icees.visuals.cosmetics;

public enum WingStyle {
	PETAL("Petal", 1.00f, 0.62f, 0.78f),
	ANGEL("Angel", 0.95f, 0.92f, 0.98f),
	DRAGON("Dragon", 0.95f, 0.35f, 0.45f),
	CRYSTAL("Crystal", 0.75f, 0.85f, 1.00f),
	PHOENIX("Phoenix", 1.00f, 0.55f, 0.30f),
	DRAGONFLY("Dragonfly", 0.25f, 0.85f, 0.88f),
	FAIRY("Fairy", 0.85f, 0.70f, 1.00f),
	BAT("Bat", 0.35f, 0.22f, 0.40f),
	BUTTERFLY("Butterfly", 1.00f, 0.72f, 0.88f),
	LEAF("Leaf", 0.45f, 0.85f, 0.50f),
	ARCH("Arch", 0.92f, 0.95f, 1.00f),
	MECHA("Mecha", 0.55f, 0.78f, 1.00f);

	public final String title;
	public final float r, g, b;

	WingStyle(String title, float r, float g, float b) {
		this.title = title;
		this.r = r;
		this.g = g;
		this.b = b;
	}

	public WingStyle next() {
		WingStyle[] a = values();
		return a[(ordinal() + 1) % a.length];
	}
}
