package com.icees.visuals.sky;

import net.minecraft.util.ARGB;

/** Soft cinematic skies — washed, not neon vanilla colors. */
public enum SkyStyle {
	SAKURA("Sakura", "Soft pink dusk with drifting blossom light.", 0.92f, 0.62f, 0.74f),
	GALAXY("Galaxy", "Deep rose nebula with quiet stars.", 0.38f, 0.22f, 0.55f),
	AETHER("Aether", "Pale silver haze over soft graphite.", 0.72f, 0.74f, 0.80f),
	NOCTURNE("Nocturne", "Muted lilac night with soft edges.", 0.40f, 0.28f, 0.52f),
	AMETHYST("Amethyst", "Dusty violet marble glow.", 0.42f, 0.24f, 0.52f),
	OPALINE("Opaline", "Pearl lavender with cool cyan breath.", 0.62f, 0.78f, 0.86f),
	RAIN("Rain", "Soft blue-grey rain veil.", 0.42f, 0.46f, 0.52f),
	FIRE("Fire", "Warm ember clouds with sakura ash.", 0.88f, 0.48f, 0.32f);

	public final String title;
	public final String description;
	public final float r, g, b;

	SkyStyle(String title, String description, float r, float g, float b) {
		this.title = title;
		this.description = description;
		this.r = r; this.g = g; this.b = b;
	}

	public int toSkyArgb() {
		return ARGB.colorFromFloat(1.0f, r, g, b);
	}

	public int toSunriseArgb() {
		return ARGB.colorFromFloat(0.75f, Math.min(1f, r + 0.12f), Math.min(1f, g + 0.08f), Math.min(1f, b + 0.08f));
	}

	public SkyStyle next() {
		SkyStyle[] all = values();
		return all[(ordinal() + 1) % all.length];
	}
}
