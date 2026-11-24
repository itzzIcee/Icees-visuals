package com.icees.visuals.cosmetics;

/** Expandable rings under the player while airborne. */
public enum JumpCircleStyle {
	RIPPLE("Ripple", "Soft expanding rings."),
	DOUBLE("Double", "Two thick stacked rings."),
	PULSE("Pulse", "Breathing single ring."),
	HEX("Hex", "Hexagon shock rings."),
	STAR("Star", "Star-shaped burst rings."),
	SPIRAL("Spiral", "Twisting spiral rings."),
	SPARKS("Sparks", "Ring with spark dots."),
	SQUARE("Square", "Soft square frames."),
	HEART("Heart", "Heart-shaped bloom."),
	ORBIT("Orbit", "Orbiting bead ring."),
	WAVE("Wave", "Undulating wave rings."),
	PRISM("Prism", "Faceted crystal rings.");

	public final String title;
	public final String description;

	JumpCircleStyle(String title, String description) {
		this.title = title;
		this.description = description;
	}

	public JumpCircleStyle next() {
		JumpCircleStyle[] a = values();
		return a[(ordinal() + 1) % a.length];
	}

	public JumpCircleStyle prev() {
		JumpCircleStyle[] a = values();
		return a[(ordinal() + a.length - 1) % a.length];
	}
}
