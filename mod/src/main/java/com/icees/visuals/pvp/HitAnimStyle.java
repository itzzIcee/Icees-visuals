package com.icees.visuals.pvp;

/** Selectable hit-marker animation style. */
public enum HitAnimStyle {
	CROSS("Cross"),
	RING("Ring"),
	SLASH("Slash"),
	FLASH("Flash"),
	SPARKS("Sparks");

	public final String title;

	HitAnimStyle(String title) {
		this.title = title;
	}

	public HitAnimStyle next() {
		HitAnimStyle[] a = values();
		return a[(ordinal() + 1) % a.length];
	}
}
