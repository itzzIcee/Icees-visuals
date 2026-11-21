package com.icees.visuals.effects;

import com.icees.visuals.config.IceesConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Soft fall atmosphere in world space — petals / snow / ash / mist around you.
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

		Vec3 origin = client.player.position();
		int density = IceesConfig.clamp10(IceesConfig.fallIntensity);
		int sizeLvl = IceesConfig.clamp10(IceesConfig.fallSize);
		int chunks = IceesConfig.clampChunks(IceesConfig.fallRangeChunks);
		float range = IceesConfig.fallRangeBlocks();
		int max = switch (theme) {
			case SOFT_MIST -> 8 + density * 4;
			case WHITE_SNOW, MOON_DUST -> 40 + density * 18;
			case EMBER_ASH -> 24 + density * 12;
			default -> 30 + density * 15;
		};
		// Cover more area when range is larger without exploding particle count
		max = Math.min(420, max + (chunks - 1) * (8 + density * 3));

		if (spawnCooldown > 0) spawnCooldown--;
		else {
			int spawn = theme == FallTheme.SOFT_MIST ? 1 : 1 + density / 2;
			for (int i = 0; i < spawn && PARTICLES.size() < max; i++) {
				PARTICLES.add(spawnOne(theme, origin, sizeLvl));
			}
			spawnCooldown = theme == FallTheme.SOFT_MIST ? Math.max(2, 7 - density) : Math.max(0, 1);
		}

		double px = origin.x;
		double py = origin.y;
		double pz = origin.z;
		Iterator<Particle> it = PARTICLES.iterator();
		while (it.hasNext()) {
			Particle p = it.next();
			p.x += p.vx;
			p.y += p.vy;
			p.z += p.vz;
			p.rot += p.spin;
			p.life--;
			p.x += Math.sin((p.life + p.phase) * 0.035) * p.sway;
			p.z += Math.cos((p.life + p.phase) * 0.028) * p.sway * 0.65;

			double dx = p.x - px;
			double dy = p.y - py;
			double dz = p.z - pz;
			double distSq = dx * dx + dy * dy + dz * dz;
			boolean tooFar = distSq > (double) range * range;
			boolean fallen = p.kind != Kind.MIST && p.kind != Kind.EMBER && p.y < py - 2.5;
			boolean risen = p.kind == Kind.EMBER && p.y > py + 10;
			boolean mistGone = p.kind == Kind.MIST && (p.y > py + 3.5 || p.y < py - 0.5);
			if (p.life <= 0 || tooFar || fallen || risen || mistGone) it.remove();
		}
	}

	public static void render(WorldRenderContext context) {
		if (IceesConfig.fallTheme == FallTheme.OFF) return;
		Minecraft client = Minecraft.getInstance();
		if (client.level == null || client.player == null) return;
		MultiBufferSource consumers = context.consumers();
		PoseStack matrices = context.matrices();
		if (consumers == null || matrices == null) return;

		float partial = client.getDeltaTracker().getGameTimeDeltaPartialTick(false);
		Camera camera = client.gameRenderer.getMainCamera();
		Vec3 cam = camera.position();

		if (PARTICLES.isEmpty()) {
			Vec3 origin = client.player.position();
			int sizeLvl = Math.max(1, IceesConfig.fallSize);
			for (int i = 0; i < 10; i++) {
				PARTICLES.add(spawnOne(IceesConfig.fallTheme, origin, sizeLvl));
			}
		}

		for (Particle p : PARTICLES) {
			double wx = p.x + p.vx * partial;
			double wy = p.y + p.vy * partial;
			double wz = p.z + p.vz * partial;

			float fadeIn = Math.min(1f, (p.maxLife - p.life) / 24f);
			float fadeOut = Math.min(1f, p.life / 40f);
			float alpha = Math.min(1f, p.a * fadeIn * fadeOut * IceesConfig.fallOpacityFactor());
			if (alpha < 0.02f) continue;

			float half = p.size * 0.5f;
			VertexConsumer soft = consumers.getBuffer(RenderTypes.eyes(p.tex));

			matrices.pushPose();
			matrices.translate(wx - cam.x, wy - cam.y, wz - cam.z);
			matrices.mulPose(camera.rotation());
			matrices.mulPose(Axis.ZP.rotation(p.rot));

			float bloom = p.kind == Kind.MIST ? 1.55f : p.kind == Kind.EMBER ? 1.45f : 1.35f;
			quad(matrices, soft, -half * bloom, -half * bloom, half * bloom, half * bloom,
				p.r, p.g, p.b, alpha * (p.kind == Kind.MIST ? 0.22f : 0.28f));
			quad(matrices, soft, -half, -half, half, half, p.r, p.g, p.b, alpha);

			matrices.popPose();
		}
	}

	private static Particle spawnOne(FallTheme theme, Vec3 origin, int sizeLvl) {
		Particle p = new Particle();
		float sizeMul = 0.45f + sizeLvl * 0.14f;
		float speedMul = IceesConfig.fallSpeedFactor();
		float spreadMul = IceesConfig.fallSpreadFactor();
		float rangeBlocks = IceesConfig.fallRangeBlocks();
		p.life = 140 + RNG.nextInt(180);
		p.maxLife = p.life;
		p.phase = RNG.nextFloat() * 100f;
		p.sway = 0.012f + RNG.nextFloat() * 0.028f;
		p.rot = RNG.nextFloat() * Mth.TWO_PI;

		// Spawn across the configured chunk range; Spread fine-tunes density within it
		float spread = rangeBlocks * (0.55f + 0.45f * spreadMul);
		float ox = (RNG.nextFloat() - 0.5f) * spread * 2f;
		float oz = (RNG.nextFloat() - 0.5f) * spread * 2f;

		switch (theme) {
			case SAKURA_FALL -> fallPetal(p, origin, ox, oz, sizeMul, speedMul,
				1.0f, 0.58f, 0.74f, 0.28f, 0.18f);
			case BLUE_BLOSSOM -> fallPetal(p, origin, ox, oz, sizeMul, speedMul,
				0.55f, 0.72f, 1.0f, 0.18f, 0.05f);
			case GOLDEN_LEAVES -> {
				fallPetal(p, origin, ox, oz, sizeMul, speedMul, 1.0f, 0.72f, 0.28f, 0.18f, 0.08f);
				p.size *= 1.15f;
				p.vy *= 0.75f;
				p.spin *= 0.7f;
			}
			case WHITE_SNOW -> {
				p.kind = Kind.SNOW;
				p.tex = TEX_SNOW;
				p.size = (0.10f + RNG.nextFloat() * 0.18f) * sizeMul;
				placeAbove(p, origin, ox, oz, 5f, 9f);
				driftDown(p, 0.015f * speedMul, 0.03f * speedMul);
				p.spin = -0.02f + RNG.nextFloat() * 0.04f;
				p.a = 0.5f + RNG.nextFloat() * 0.35f;
				p.r = p.g = p.b = 1f;
				p.sway = 0.008f + RNG.nextFloat() * 0.018f;
			}
			case MOON_DUST -> {
				p.kind = Kind.SNOW;
				p.tex = TEX_SNOW;
				p.size = (0.06f + RNG.nextFloat() * 0.12f) * sizeMul;
				placeAbove(p, origin, ox, oz, 3f, 7f);
				driftDown(p, 0.006f * speedMul, 0.018f * speedMul);
				p.spin = -0.01f + RNG.nextFloat() * 0.02f;
				p.a = 0.35f + RNG.nextFloat() * 0.3f;
				p.r = 0.86f + RNG.nextFloat() * 0.1f;
				p.g = 0.90f + RNG.nextFloat() * 0.08f;
				p.b = 1.0f;
				p.sway = 0.015f + RNG.nextFloat() * 0.03f;
			}
			case EMBER_ASH -> {
				p.kind = Kind.EMBER;
				p.tex = TEX_PETAL_SOFT;
				p.size = (0.08f + RNG.nextFloat() * 0.14f) * sizeMul;
				p.x = origin.x + ox * 0.7f;
				p.y = origin.y + 0.2f + RNG.nextFloat() * 1.5f;
				p.z = origin.z + oz * 0.7f;
				p.vx = (-0.01f + RNG.nextFloat() * 0.02f) * speedMul;
				p.vy = (0.02f + RNG.nextFloat() * 0.045f) * speedMul;
				p.vz = (-0.01f + RNG.nextFloat() * 0.02f) * speedMul;
				p.spin = -0.03f + RNG.nextFloat() * 0.06f;
				p.a = 0.45f + RNG.nextFloat() * 0.4f;
				float tint = RNG.nextFloat();
				p.r = 1.0f;
				p.g = 0.35f + tint * 0.35f;
				p.b = 0.12f + tint * 0.15f;
				p.sway = 0.01f + RNG.nextFloat() * 0.02f;
			}
			case SOFT_MIST -> {
				p.kind = Kind.MIST;
				p.tex = TEX_MIST;
				p.size = (1.4f + RNG.nextFloat() * 1.8f) * sizeMul;
				float mistSpread = rangeBlocks * (0.45f + 0.35f * spreadMul);
				p.x = origin.x + (RNG.nextFloat() - 0.5f) * mistSpread * 2f;
				p.y = origin.y + 0.15f + RNG.nextFloat() * 1.4f;
				p.z = origin.z + (RNG.nextFloat() - 0.5f) * mistSpread * 2f;
				p.vx = (-0.004f + RNG.nextFloat() * 0.008f) * speedMul;
				p.vy = (0.001f + RNG.nextFloat() * 0.004f) * speedMul;
				p.vz = (-0.004f + RNG.nextFloat() * 0.008f) * speedMul;
				p.spin = 0f;
				p.a = 0.12f + RNG.nextFloat() * 0.1f;
				p.r = 0.96f;
				p.g = 0.97f;
				p.b = 1.0f;
				p.life = 160 + RNG.nextInt(120);
				p.maxLife = p.life;
				p.sway = 0.004f;
			}
			default -> {
				p.kind = Kind.SNOW;
				p.tex = TEX_SNOW;
				p.size = 0.12f * sizeMul;
				p.x = origin.x;
				p.y = origin.y + 6f;
				p.z = origin.z;
				p.vy = -0.02f * speedMul;
				p.a = 0.4f;
				p.r = p.g = p.b = 1f;
			}
		}
		return p;
	}

	private static void fallPetal(Particle p, Vec3 origin, float ox, float oz, float sizeMul, float speedMul,
		float baseR, float baseG, float baseB, float gVar, float bVar) {
		p.kind = Kind.PETAL;
		p.tex = RNG.nextBoolean() ? TEX_PETAL : TEX_PETAL_SOFT;
		p.size = (0.18f + RNG.nextFloat() * 0.28f) * sizeMul;
		placeAbove(p, origin, ox, oz, 4.5f, 8f);
		driftDown(p, 0.015f * speedMul, 0.035f * speedMul);
		p.spin = -0.04f + RNG.nextFloat() * 0.08f;
		p.a = 0.55f + RNG.nextFloat() * 0.35f;
		float tint = RNG.nextFloat();
		p.r = Math.min(1f, baseR);
		p.g = Math.min(1f, baseG + tint * gVar);
		p.b = Math.min(1f, baseB + tint * bVar);
	}

	private static void placeAbove(Particle p, Vec3 origin, float ox, float oz, float yMin, float ySpan) {
		p.x = origin.x + ox;
		p.y = origin.y + yMin + RNG.nextFloat() * ySpan;
		p.z = origin.z + oz;
	}

	private static void driftDown(Particle p, float minFall, float fallSpan) {
		p.vx = -0.02f + RNG.nextFloat() * 0.04f;
		p.vy = -(minFall + RNG.nextFloat() * fallSpan);
		p.vz = -0.02f + RNG.nextFloat() * 0.04f;
	}

	private static void quad(PoseStack matrices, VertexConsumer vc,
		float x0, float y0, float x1, float y1,
		float r, float g, float b, float a) {
		PoseStack.Pose pose = matrices.last();
		Matrix4f m = pose.pose();
		int light = LightTexture.FULL_BRIGHT;
		int overlay = OverlayTexture.NO_OVERLAY;
		float aa = Mth.clamp(a, 0f, 1f);
		vc.addVertex(m, x0, y0, 0).setColor(r, g, b, aa).setUv(0f, 1f).setOverlay(overlay).setLight(light).setNormal(pose, 0, 0, 1);
		vc.addVertex(m, x1, y0, 0).setColor(r, g, b, aa).setUv(1f, 1f).setOverlay(overlay).setLight(light).setNormal(pose, 0, 0, 1);
		vc.addVertex(m, x1, y1, 0).setColor(r, g, b, aa).setUv(1f, 0f).setOverlay(overlay).setLight(light).setNormal(pose, 0, 0, 1);
		vc.addVertex(m, x0, y1, 0).setColor(r, g, b, aa).setUv(0f, 0f).setOverlay(overlay).setLight(light).setNormal(pose, 0, 0, 1);
	}

	private enum Kind { PETAL, SNOW, MIST, EMBER }

	private static final class Particle {
		double x, y, z;
		float vx, vy, vz, size, spin, rot, a, r, g, b, sway, phase;
		int life, maxLife;
		Identifier tex = TEX_SNOW;
		Kind kind = Kind.SNOW;
	}
}
