package com.icees.visuals.gui;

/** Set while Orbit Cosmetics preview is open — keeps the GUI entity mixin free of OrbitScreen imports. */
public final class CosmeticPreviewGate {
	public static volatile boolean active;

	private CosmeticPreviewGate() {}
}
