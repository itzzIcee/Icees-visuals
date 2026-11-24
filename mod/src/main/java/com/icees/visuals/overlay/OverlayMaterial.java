package com.icees.visuals.overlay;

/** Block outline styles — Rainbow or Custom colour only. */
public enum OverlayMaterial {
	RAINBOW("Rainbow"),
	CUSTOM("Custom");

	public final String title;

	OverlayMaterial(String title) {
		this.title = title;
	}

	public OverlayMaterial next() {
		OverlayMaterial[] a = values();
		return a[(ordinal() + 1) % a.length];
	}

	/** Migrate old materials (Sakura/Chrome/…) — only Rainbow + Custom remain. */
	public static OverlayMaterial fromSaved(String name) {
		if (name == null || "RAINBOW".equals(name)) return RAINBOW;
		if ("CUSTOM".equals(name) || "SOLID".equals(name)
			|| "GOLD".equals(name) || "SAKURA".equals(name)
			|| "SILVER".equals(name) || "CHROME".equals(name)) {
			return CUSTOM;
		}
		try {
			return OverlayMaterial.valueOf(name);
		} catch (IllegalArgumentException e) {
			return RAINBOW;
		}
	}
}
