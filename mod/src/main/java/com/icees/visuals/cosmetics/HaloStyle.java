package com.icees.visuals.cosmetics;
public enum HaloStyle {
	BLOSSOM("Blossom"), RING("Ring"), ORBIT("Orbit"), CROWN("Crown"), PULSE("Pulse"), NOVA("Nova");
	public final String title;
	HaloStyle(String title) { this.title = title; }
	public HaloStyle next() { HaloStyle[] a = values(); return a[(ordinal()+1)%a.length]; }
}
