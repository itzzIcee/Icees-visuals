package com.icees.visuals.cosmetics;

public enum KnifeStyle {
	BUTTERFLY("Butterfly", "Open → Flip → Twirl → Inspect"),
	KARAMBIT("Karambit", "Spin → Flick → Orbit → Inspect"),
	BAYONET("Bayonet", "Slash → Guard → Toss → Inspect"),
	STILETTO("Stiletto", "Draw → Point → Flip → Inspect"),
	URSUS("Ursus", "Snap → Roll → Catch → Inspect"),
	LONG_BLADE("Long Blade", "Draw → Arc → Guard → Inspect"),
	SHORT_BLADE("Short Blade", "Quick → Cross → Flash → Inspect"),
	CURVED("Curved Blade", "Sweep → Hook → Spin → Inspect");

	public final String title;
	public final String tricks;

	KnifeStyle(String title, String tricks) {
		this.title = title;
		this.tricks = tricks;
	}

	public KnifeStyle next() {
		KnifeStyle[] all = values();
		return all[(ordinal() + 1) % all.length];
	}
}
