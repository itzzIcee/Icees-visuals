package com.icees.visuals.effects;

import com.icees.visuals.config.IceesConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.joml.Matrix3x2fStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Soft AI-like fall atmosphere — blurred blooms only, no hard pixel fills.
 */
public final class FallParticleRenderer {
	private static final Identifier TEX_PETAL = Identifier.fromNamespaceAndPath("icees-visuals", "textures/particle/petal.png");
	private static final Identifier TEX_PETAL_SOFT = Identifier.fromNamespaceAndPath("icees-visuals", "textures/particle/petal_soft.png");
	private static final Identifier TEX_SNOW = Identifier.fromNamespaceAndPath("icees-visuals", "textures/particle/snow.png");
	private static final Identifier TEX_MIST = Identifier.fromNamespaceAndPath("icees-visuals", "textures/particle/mist.png");

	private static final List<Particle> PARTICLES = new ArrayList<>();
	private static final Random RNG = new Random();
	private static int spawnCooldown;
	private static FallTheme lastTheme = FallTheme.OFF;

	private FallParticleRenderer() {}

	public static void tick(Minecraft client) {
		if (client.level == null || client.player == null) {
			PARTICLES.clear();
			return;
		}
		FallTheme theme = IceesConfig.fallTheme;
		if (theme != lastTheme) {
			PARTICLES.clear();
			lastTheme = theme;
		}
		if (theme == FallTheme.OFF) {
			PARTICLES.clear();
			return;
		}

		int w = Math.max(16, client.getWindow().getGuiScaledWidth());
		int h = Math.max(16, client.getWindow().getGuiScaledHeight());
		int density = Math.max(1, IceesConfig.fallIntensity);
		int max = switch (theme) {
			case SOFT_MIST -> 10 + density * 3;
			case WHITE_SNOW -> 40 + density * 18;
			default -> 32 + density * 16;
		};

		if (spawnCooldown > 0) spawnCooldown--;
		else {
			int spawn = theme == FallTheme.SOFT_MIST ? 1 : 1 + density / 2;
			for (int i = 0; i < spawn && PARTICLES.size() < max; i++) {
				PARTICLES.add(spawnOne(theme, w, h));
			}
			spawnCooldown = theme == FallTheme.SOFT_MIST ? Math.max(2, 6 - density) : Math.max(0, 1);
		}

		Iterator<Particle> it = PARTICLES.iterator();
		while (it.hasNext()) {
			Particle p = it.next();
			p.x += p.vx;
			p.y += p.vy;
			p.rot += p.spin;
			p.life--;
			p.x += (float) Math.sin((p.life + p.phase) * 0.035) * p.sway;
			if (p.life <= 0 || p.y > h + 90 || p.x < -120 || p.x > w + 120) it.remove();
		}
	}

	public static void render(GuiGraphics graphics, float tickDelta) {
		if (IceesConfig.fallTheme == FallTheme.OFF) return;
		Minecraft client = Minecraft.getInstance();
		if (client.level == null || client.player == null) return;

		// Mist must never bury the sky when looking up
		float pitch = client.player.getXRot();
		boolean lookingUp = pitch < -28f;
		if (lookingUp && IceesConfig.fallTheme == FallTheme.SOFT_MIST) return;

		if (PARTICLES.isEmpty()) {
			int w = Math.max(16, client.getWindow().getGuiScaledWidth());
			int h = Math.max(16, client.getWindow().getGuiScaledHeight());
			for (int i = 0; i < 6; i++) PARTICLES.add(spawnOne(IceesConfig.fallTheme, w, h));
		}

		Matrix3x2fStack pose = graphics.pose();
		for (Particle p : PARTICLES) {
			if (lookingUp && p.kind == Kind.MIST) continue;

			float px = p.x + p.vx * tickDelta;
			float py = p.y + p.vy * tickDelta;
			float fadeIn = Math.min(1f, (p.maxLife - p.life) / 28f);
			float fadeOut = Math.min(1f, p.life / 55f);
			float alpha = Math.min(1f, p.a * fadeIn * fadeOut);
			int size = Math.max(12, (int) p.size);

			pose.pushMatrix();
			pose.translate(px, py);
			pose.rotate(p.rot);

			// Outer soft bloom layers (AI glow) — texture only, no hard fills
			int layers = p.kind == Kind.MIST ? 2 : 3;
			for (int i = layers; i >= 1; i--) {
				float scale = 1f + (i - 1) * 0.5f;
				float la = alpha * (0.16f / i);
				int col = ARGB.colorFromFloat(la, p.r, p.g, p.b);
				pose.pushMatrix();
				pose.scale(scale, scale);
				pose.translate(-size / 2f, -size / 2f);
				graphics.blit(RenderPipelines.GUI_TEXTURED, p.tex, 0, 0, 0f, 0f, size, size, p.texSize, p.texSize, col);
				pose.popMatrix();
			}

			pose.translate(-size / 2f, -size / 2f);
			int tint = ARGB.colorFromFloat(alpha * 0.8f, p.r, p.g, p.b);
			graphics.blit(RenderPipelines.GUI_TEXTURED, p.tex, 0, 0, 0f, 0f, size, size, p.texSize, p.texSize, tint);

			pose.popMatrix();
		}
	}

