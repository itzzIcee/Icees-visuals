package com.icees.visuals.effects;

/** Particle behaviour for {@link FallTheme#CUSTOM}. */
public enum CustomThemeStyle {
	PETALS("Petals"),
	SNOW("Snow"),
	EMBER("Ember"),
	MIST("Mist");

	public final String title;

	CustomThemeStyle(String title) {
		this.title = title;
	}

	public CustomThemeStyle next() {
		CustomThemeStyle[] a = values();
		return a[(ordinal() + 1) % a.length];
	}

	public CustomThemeStyle prev() {
		CustomThemeStyle[] a = values();
		return a[(ordinal() + a.length - 1) % a.length];
	}
}
