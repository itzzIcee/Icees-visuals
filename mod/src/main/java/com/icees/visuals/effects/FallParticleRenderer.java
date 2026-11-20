package com.icees.visuals.effects;

import com.icees.visuals.config.IceesConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.joml.Matrix3x2fStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Soft fall atmosphere — sakura / snow / mist overlay (always drawn when theme is on).
 */
public final class FallParticleRenderer {
	private static final Identifier TEX_PETAL = Identifier.fromNamespaceAndPath("icees-visuals", "textures/particle/petal.png");
	private static final Identifier TEX_PETAL_SOFT = Identifier.fromNamespaceAndPath("icees-visuals", "textures/particle/petal_soft.png");
	private static final Identifier TEX_SNOW = Identifier.fromNamespaceAndPath("icees-visuals", "textures/particle/snow.png");
	private static final Identifier TEX_MIST = Identifier.fromNamespaceAndPath("icees-visuals", "textures/particle/mist.png");

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
		if (client.screen != null && !client.screen.isPauseScreen()) {
			// Keep particles while Orbit is open so the preview stays alive
		}

		int w = Math.max(16, client.getWindow().getGuiScaledWidth());
		int h = Math.max(16, client.getWindow().getGuiScaledHeight());
		int density = Math.max(1, IceesConfig.fallIntensity);
		int max = switch (theme) {
			case SOFT_MIST -> 18 + density * 6;
			case WHITE_SNOW -> 90 + density * 40;
			default -> 70 + density * 35;
		};

		if (spawnCooldown > 0) {
			spawnCooldown--;
		} else {
			int spawn = theme == FallTheme.SOFT_MIST ? 2 : 2 + density;
			for (int i = 0; i < spawn && PARTICLES.size() < max; i++) {
				PARTICLES.add(spawnOne(theme, w, h));
			}
			spawnCooldown = theme == FallTheme.SOFT_MIST ? Math.max(1, 4 - density) : 0;
		}

