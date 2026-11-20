package com.icees.visuals.cosmetics;

/** Headwear cosmetics — straw stays natural (no colour tint). */
public enum HatStyle {
	STRAW("Straw Hat"),
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
