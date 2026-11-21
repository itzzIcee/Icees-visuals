package com.icees.visuals.cosmetics;

/** Selectable aura looks around the player. */
public enum AuraStyle {
	ORBIT("Orbit", "Soft crystals circling the body.", 1.00f, 0.70f, 0.85f, 4, 0.10f),
	PULSE("Pulse", "Breathing glow that expands and fades.", 1.00f, 0.75f, 0.90f, 1, 0.0f),
	FLAME("Flame", "Warm rising ember wisps.", 1.00f, 0.45f, 0.28f, 6, 0.14f),
	FROST("Frost", "Cool drifting ice motes.", 0.70f, 0.88f, 1.00f, 5, 0.08f),
	BLOOM("Bloom", "Sakura petal sparks around you.", 1.00f, 0.62f, 0.78f, 8, 0.12f),
	RING("Ring", "Flat glowing ring at the feet.", 1.00f, 0.80f, 0.90f, 1, 0.0f),
	SPARKS("Sparks", "Fast spinning spark trail.", 1.00f, 0.90f, 0.65f, 10, 0.22f),
	TWIN("Twin", "Two large side auras.", 0.90f, 0.65f, 1.00f, 2, 0.0f),
	SPIRAL("Spiral", "Rising spiral of light.", 0.85f, 0.70f, 1.00f, 7, 0.16f),
	HEARTBEAT("Heartbeat", "Double-pulse soft flare.", 1.00f, 0.55f, 0.70f, 1, 0.0f),
	DRAGONFLY("Dragonfly", "Teal wing-shard orbit.", 0.25f, 0.85f, 0.88f, 6, 0.14f),
	BEACON("Beacon", "Tall rising light columns.", 0.70f, 0.95f, 1.00f, 4, 0.06f),
	SHIELD("Shield", "Hex shell around the body.", 0.55f, 0.80f, 1.00f, 8, 0.0f),
	RIBBON("Ribbon", "Twisting light ribbons.", 1.00f, 0.55f, 0.75f, 5, 0.18f),
	CUBE("Cube Orbit", "Voxel cubes orbiting you.", 0.55f, 0.90f, 1.00f, 6, 0.12f),
	PETAL_STORM("Petal Storm", "Dense falling petal aura.", 1.00f, 0.62f, 0.78f, 12, 0.10f),
	MOON("Moon Glow", "Pale silver body glow.", 0.82f, 0.90f, 1.00f, 1, 0.0f),
	STORM("Storm", "Electric sparks around you.", 0.55f, 0.75f, 1.00f, 9, 0.24f),
	LOTUS("Lotus", "Soft lotus ring petals.", 1.00f, 0.70f, 0.85f, 8, 0.06f);

	public final String title;
	public final String description;
	public final float r, g, b;
	public final int count;
	public final float spin;

	AuraStyle(String title, String description, float r, float g, float b, int count, float spin) {
		this.title = title;
		this.description = description;
		this.r = r;
		this.g = g;
		this.b = b;
		this.count = count;
		this.spin = spin;
	}

	public AuraStyle next() {
		AuraStyle[] a = values();
		return a[(ordinal() + 1) % a.length];
	}
}
