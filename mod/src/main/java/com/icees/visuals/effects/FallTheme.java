package com.icees.visuals.effects;

/** Atmospheric fall / weather themes — soft world-space particles in the air. */
public enum FallTheme {
	OFF("Off", "No falling atmosphere."),
	SAKURA_FALL("Sakura Fall", "Cherry petals drifting in the air around you."),
	WHITE_SNOW("White Snow", "Soft snow falling through the world."),
	GOLDEN_LEAVES("Golden Leaves", "Warm autumn leaves drifting slowly."),
	EMBER_ASH("Ember Ash", "Soft glowing embers floating upward."),
	MOON_DUST("Moon Dust", "Pale silver motes in quiet air."),
	BLUE_BLOSSOM("Blue Blossom", "Cool blue petals in a gentle fall."),
	SOFT_MIST("Soft Mist", "Low ground haze in the air — not on your screen.");

	public final String title;
	public final String description;

	FallTheme(String title, String description) {
		this.title = title;
		this.description = description;
	}

	public FallTheme next() {
		FallTheme[] all = values();
		return all[(ordinal() + 1) % all.length];
	}
}
