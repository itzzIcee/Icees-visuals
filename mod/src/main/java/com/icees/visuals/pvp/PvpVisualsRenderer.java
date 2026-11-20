package com.icees.visuals.pvp;

import com.icees.visuals.config.IceesConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Extra PvP HUD visuals — hit markers, colour, crosshair, target cue,
 * plus hit / mace / spear particle bursts.
 */
public final class PvpVisualsRenderer {
	private static final Random RNG = new Random();
	private static final List<Burst> BURSTS = new ArrayList<>();
	private static float hitFlash;
	private static int lastSwing;

	private PvpVisualsRenderer() {}

	public static void tick(Minecraft client) {
		if (client.player == null || client.level == null) {
			hitFlash = 0f;
			BURSTS.clear();
			return;
		}
		if (hitFlash > 0f) hitFlash = Math.max(0f, hitFlash - 0.085f);

		Iterator<Burst> it = BURSTS.iterator();
		while (it.hasNext()) {
			Burst b = it.next();
			b.life--;
			b.x += b.vx;
			b.y += b.vy;
			b.vy += b.gravity;
			if (b.life <= 0) it.remove();
		}

		int swing = client.player.swingTime;
		if (client.player.swinging && swing > lastSwing) {
			boolean onTarget = client.hitResult instanceof EntityHitResult ehr
				&& ehr.getEntity() instanceof LivingEntity living
				&& living.isAlive()
				&& ehr.getEntity() != client.player;
			ItemStack main = client.player.getMainHandItem();
			boolean mace = main.is(Items.MACE);
			boolean spear = main.is(ItemTags.SPEARS);

			if (IceesConfig.pvpHitMarkers) {
				hitFlash = onTarget ? 1f : 0.45f;
			}
			if (onTarget) {
				int cx = client.getWindow().getGuiScaledWidth() / 2;
				int cy = client.getWindow().getGuiScaledHeight() / 2;
				if (mace && IceesConfig.pvpMaceParticles) {
					spawnBurst(cx, cy, Kind.MACE, 22 + IceesConfig.pvpParticleSize * 4);
				} else if (spear && IceesConfig.pvpSpearParticles) {
					spawnBurst(cx, cy, Kind.SPEAR, 18 + IceesConfig.pvpParticleSize * 3);
				} else if (IceesConfig.pvpHitParticles) {
					spawnBurst(cx, cy, Kind.HIT, 14 + IceesConfig.pvpParticleSize * 3);
				}
			}
		}
		lastSwing = swing;
	}

