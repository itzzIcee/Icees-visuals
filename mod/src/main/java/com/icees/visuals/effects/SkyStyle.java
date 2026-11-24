package com.icees.visuals.effects;

import net.minecraft.resources.Identifier;

/** Custom panoramic skies — OFF uses vanilla. */
public enum SkyStyle {
	OFF("Off", "Vanilla sky.", null),
	SAKURA_SKY("Sakura Sky", "Pink blossom haze over a soft night.", "sakura_sky"),
	BLOSSOM_DUSK("Blossom Dusk", "Warm rose sunset bloom.", "blossom_dusk"),
	RINGED_PLANET("Ringed Planet", "Purple space with a ringed world.", "ringed_planet"),
	VOID_ARCS("Void Arcs", "Dark void with glowing white arcs.", "void_arcs"),
	PURPLE_NEBULA("Purple Nebula", "Deep violet cosmic clouds.", "purple_nebula"),
	DARK_SWIRL("Dark Swirl", "Moody purple marble swirls.", "dark_swirl"),
	AURORA_TEAL("Aurora Teal", "Teal and lavender aurora wash.", "aurora_teal"),
	EMBER_GLOW("Ember Glow", "Dark fireglow atmosphere.", "ember_glow"),
	MINT_NIGHT("Mint Night", "Cool mint aurora night.", "mint_night");

	public final String title;
	public final String description;
	private final String texture;

	SkyStyle(String title, String description, String texture) {
		this.title = title;
		this.description = description;
		this.texture = texture;
	}

	public boolean isCustom() {
		return texture != null;
	}

	public Identifier textureId() {
		if (texture == null) return null;
		return Identifier.fromNamespaceAndPath("icees-visuals", "textures/sky/" + texture + ".png");
	}

	public SkyStyle next() {
		SkyStyle[] a = values();
		return a[(ordinal() + 1) % a.length];
	}

	public SkyStyle prev() {
		SkyStyle[] a = values();
		return a[(ordinal() + a.length - 1) % a.length];
	}
}