		Iterator<Particle> it = PARTICLES.iterator();
		while (it.hasNext()) {
			Particle p = it.next();
			p.x += p.vx;
			p.y += p.vy;
			p.rot += p.spin;
			p.life--;
			p.x += (float) Math.sin((p.life + p.phase) * 0.045) * p.sway;
			if (p.life <= 0 || p.y > h + 60 || p.x < -80 || p.x > w + 80) {
				it.remove();
			}
		}
	}

	public static void render(GuiGraphics graphics, float tickDelta) {
		if (IceesConfig.fallTheme == FallTheme.OFF) return;
		Minecraft client = Minecraft.getInstance();
		if (client.level == null) return;

		// Ensure we always have something on screen (first frames / empty list)
		if (PARTICLES.isEmpty()) {
			int w = Math.max(16, client.getWindow().getGuiScaledWidth());
			int h = Math.max(16, client.getWindow().getGuiScaledHeight());
			for (int i = 0; i < 12; i++) {
				PARTICLES.add(spawnOne(IceesConfig.fallTheme, w, h));
			}
		}

		Matrix3x2fStack pose = graphics.pose();
		for (Particle p : PARTICLES) {
			float px = p.x + p.vx * tickDelta;
			float py = p.y + p.vy * tickDelta;
			float fade = Math.min(1f, p.life / 30f);
			float alpha = Math.min(1f, p.a * fade);
			int size = Math.max(6, (int) p.size);

			pose.pushMatrix();
			pose.translate(px, py);
			pose.rotate(p.rot);

			if (p.kind == Kind.MIST) {
				int mist = ARGB.colorFromFloat(alpha * 0.55f, p.r, p.g, p.b);
				pose.translate(-size / 2f, -size / 2f);
				graphics.blit(RenderPipelines.GUI_TEXTURED, p.tex, 0, 0, 0f, 0f, size, size, p.texSize, p.texSize, mist);
				// soft fill underlay so mist always reads
				graphics.fill(size / 4, size / 4, size * 3 / 4, size * 3 / 4, ARGB.colorFromFloat(alpha * 0.12f, p.r, p.g, p.b));
			} else {
				// Solid soft core (always visible) + textured petal/snow on top
				int core = ARGB.colorFromFloat(alpha * 0.55f, p.r, p.g, p.b);
				int outer = ARGB.colorFromFloat(alpha * 0.22f, p.r, p.g, p.b);
				int half = size / 2;
				int q = Math.max(2, size / 4);
				graphics.fill(-q, -half, q, half, outer);
				graphics.fill(-half, -q, half, q, outer);
				graphics.fill(-q, -q, q, q, core);

				pose.translate(-half, -half);
				int tint = ARGB.colorFromFloat(alpha, p.r, p.g, p.b);
				graphics.blit(RenderPipelines.GUI_TEXTURED, p.tex, 0, 0, 0f, 0f, size, size, p.texSize, p.texSize, tint);
			}

			pose.popMatrix();
		}
	}

	private static Particle spawnOne(FallTheme theme, int w, int h) {
		Particle p = new Particle();
		p.x = RNG.nextFloat() * w;
		p.y = -10f - RNG.nextFloat() * h * 0.35f;
		p.life = 160 + RNG.nextInt(200);
		p.phase = RNG.nextFloat() * 100f;
		p.sway = 0.2f + RNG.nextFloat() * 0.45f;

		switch (theme) {
			case SAKURA_FALL -> {
				p.kind = Kind.PETAL;
				p.tex = RNG.nextBoolean() ? TEX_PETAL : TEX_PETAL_SOFT;
				p.texSize = 256;
				p.size = 18f + RNG.nextFloat() * 28f;
				p.vx = -0.55f + RNG.nextFloat() * 1.1f;
				p.vy = 0.35f + RNG.nextFloat() * 0.65f;
				p.spin = -0.05f + RNG.nextFloat() * 0.1f;
				p.a = 0.85f + RNG.nextFloat() * 0.15f;
				float tint = RNG.nextFloat();
				p.r = 1.0f;
				p.g = 0.55f + tint * 0.25f;
				p.b = 0.72f + tint * 0.18f;
			}
			case WHITE_SNOW -> {
				p.kind = Kind.SNOW;
				p.tex = TEX_SNOW;
				p.texSize = 256;
				p.size = 10f + RNG.nextFloat() * 18f;
				p.vx = -0.35f + RNG.nextFloat() * 0.7f;
				p.vy = 0.28f + RNG.nextFloat() * 0.55f;
				p.spin = -0.02f + RNG.nextFloat() * 0.04f;
				p.a = 0.9f + RNG.nextFloat() * 0.1f;
				p.r = 1f;
				p.g = 1f;
				p.b = 1f;
				p.sway = 0.1f + RNG.nextFloat() * 0.25f;
			}
			case SOFT_MIST -> {
				p.kind = Kind.MIST;
				p.tex = TEX_MIST;
				p.texSize = 256;
				// Ground haze only — never cover the whole sky
				p.size = 70f + RNG.nextFloat() * 90f;
				p.vx = -0.12f + RNG.nextFloat() * 0.24f;
				p.vy = 0.02f + RNG.nextFloat() * 0.06f;
				p.spin = 0f;
				p.a = 0.18f + RNG.nextFloat() * 0.14f;
				p.r = 0.95f;
				p.g = 0.96f;
				p.b = 1.0f;
				p.life = 180 + RNG.nextInt(120);
				p.y = h * 0.55f + RNG.nextFloat() * h * 0.4f;
				p.sway = 0.05f;
			}
			default -> {
				p.kind = Kind.SNOW;
				p.tex = TEX_SNOW;
				p.texSize = 256;
				p.size = 8f;
				p.a = 0.5f;
				p.r = p.g = p.b = 1f;
				p.vy = 0.4f;
			}
		}
		return p;
	}

	private enum Kind { PETAL, SNOW, MIST }

	private static final class Particle {
		float x, y, vx, vy, size, spin, rot, a, r, g, b, sway, phase;
		int life, texSize = 64;
		Identifier tex = TEX_SNOW;
		Kind kind = Kind.SNOW;
	}
}
