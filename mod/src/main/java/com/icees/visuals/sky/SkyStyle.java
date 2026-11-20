package com.icees.visuals.sky;

import net.minecraft.util.ARGB;

/** Punchy, clearly distinct skies (easy to see vs vanilla). */
public enum SkyStyle {
	SAKURA("Sakura", "Bright pink blossom sky.", 1.00f, 0.45f, 0.68f),
	GALAXY("Galaxy", "Deep violet nebula sky.", 0.28f, 0.08f, 0.55f),
	AETHER("Aether", "Silver-white overcast glow.", 0.78f, 0.82f, 0.90f),
	NOCTURNE("Nocturne", "Dark lilac night sky.", 0.28f, 0.12f, 0.42f),
	AMETHYST("Amethyst", "Rich purple crystal sky.", 0.32f, 0.05f, 0.48f),
	OPALINE("Opaline", "Pearl cyan-lavender sky.", 0.45f, 0.82f, 0.95f),
	RAIN("Rain", "Heavy steel-blue rain sky.", 0.28f, 0.34f, 0.42f),
	FIRE("Fire", "Burning orange ember sky.", 0.98f, 0.32f, 0.12f);

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
		return ARGB.colorFromFloat(0.95f, Math.min(1f, r + 0.2f), Math.min(1f, g + 0.1f), Math.min(1f, b + 0.08f));
	}

	public int toFogArgb() {
		// Slightly darker / hazier for fog so the disc still reads
		return ARGB.colorFromFloat(1.0f, r * 0.85f, g * 0.85f, b * 0.85f);
	}

	public int toCloudArgb() {
		// Soft tinted clouds matching the sky (keep some white so they stay readable)
		float cr = Math.min(1f, r * 0.55f + 0.45f);
		float cg = Math.min(1f, g * 0.55f + 0.45f);
		float cb = Math.min(1f, b * 0.55f + 0.45f);
		return ARGB.colorFromFloat(0.92f, cr, cg, cb);
	}

	public SkyStyle next() {
		SkyStyle[] all = values();
		return all[(ordinal() + 1) % all.length];
	}
}
