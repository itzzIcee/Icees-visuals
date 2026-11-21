package com.icees.visuals.cosmetics;

/** Decorative shield cosmetics (back / side). */
public enum ShieldStyle {
	ROUND("Round", 1.00f, 0.62f, 0.78f),
	KITE("Kite", 0.75f, 0.85f, 1.00f),
	TOWER("Tower", 0.95f, 0.78f, 0.45f),
	SAKURA("Sakura", 1.00f, 0.55f, 0.72f),
	CRYSTAL("Crystal", 0.65f, 0.90f, 1.00f);

	public final String title;
	public final float r, g, b;

	ShieldStyle(String title, float r, float g, float b) {
		this.title = title;
		this.r = r;
		this.g = g;
		this.b = b;
	}

	public ShieldStyle next() {
		ShieldStyle[] a = values();
		return a[(ordinal() + 1) % a.length];
	}
}
