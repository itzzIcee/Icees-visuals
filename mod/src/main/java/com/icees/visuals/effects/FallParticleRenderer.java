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
 * Soft, smooth fall atmosphere — textured petals / snow / mist (not pixel blocks).
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

		int w = client.getWindow().getGuiScaledWidth();
		int h = client.getWindow().getGuiScaledHeight();
		int density = Math.max(1, IceesConfig.fallIntensity);
		int max = switch (theme) {
			case SOFT_MIST -> 28 + density * 10;
			case WHITE_SNOW -> 70 + density * 35;
			default -> 55 + density * 30;
		};

		if (spawnCooldown > 0) {
			spawnCooldown--;
		} else {
			int spawn = theme == FallTheme.SOFT_MIST ? 1 : 1 + density / 2;
			for (int i = 0; i < spawn && PARTICLES.size() < max; i++) {
				PARTICLES.add(spawnOne(theme, w, h));
			}
			spawnCooldown = theme == FallTheme.SOFT_MIST ? Math.max(2, 6 - density) : Math.max(0, 2 - density / 3);
		}

		Iterator<Particle> it = PARTICLES.iterator();
		while (it.hasNext()) {
			Particle p = it.next();
			p.x += p.vx;
			p.y += p.vy;
			p.rot += p.spin;
			p.life--;
			// gentle sway
			p.x += Math.sin((p.life + p.phase) * 0.045) * p.sway;
			if (p.life <= 0 || p.y > h + 40 || p.x < -60 || p.x > w + 60) {
				it.remove();
			}
		}
	}

	public static void render(GuiGraphics graphics, float tickDelta) {
		if (IceesConfig.fallTheme == FallTheme.OFF || PARTICLES.isEmpty()) return;
		Minecraft client = Minecraft.getInstance();
		if (client.level == null) return;

		Matrix3x2fStack pose = graphics.pose();
		for (Particle p : PARTICLES) {
			float px = p.x + p.vx * tickDelta;
			float py = p.y + p.vy * tickDelta;
			float fade = Math.min(1f, p.life / 40f);
			float alpha = p.a * fade;
			int color = ARGB.colorFromFloat(alpha, p.r, p.g, p.b);
			int size = Math.max(4, (int) p.size);

			pose.pushMatrix();
			pose.translate(px, py);
			pose.rotate(p.rot);
			pose.translate(-size / 2f, -size / 2f);

			graphics.blit(
				RenderPipelines.GUI_TEXTURED,
				p.tex,
				0, 0,
				0f, 0f,
				size, size,
				p.texSize, p.texSize,
				color
			);

			// Soft outer bloom pass for sakura / snow
			if (p.kind != Kind.MIST && alpha > 0.2f) {
				int bloom = Math.max(size + 4, (int) (size * 1.35f));
				int bloomColor = ARGB.colorFromFloat(alpha * 0.28f, p.r, p.g, p.b);
				pose.translate(-(bloom - size) / 2f, -(bloom - size) / 2f);
				graphics.blit(
					RenderPipelines.GUI_TEXTURED,
					p.tex,
					0, 0,
					0f, 0f,
					bloom, bloom,
					p.texSize, p.texSize,
					bloomColor
				);
			}

			pose.popMatrix();
		}
	}

	private static Particle spawnOne(FallTheme theme, int w, int h) {
		Particle p = new Particle();
		p.x = RNG.nextFloat() * w;
		p.y = -20f - RNG.nextFloat() * 50f;
		p.life = 140 + RNG.nextInt(180);
		p.phase = RNG.nextFloat() * 100f;
		p.sway = 0.15f + RNG.nextFloat() * 0.35f;

		switch (theme) {
			case SAKURA_FALL -> {
				p.kind = Kind.PETAL;
				p.tex = RNG.nextBoolean() ? TEX_PETAL : TEX_PETAL_SOFT;
				p.texSize = 256;
				p.size = 22f + RNG.nextFloat() * 36f;
				p.vx = -0.45f + RNG.nextFloat() * 0.9f;
				p.vy = 0.28f + RNG.nextFloat() * 0.5f;
				p.spin = -0.04f + RNG.nextFloat() * 0.08f;
				p.a = 0.75f + RNG.nextFloat() * 0.25f;
				// soft pink / rose (not hard magenta)
				float tint = RNG.nextFloat();
				p.r = 1.0f;
				p.g = 0.72f + tint * 0.18f;
				p.b = 0.82f + tint * 0.12f;
			}
			case WHITE_SNOW -> {
				p.kind = Kind.SNOW;
				p.tex = TEX_SNOW;
				p.texSize = 256;
				p.size = 14f + RNG.nextFloat() * 24f;
				p.vx = -0.25f + RNG.nextFloat() * 0.5f;
				p.vy = 0.22f + RNG.nextFloat() * 0.4f;
				p.spin = -0.01f + RNG.nextFloat() * 0.02f;
				p.a = 0.8f + RNG.nextFloat() * 0.2f;
				p.r = 0.95f + RNG.nextFloat() * 0.05f;
				p.g = 0.96f + RNG.nextFloat() * 0.04f;
				p.b = 1.0f;
				p.sway = 0.08f + RNG.nextFloat() * 0.2f;
			}
			case SOFT_MIST -> {
				p.kind = Kind.MIST;
				p.tex = TEX_MIST;
				p.texSize = 256;
				p.size = 120f + RNG.nextFloat() * 180f;
				p.vx = -0.12f + RNG.nextFloat() * 0.24f;
				p.vy = 0.04f + RNG.nextFloat() * 0.12f;
				p.spin = 0f;
				p.a = 0.22f + RNG.nextFloat() * 0.18f;
				p.r = 0.96f;
				p.g = 0.97f;
				p.b = 1.0f;
				p.life = 220 + RNG.nextInt(160);
				p.y = RNG.nextFloat() * h * 0.75f;
				p.sway = 0.05f;
			}
			default -> {
				p.kind = Kind.SNOW;
				p.tex = TEX_SNOW;
				p.texSize = 256;
				p.size = 8f;
				p.a = 0.4f;
				p.r = p.g = p.b = 1f;
				p.vy = 0.3f;
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
