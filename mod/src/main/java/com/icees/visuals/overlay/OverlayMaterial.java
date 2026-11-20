package com.icees.visuals.overlay;

public enum OverlayMaterial {
	SOLID("Solid"),
	SILVER("Silver"),
	CHROME("Chrome"),
	RAINBOW("Rainbow"),
	GOLD("Gold"),
	SAKURA("Sakura");

	public final String title;

	OverlayMaterial(String title) {
		this.title = title;
	}

	public OverlayMaterial next() {
		OverlayMaterial[] a = values();
		return a[(ordinal() + 1) % a.length];
	}
}
