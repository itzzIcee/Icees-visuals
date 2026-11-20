package com.icees.visuals.cosmetics;
public enum CapeStyle {
	SAKURA("Sakura"), AURORA("Aurora"), EMBER("Ember"), FROST("Frost"), PRISM("Prism"),
	NIGHT("Night"), BLOOM("Bloom"), TIDE("Tide"), GOLD("Gold");
	public final String title;
	CapeStyle(String title) { this.title = title; }
	public CapeStyle next() { CapeStyle[] a = values(); return a[(ordinal()+1)%a.length]; }
}
