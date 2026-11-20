package com.icees.visuals.sky;

import net.minecraft.util.ARGB;

/**
 * Punchy skies. SKY/FOG use RGB (no alpha) — EnvironmentAttributes.SKY_COLOR is RGB_COLOR.
 * Clouds use ARGB so they stay visible and tinted even at night.
 */
public enum SkyStyle {
	SAKURA("Sakura", "Bright pink blossom sky.", 1.00f, 0.52f, 0.72f),
	GALAXY("Galaxy", "Deep violet nebula sky.", 0.42f, 0.18f, 0.72f),
	AETHER("Aether", "Silver-white overcast glow.", 0.82f, 0.86f, 0.94f),
	NOCTURNE("Nocturne", "Dark lilac night sky.", 0.38f, 0.22f, 0.55f),
	AMETHYST("Amethyst", "Rich purple crystal sky.", 0.48f, 0.16f, 0.68f),
	OPALINE("Opaline", "Pearl cyan-lavender sky.", 0.52f, 0.86f, 0.96f),
	RAIN("Rain", "Heavy steel-blue rain sky.", 0.38f, 0.48f, 0.58f),
	FIRE("Fire", "Burning orange ember sky.", 1.00f, 0.42f, 0.18f);

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

	/** RGB_COLOR attribute — alpha must stay 0 like vanilla biome skies. */
	public int toSkyArgb() {
		return rgb(r, g, b);
	}

	public int toSunriseArgb() {
		return ARGB.colorFromFloat(0.95f, Math.min(1f, r + 0.22f), Math.min(1f, g + 0.12f), Math.min(1f, b + 0.1f));
	}

	public int toFogArgb() {
		return rgb(r * 0.88f, g * 0.88f, b * 0.88f);
	}

	/** Strong sky-tinted clouds (ARGB) — beats rain/thunder greyscale wash. */
	public int toCloudArgb() {
		float cr = Math.min(1f, r * 0.72f + 0.28f);
		float cg = Math.min(1f, g * 0.72f + 0.28f);
		float cb = Math.min(1f, b * 0.72f + 0.28f);
		return ARGB.colorFromFloat(1.0f, cr, cg, cb);
	}

	/** Opaque disc colour for renderSkyDisc (shader reads alpha). */
	public int toSkyDiscArgb() {
		return ARGB.colorFromFloat(1.0f, r, g, b);
	}

	private static int rgb(float r, float g, float b) {
		int ri = MthClamp(r);
		int gi = MthClamp(g);
		int bi = MthClamp(b);
		return (ri << 16) | (gi << 8) | bi;
	}

	private static int MthClamp(float v) {
		return Math.max(0, Math.min(255, Math.round(v * 255f)));
	}

	public SkyStyle next() {
		SkyStyle[] all = values();
		return all[(ordinal() + 1) % all.length];
	}
}