	private static Particle spawnOne(FallTheme theme, int w, int h) {
		Particle p = new Particle();
		p.x = RNG.nextFloat() * w;
		p.y = -40f - RNG.nextFloat() * 90f;
		p.life = 200 + RNG.nextInt(240);
		p.maxLife = p.life;
		p.phase = RNG.nextFloat() * 100f;
		p.sway = 0.28f + RNG.nextFloat() * 0.55f;
		p.rot = RNG.nextFloat() * Mth.TWO_PI;

		switch (theme) {
			case SAKURA_FALL -> {
				p.kind = Kind.PETAL;
				p.tex = RNG.nextBoolean() ? TEX_PETAL : TEX_PETAL_SOFT;
				p.texSize = 256;
				p.size = 32f + RNG.nextFloat() * 48f;
				p.vx = -0.45f + RNG.nextFloat() * 0.9f;
				p.vy = 0.18f + RNG.nextFloat() * 0.38f;
				p.spin = -0.025f + RNG.nextFloat() * 0.05f;
				p.a = 0.45f + RNG.nextFloat() * 0.3f;
				float tint = RNG.nextFloat();
				p.r = 1.0f;
				p.g = 0.58f + tint * 0.28f;
				p.b = 0.74f + tint * 0.18f;
			}
			case WHITE_SNOW -> {
				p.kind = Kind.SNOW;
				p.tex = TEX_SNOW;
				p.texSize = 256;
				p.size = 18f + RNG.nextFloat() * 32f;
				p.vx = -0.28f + RNG.nextFloat() * 0.56f;
				p.vy = 0.15f + RNG.nextFloat() * 0.35f;
				p.spin = -0.012f + RNG.nextFloat() * 0.024f;
				p.a = 0.4f + RNG.nextFloat() * 0.3f;
				p.r = 1f;
				p.g = 1f;
				p.b = 1f;
				p.sway = 0.12f + RNG.nextFloat() * 0.28f;
			}
			case SOFT_MIST -> {
				p.kind = Kind.MIST;
				p.tex = TEX_MIST;
				p.texSize = 256;
				p.size = 100f + RNG.nextFloat() * 120f;
				p.vx = -0.06f + RNG.nextFloat() * 0.12f;
				p.vy = 0.01f + RNG.nextFloat() * 0.03f;
				p.spin = 0f;
				p.a = 0.1f + RNG.nextFloat() * 0.08f;
				p.r = 0.96f;
				p.g = 0.97f;
				p.b = 1.0f;
				p.life = 220 + RNG.nextInt(160);
				p.maxLife = p.life;
				// Strict ground haze — never a skybox
				p.y = h * 0.68f + RNG.nextFloat() * h * 0.3f;
				p.sway = 0.03f;
			}
			default -> {
				p.kind = Kind.SNOW;
				p.tex = TEX_SNOW;
				p.texSize = 256;
				p.size = 14f;
				p.a = 0.35f;
				p.r = p.g = p.b = 1f;
				p.vy = 0.25f;
			}
		}
		return p;
	}

	private enum Kind { PETAL, SNOW, MIST }

	private static final class Particle {
		float x, y, vx, vy, size, spin, rot, a, r, g, b, sway, phase;
		int life, maxLife, texSize = 64;
		Identifier tex = TEX_SNOW;
		Kind kind = Kind.SNOW;
	}
}
