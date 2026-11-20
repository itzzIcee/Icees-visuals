package com.icees.visuals.cosmetics;

/** Straw hats only — always original natural colours (no HSV). */
public enum HatStyle {
	STRAW_RED("Straw Hat · Red Band"),
	STRAW_BROWN("Straw Hat · Brown Band");

	public final String title;

	HatStyle(String title) {
		this.title = title;
	}

	public HatStyle next() {
		HatStyle[] a = values();
		return a[(ordinal() + 1) % a.length];
	}
}
