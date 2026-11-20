package com.icees.visuals.sky;

/**
 * Eight living skies — Icees originals with a sakura-first palette.
 */
public enum SkyStyle {
	SAKURA("Sakura", "Soft pink dusk with drifting blossom light.", 1.00f, 0.55f, 0.72f),
	GALAXY("Galaxy", "Nebulae, aurora and a ringed planet in rose light.", 0.72f, 0.35f, 0.95f),
	AETHER("Aether", "Silver light flowing over dark anthracite.", 0.75f, 0.78f, 0.85f),
	NOCTURNE("Nocturne", "Soft violet clouds with lilac edges.", 0.55f, 0.30f, 0.75f),
	AMETHYST("Amethyst", "Deep violet marble with fine veins of light.", 0.45f, 0.18f, 0.70f),
	OPALINE("Opaline", "Iridescent veils of lavender, pearl and cyan.", 0.65f, 0.70f, 0.90f),
	RAIN("Rain", "Heavy blue-grey clouds and distant veils of rain.", 0.45f, 0.50f, 0.60f),
	FIRE("Fire", "Moving flame clouds with warm sakura embers.", 0.95f, 0.35f, 0.20f);

	public final String title;
	public final String description;
	public final float r;
	public final float g;
	public final float b;

	SkyStyle(String title, String description, float r, float g, float b) {
		this.title = title;
		this.description = description;
		this.r = r;
		this.g = g;
		this.b = b;
	}

	public SkyStyle next() {
		SkyStyle[] all = values();
		return all[(ordinal() + 1) % all.length];
	}

	public SkyStyle previous() {
		SkyStyle[] all = values();
		return all[(ordinal() + all.length - 1) % all.length];
	}
}
