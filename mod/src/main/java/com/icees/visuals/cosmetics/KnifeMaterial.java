package com.icees.visuals.cosmetics;

/** Blade finish for knife cosmetics. */
public enum KnifeMaterial {
	STEEL("Steel"),
	CHROME("Chrome"),
	RAINBOW("Rainbow"),
	GOLD("Gold"),
	SAKURA("Sakura"),
	OBSIDIAN("Obsidian");

	public final String title;

	KnifeMaterial(String title) {
		this.title = title;
	}

	public KnifeMaterial next() {
		KnifeMaterial[] a = values();
		return a[(ordinal() + 1) % a.length];
	}
}