	public static void render(GuiGraphics graphics) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || client.level == null) return;
		if (client.options.hideGui) return;
		if (client.screen != null) return;

		int w = client.getWindow().getGuiScaledWidth();
		int h = client.getWindow().getGuiScaledHeight();
		int cx = w / 2;
		int cy = h / 2;

		if (IceesConfig.pvpCrosshair) drawCrosshair(graphics, cx, cy);
		if (IceesConfig.pvpHitMarkers && hitFlash > 0.02f) drawHitMarker(graphics, cx, cy, hitFlash);
		if (IceesConfig.pvpTargetCue) drawTargetCue(graphics, client, cx, cy);
		drawBursts(graphics);
	}

	private static void spawnBurst(int cx, int cy, Kind kind, int count) {
		float[] rgb = hitRgb();
		for (int i = 0; i < count; i++) {
			Burst b = new Burst();
			b.kind = kind;
			b.x = cx + RNG.nextFloat() * 10f - 5f;
			b.y = cy + RNG.nextFloat() * 10f - 5f;
			float ang = RNG.nextFloat() * Mth.TWO_PI;
			float spd = switch (kind) {
				case MACE -> 2.2f + RNG.nextFloat() * 4.5f;
				case SPEAR -> 3.5f + RNG.nextFloat() * 5.5f;
				case HIT -> 1.6f + RNG.nextFloat() * 3.2f;
			};
			b.vx = Mth.cos(ang) * spd;
			b.vy = Mth.sin(ang) * spd - (kind == Kind.MACE ? 1.2f : 0.2f);
			b.gravity = kind == Kind.MACE ? 0.18f : 0.04f;
			b.life = b.maxLife = switch (kind) {
				case MACE -> 18 + RNG.nextInt(14);
				case SPEAR -> 16 + RNG.nextInt(12);
				case HIT -> 12 + RNG.nextInt(10);
			};
			b.size = switch (kind) {
				case MACE -> 5f + RNG.nextFloat() * 7f + IceesConfig.pvpParticleSize;
				case SPEAR -> 3f + RNG.nextFloat() * 5f + IceesConfig.pvpParticleSize * 0.7f;
				case HIT -> 3f + RNG.nextFloat() * 4f + IceesConfig.pvpParticleSize * 0.6f;
			};
			// Colour shifts per kind
			float shade = 0.75f + RNG.nextFloat() * 0.35f;
			if (kind == Kind.MACE) {
				b.r = Math.min(1f, rgb[0] * 0.55f + 0.45f * shade);
				b.g = Math.min(1f, rgb[1] * 0.45f + 0.2f);
				b.b = Math.min(1f, rgb[2] * 0.35f);
			} else if (kind == Kind.SPEAR) {
				b.r = Math.min(1f, rgb[0] * 0.5f + 0.5f);
				b.g = Math.min(1f, rgb[1] * 0.6f + 0.45f);
				b.b = Math.min(1f, rgb[2] * 0.85f + 0.4f);
			} else {
				b.r = Math.min(1f, rgb[0] * shade);
				b.g = Math.min(1f, rgb[1] * shade);
				b.b = Math.min(1f, rgb[2] * shade);
			}
			b.rot = RNG.nextFloat() * Mth.TWO_PI;
			b.spin = (RNG.nextFloat() - 0.5f) * 0.4f;
			BURSTS.add(b);
		}
	}

	private static void drawBursts(GuiGraphics g) {
		for (Burst b : BURSTS) {
			float fade = Math.min(1f, b.life / (float) Math.max(1, b.maxLife * 0.45f));
			float a = 0.85f * fade;
			int col = ARGB.colorFromFloat(a, b.r, b.g, b.b);
			int soft = ARGB.colorFromFloat(a * 0.35f, b.r, b.g, b.b);
			int s = Math.max(2, (int) b.size);
			int x = Math.round(b.x);
			int y = Math.round(b.y);
			switch (b.kind) {
				case MACE -> {
					// chunky smash sparks
					g.fill(x - s, y - s / 2, x + s, y + s / 2, soft);
					g.fill(x - s / 2, y - s, x + s / 2, y + s, col);
					g.fill(x - 1, y - s - 2, x + 1, y + s + 2, col);
				}
				case SPEAR -> {
					// elongated streak in travel direction
					float dx = b.vx, dy = b.vy;
					float len = Mth.sqrt(dx * dx + dy * dy);
					if (len < 0.01f) len = 1f;
					dx /= len; dy /= len;
					int x2 = Math.round(b.x - dx * s * 2.2f);
					int y2 = Math.round(b.y - dy * s * 2.2f);
					drawLine(g, x, y, x2, y2, Math.max(1, s / 3), col);
					g.fill(x - 1, y - 1, x + 2, y + 2, ARGB.colorFromFloat(a, 1f, 1f, 1f));
				}
				case HIT -> {
					g.fill(x - s / 2, y - s / 2, x + s / 2, y + s / 2, soft);
					g.fill(x - 1, y - s / 2, x + 2, y + s / 2, col);
					g.fill(x - s / 2, y - 1, x + s / 2, y + 2, col);
				}
			}
		}
	}

	private static void drawLine(GuiGraphics g, int x0, int y0, int x1, int y1, int thick, int col) {
		int steps = Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0));
		if (steps <= 0) {
			g.fill(x0, y0, x0 + thick, y0 + thick, col);
			return;
		}
		for (int i = 0; i <= steps; i++) {
			float t = i / (float) steps;
			int x = Math.round(Mth.lerp(t, x0, x1));
			int y = Math.round(Mth.lerp(t, y0, y1));
			g.fill(x, y, x + thick, y + thick, col);
		}
	}

	private static float[] hitRgb() {
		if (IceesConfig.pvpHitColour) {
			return IceesConfig.hsvToRgb(IceesConfig.pvpHitHue, IceesConfig.pvpHitSat / 100f, IceesConfig.pvpHitVal / 100f);
		}
		if (IceesConfig.pvpTint.enabled) return IceesConfig.pvpTint.apply(1f, 0.55f, 0.7f);
		return new float[]{1f, 0.55f, 0.7f};
	}

	private static void drawCrosshair(GuiGraphics g, int cx, int cy) {
		float[] rgb = IceesConfig.pvpTint.enabled
			? IceesConfig.pvpTint.apply(1f, 0.62f, 0.78f)
			: new float[]{1f, 0.62f, 0.78f};
		int gap = 3 + IceesConfig.pvpCrosshairSize;
		int len = 4 + IceesConfig.pvpCrosshairSize;
		int thick = Math.max(1, IceesConfig.pvpCrosshairSize / 2);
		int col = ARGB.colorFromFloat(0.95f, rgb[0], rgb[1], rgb[2]);
		int soft = ARGB.colorFromFloat(0.35f, rgb[0], rgb[1], rgb[2]);
		g.fill(cx - gap - len - 1, cy - thick - 1, cx - gap + 1, cy + thick + 1, soft);
		g.fill(cx + gap - 1, cy - thick - 1, cx + gap + len + 1, cy + thick + 1, soft);
		g.fill(cx - thick - 1, cy - gap - len - 1, cx + thick + 1, cy - gap + 1, soft);
		g.fill(cx - thick - 1, cy + gap - 1, cx + thick + 1, cy + gap + len + 1, soft);
		g.fill(cx - gap - len, cy - thick, cx - gap, cy + thick, col);
		g.fill(cx + gap, cy - thick, cx + gap + len, cy + thick, col);
		g.fill(cx - thick, cy - gap - len, cx + thick, cy - gap, col);
		g.fill(cx - thick, cy + gap, cx + thick, cy + gap + len, col);
		g.fill(cx - 1, cy - 1, cx + 1, cy + 1, col);
	}

	private static void drawHitMarker(GuiGraphics g, int cx, int cy, float flash) {
		float[] rgb = hitRgb();
		float a = Mth.clamp(flash, 0f, 1f);
		int col = ARGB.colorFromFloat(a, 1f, 1f, 1f);
		int tint = ARGB.colorFromFloat(a * 0.9f, rgb[0], rgb[1], rgb[2]);
		int s = (int) (10 + (1f - a) * 6);
		drawLine(g, cx - s, cy - s, cx + s, cy + s, 2, col);
		drawLine(g, cx + s, cy - s, cx - s, cy + s, 2, col);
		drawLine(g, cx - s + 1, cy - s, cx + s + 1, cy + s, 1, tint);
		drawLine(g, cx + s - 1, cy - s, cx - s - 1, cy + s, 1, tint);
	}

	private static void drawTargetCue(GuiGraphics g, Minecraft client, int cx, int cy) {
		HitResult hit = client.hitResult;
		if (!(hit instanceof EntityHitResult ehr)) return;
		Entity e = ehr.getEntity();
		if (!(e instanceof LivingEntity living) || !living.isAlive()) return;
		if (e == client.player) return;

		float[] rgb = IceesConfig.pvpTint.enabled
			? IceesConfig.pvpTint.apply(1f, 0.55f, 0.7f)
			: new float[]{1f, 0.55f, 0.7f};
		int ring = ARGB.colorFromFloat(0.55f, rgb[0], rgb[1], rgb[2]);
		int r = 14 + IceesConfig.pvpCrosshairSize;
		int b = 5;
		g.fill(cx - r, cy - r, cx - r + b, cy - r + 2, ring);
		g.fill(cx - r, cy - r, cx - r + 2, cy - r + b, ring);
		g.fill(cx + r - b, cy - r, cx + r, cy - r + 2, ring);
		g.fill(cx + r - 2, cy - r, cx + r, cy - r + b, ring);
		g.fill(cx - r, cy + r - 2, cx - r + b, cy + r, ring);
		g.fill(cx - r, cy + r - b, cx - r + 2, cy + r, ring);
		g.fill(cx + r - b, cy + r - 2, cx + r, cy + r, ring);
		g.fill(cx + r - 2, cy + r - b, cx + r, cy + r, ring);

		String name = living.getName().getString();
		if (name.length() > 18) name = name.substring(0, 18) + "…";
		int tw = client.font.width(name);
		g.drawString(client.font, name, cx - tw / 2, cy + r + 6, ARGB.colorFromFloat(0.9f, rgb[0], rgb[1], rgb[2]), false);
	}

	private enum Kind { HIT, MACE, SPEAR }

	private static final class Burst {
		float x, y, vx, vy, gravity, size, rot, spin, r, g, b;
		int life, maxLife;
		Kind kind = Kind.HIT;
	}
}
