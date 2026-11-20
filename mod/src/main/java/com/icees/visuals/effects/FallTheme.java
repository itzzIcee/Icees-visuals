package com.icees.visuals.effects;

/** Atmospheric fall / weather themes — world-space particles in the air. */
public enum FallTheme {
	OFF("Off", "No falling atmosphere."),
	SAKURA_FALL("Sakura Fall", "Cherry petals drifting in the air around you."),
	WHITE_SNOW("White Snow", "Soft snow falling through the world."),
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
