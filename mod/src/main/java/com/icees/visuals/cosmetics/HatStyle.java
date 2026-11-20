package com.icees.visuals.cosmetics;

/** Headwear — straw hats use fixed natural colours (no HSV tint). */
public enum HatStyle {
	STRAW_RED("Straw Hat · Red"),
	STRAW_BROWN("Straw Hat · Brown"),
	CHINA("China Hat");

	public final String title;

	HatStyle(String title) {
		this.title = title;
	}

	public HatStyle next() {
		HatStyle[] a = values();
		return a[(ordinal() + 1) % a.length];
	}
}
