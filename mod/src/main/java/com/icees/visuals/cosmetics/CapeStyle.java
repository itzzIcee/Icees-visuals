package com.icees.visuals.cosmetics;

public enum CapeStyle {
	SAKURA("Sakura", 0.95f, 0.45f, 0.65f, 1.00f, 0.80f, 0.88f),
	AURORA("Aurora", 0.55f, 0.45f, 0.95f, 1.00f, 0.70f, 0.85f),
	EMBER("Ember", 0.55f, 0.15f, 0.15f, 1.00f, 0.55f, 0.35f),
	FROST("Frost", 0.15f, 0.20f, 0.35f, 0.85f, 0.92f, 1.00f),
	PRISM("Prism", 0.40f, 0.15f, 0.55f, 1.00f, 0.70f, 0.95f),
	NIGHT("Night", 0.10f, 0.08f, 0.16f, 0.75f, 0.45f, 0.70f),
	BLOOM("Bloom", 0.55f, 0.10f, 0.30f, 1.00f, 0.55f, 0.70f),
	TIDE("Tide", 0.10f, 0.35f, 0.45f, 0.55f, 0.90f, 0.95f),
	GOLD("Gold", 0.35f, 0.20f, 0.05f, 1.00f, 0.80f, 0.35f);

	public final String title;
	public final float r1, g1, b1, r2, g2, b2;

	CapeStyle(String title, float r1, float g1, float b1, float r2, float g2, float b2) {
		this.title = title;
		this.r1 = r1;
		this.g1 = g1;
		this.b1 = b1;
		this.r2 = r2;
		this.g2 = g2;
		this.b2 = b2;
	}

	public CapeStyle next() {
		CapeStyle[] all = values();
		return all[(ordinal() + 1) % all.length];
	}
}
