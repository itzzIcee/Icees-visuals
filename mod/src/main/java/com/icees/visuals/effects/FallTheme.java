package com.icees.visuals.effects;

/** Atmospheric fall / weather themes drawn client-side. */
public enum FallTheme {
	OFF("Off", "No falling atmosphere."),
	SAKURA_FALL("Sakura Fall", "Soft AI-bloom cherry petals drifting down."),
	WHITE_SNOW("White Snow", "Soft blurred snow drifting quietly."),
	SOFT_MIST("Soft Mist", "Ground haze only — never covers the sky.");

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
