package com.icees.visuals.cosmetics;
public enum WingStyle {
	PETAL("Petal"), ANGEL("Angel"), DRAGON("Dragon"), CRYSTAL("Crystal"), PHOENIX("Phoenix");
	public final String title;
	WingStyle(String title) { this.title = title; }
	public WingStyle next() { WingStyle[] a = values(); return a[(ordinal()+1)%a.length]; }
}
