package com.icees.visuals.cosmetics;

public enum HaloStyle {
	BLOSSOM("Blossom", 1.00f, 0.75f, 0.86f),
	RING("Ring", 1.00f, 0.90f, 0.95f),
	ORBIT("Orbit", 0.85f, 0.70f, 1.00f),
	CROWN("Crown", 1.00f, 0.85f, 0.55f),
	PULSE("Pulse", 1.00f, 0.65f, 0.80f),
	NOVA("Nova", 1.00f, 0.95f, 0.85f),
	DOUBLE("Double Ring", 0.70f, 0.90f, 1.00f),
	CRESCENT("Crescent", 1.00f, 0.92f, 0.55f),
	HORNS("Horns", 0.95f, 0.95f, 0.98f),
	DIAMOND("Diamond", 0.55f, 0.95f, 1.00f),
	STAR("Star", 1.00f, 0.92f, 0.65f),
	HEX("Hex", 0.65f, 0.95f, 0.90f),
	SPIRAL("Spiral", 0.90f, 0.70f, 1.00f),
	FLAME("Flame", 1.00f, 0.55f, 0.30f);

	public final String title;
	public final float r, g, b;

	HaloStyle(String title, float r, float g, float b) {
		this.title = title;
		this.r = r;
		this.g = g;
		this.b = b;
	}

	public HaloStyle next() {
		HaloStyle[] a = values();
		return a[(ordinal() + 1) % a.length];
	}
}
