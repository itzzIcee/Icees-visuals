package com.icees.visuals.cosmetics;

/** Hats — all styles use hatTint for colour. */
public enum HatStyle {
	STRAW_RED("Straw Hat · Red Band", true),
	STRAW_BROWN("Straw Hat · Brown Band", true),
	CHINA("China Cone", true),
	TOP_HAT("Top Hat", false),
	BEANIE("Beanie", false),
	BERET("Beret", false),
	CROWN_BRIM("Crown Brim", false);

	public final String title;
	/** Legacy flag — tint is always available now. */
	public final boolean natural;

	HatStyle(String title, boolean natural) {
		this.title = title;
		this.natural = natural;
	}

	public HatStyle next() {
		HatStyle[] a = values();
		return a[(ordinal() + 1) % a.length];
	}

	public HatStyle prev() {
		HatStyle[] a = values();
		return a[(ordinal() + a.length - 1) % a.length];
	}
}
