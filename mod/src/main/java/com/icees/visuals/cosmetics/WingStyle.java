package com.icees.visuals.cosmetics;

public enum WingStyle {
	PETAL("Petal", 1.00f, 0.70f, 0.82f),
	ANGEL("Angel", 1.00f, 0.92f, 0.95f),
	DRAGON("Dragon", 0.95f, 0.40f, 0.45f),
	CRYSTAL("Crystal", 0.85f, 0.75f, 1.00f),
	PHOENIX("Phoenix", 1.00f, 0.65f, 0.35f);

	public final String title;
	public final float r, g, b;

	WingStyle(String title, float r, float g, float b) {
		this.title = title;
		this.r = r;
		this.g = g;
		this.b = b;
	}

	public WingStyle next() {
		WingStyle[] all = values();
		return all[(ordinal() + 1) % all.length];
	}
}
