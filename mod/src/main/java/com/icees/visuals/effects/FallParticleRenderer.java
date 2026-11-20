package com.icees.visuals.effects;

import com.icees.visuals.config.IceesConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Screen-space falling atmosphere: sakura petals, snow, or soft mist.
 * Drawn over the world HUD so it works reliably on 1.21.11.
 */
public final class FallParticleRenderer {
	private static final List<Particle> PARTICLES = new ArrayList<>();
	private static final Random RNG = new Random();
	private static int spawnCooldown;

	private FallParticleRenderer() {}

	public static void tick(Minecraft client) {
		if (client.level == null || client.player == null) {
			PARTICLES.clear();
			return;
		}
		FallTheme theme = IceesConfig.fallTheme;
		if (theme == FallTheme.OFF) {
			PARTICLES.clear();
			return;
		}

		int w = client.getWindow().getGuiScaledWidth();
		int h = client.getWindow().getGuiScaledHeight();
		int density = Math.max(1, IceesConfig.fallIntensity); // 1..5
		int max = 40 + density * 28;

		if (spawnCooldown > 0) spawnCooldown--;
		else {
			int spawn = 1 + density / 2;
			for (int i = 0; i < spawn && PARTICLES.size() < max; i++) {
				PARTICLES.add(spawnOne(theme, w, h));
			}
			spawnCooldown = Math.max(0, 3 - density / 2);
		}

		Iterator<Particle> it = PARTICLES.iterator();
		while (it.hasNext()) {
			Particle p = it.next();
			p.x += p.vx;
			p.y += p.vy;
			p.rot += p.spin;
			p.life--;
			if (p.life <= 0 || p.y > h + 20 || p.x < -40 || p.x > w + 40) {
				it.remove();
			}
		}
	}

	public static void render(GuiGraphics graphics, float tickDelta) {
		if (IceesConfig.fallTheme == FallTheme.OFF || PARTICLES.isEmpty()) return;
		Minecraft client = Minecraft.getInstance();
		if (client.level == null) return;

		for (Particle p : PARTICLES) {
			float px = p.x + p.vx * tickDelta;
			float py = p.y + p.vy * tickDelta;
			int alpha = Math.min(255, Math.max(30, (int) (p.a * 255)));
			int argb = (alpha << 24) | (p.rgb & 0xFFFFFF);
			int s = Math.max(1, (int) p.size);

			switch (p.kind) {
				case PETAL -> drawPetal(graphics, (int) px, (int) py, s, argb, p.rot);
				case SNOW -> drawSnowflake(graphics, (int) px, (int) py, s, argb);
				case MIST -> drawMist(graphics, (int) px, (int) py, s, argb);
			}
		}
	}

	private static Particle spawnOne(FallTheme theme, int w, int h) {
		Particle p = new Particle();
		p.x = RNG.nextFloat() * w;
		p.y = -10f - RNG.nextFloat() * 40f;
		p.life = 120 + RNG.nextInt(160);

		switch (theme) {
			case SAKURA_FALL -> {
				p.kind = Kind.PETAL;
				p.size = 2.5f + RNG.nextFloat() * 3.5f;
				p.vx = -0.35f + RNG.nextFloat() * 0.7f;
				p.vy = 0.35f + RNG.nextFloat() * 0.55f;
				p.spin = -0.08f + RNG.nextFloat() * 0.16f;
				p.a = 0.55f + RNG.nextFloat() * 0.35f;
				// pink / rose petal tones
				int[] petals = {0xFF8FB8, 0xFFB7C5, 0xFFC0D0, 0xE85A8C, 0xFFD6E0};
				p.rgb = petals[RNG.nextInt(petals.length)];
			}
			case WHITE_SNOW -> {
				p.kind = Kind.SNOW;
				p.size = 1.5f + RNG.nextFloat() * 2.8f;
				p.vx = -0.2f + RNG.nextFloat() * 0.4f;
				p.vy = 0.25f + RNG.nextFloat() * 0.45f;
				p.spin = 0f;
				p.a = 0.65f + RNG.nextFloat() * 0.3f;
				p.rgb = RNG.nextBoolean() ? 0xFFFFFF : 0xE8F0FF;
			}
			case SOFT_MIST -> {
				p.kind = Kind.MIST;
				p.size = 18f + RNG.nextFloat() * 36f;
				p.vx = -0.15f + RNG.nextFloat() * 0.3f;
				p.vy = 0.08f + RNG.nextFloat() * 0.18f;
				p.spin = 0f;
				p.a = 0.08f + RNG.nextFloat() * 0.12f;
				p.rgb = RNG.nextBoolean() ? 0xF5F7FA : 0xE8EEF5;
				p.life = 180 + RNG.nextInt(120);
				p.y = RNG.nextFloat() * h * 0.7f;
			}
			default -> {
				p.kind = Kind.SNOW;
				p.rgb = 0xFFFFFF;
				p.a = 0.5f;
				p.size = 2f;
				p.vy = 0.4f;
			}
		}
		return p;
	}

	private static void drawPetal(GuiGraphics g, int x, int y, int s, int argb, float rot) {
		// Simple diamond / petal cross — reads as blossom at small scale
		g.fill(x, y, x + s, y + Math.max(1, s / 2), argb);
		g.fill(x + Math.max(1, s / 3), y - Math.max(1, s / 3), x + Math.max(1, s / 3) + Math.max(1, s / 2), y + s, argb);
		int soft = (argb & 0x00FFFFFF) | (((argb >>> 24) / 2) << 24);
		g.fill(x - 1, y + 1, x + s + 1, y + 2, soft);
	}

	private static void drawSnowflake(GuiGraphics g, int x, int y, int s, int argb) {
		g.fill(x, y, x + s, y + s, argb);
		if (s >= 3) {
			g.fill(x - 1, y + s / 2, x + s + 1, y + s / 2 + 1, argb);
			g.fill(x + s / 2, y - 1, x + s / 2 + 1, y + s + 1, argb);
		}
	}

	private static void drawMist(GuiGraphics g, int x, int y, int s, int argb) {
		int soft = argb;
		g.fill(x, y, x + s, y + s / 2, soft);
		int softer = (argb & 0x00FFFFFF) | (Math.max(10, (argb >>> 24) / 2) << 24);
		g.fill(x + s / 4, y + s / 4, x + s + s / 3, y + s / 2 + 4, softer);
	}

	private enum Kind { PETAL, SNOW, MIST }

	private static final class Particle {
		float x, y, vx, vy, size, spin, rot, a;
		int life, rgb;
		Kind kind = Kind.SNOW;
	}
}
