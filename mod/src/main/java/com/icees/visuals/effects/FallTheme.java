package com.icees.visuals.effects;

/** Atmospheric fall / weather themes drawn client-side. */
public enum FallTheme {
	OFF("Off", "No falling atmosphere."),
	SAKURA_FALL("Sakura Fall", "Pink cherry blossoms drifting down."),
	WHITE_SNOW("White Snow", "Soft white snowflakes falling quietly."),
	SOFT_MIST("Soft Mist", "Pale white mist drifting through the air.");

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
