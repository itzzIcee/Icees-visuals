package com.icees.visuals.sky;

import net.minecraft.util.ARGB;

/** Eight living skies — each has a strong, distinct ARGB sky disc colour. */
public enum SkyStyle {
	SAKURA("Sakura", "Soft pink dusk with drifting blossom light.", 1.00f, 0.55f, 0.72f),
	GALAXY("Galaxy", "Deep violet nebula with rose aurora.", 0.45f, 0.18f, 0.85f),
	AETHER("Aether", "Silver light over dark anthracite.", 0.70f, 0.74f, 0.82f),
	NOCTURNE("Nocturne", "Soft violet clouds with lilac edges.", 0.42f, 0.22f, 0.62f),
	AMETHYST("Amethyst", "Deep violet marble with veins of light.", 0.38f, 0.12f, 0.58f),
	OPALINE("Opaline", "Iridescent lavender, pearl and cyan.", 0.55f, 0.78f, 0.88f),
	RAIN("Rain", "Heavy blue-grey clouds and distant rain.", 0.35f, 0.40f, 0.48f),
	FIRE("Fire", "Flame clouds with warm sakura embers.", 0.95f, 0.38f, 0.18f);

	public final String title;
	public final String description;
	public final float r, g, b;

	SkyStyle(String title, String description, float r, float g, float b) {
		this.title = title;
		this.description = description;
		this.r = r;
		this.g = g;
		this.b = b;
	}

	public int toSkyArgb() {
		return ARGB.colorFromFloat(1.0f, r, g, b);
	}

	public int toSunriseArgb() {
		return ARGB.colorFromFloat(0.85f, Math.min(1f, r + 0.15f), Math.min(1f, g + 0.05f), Math.min(1f, b + 0.1f));
	}

	public SkyStyle next() {
		SkyStyle[] all = values();
		return all[(ordinal() + 1) % all.length];
	}
}
