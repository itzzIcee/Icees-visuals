package com.icees.visuals.pvp;

import com.icees.visuals.config.IceesConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * PvP combat FX — stunslam / mace (Zeus bolt) / spear / sword / axe animations + hit HUD.
 */
public final class PvpVisualsRenderer {
	private static final Identifier TEX_AURA = Identifier.fromNamespaceAndPath("icees-visuals", "textures/cosmetic/aura.png");
	private static final Identifier TEX_HALO = Identifier.fromNamespaceAndPath("icees-visuals", "textures/cosmetic/halo.png");
	private static final Random RNG = new Random();

	private static final List<HudBurst> HUD = new ArrayList<>();
	private static final List<WorldFx> WORLD = new ArrayList<>();
	private static float hitFlash;
	private static float slamPulse;
	private static float slashAngle;
	private static float slashLife;
	private static float axeLife;
	private static float spearLife;
	private static int lastSwing;

	private PvpVisualsRenderer() {}

	public static void tick(Minecraft client) {
		if (client.player == null || client.level == null) {
			hitFlash = 0f;
			slamPulse = 0f;
			slashLife = 0f;
			axeLife = 0f;
			spearLife = 0f;
			HUD.clear();
			WORLD.clear();
			return;
		}

		if (hitFlash > 0f) hitFlash = Math.max(0f, hitFlash - 0.08f);
		if (slamPulse > 0f) slamPulse = Math.max(0f, slamPulse - 0.06f);
		if (slashLife > 0f) slashLife = Math.max(0f, slashLife - 0.09f);
		if (axeLife > 0f) axeLife = Math.max(0f, axeLife - 0.08f);
		if (spearLife > 0f) spearLife = Math.max(0f, spearLife - 0.1f);

		Iterator<HudBurst> hit = HUD.iterator();
		while (hit.hasNext()) {
			HudBurst b = hit.next();
			b.life--;
			b.x += b.vx;
			b.y += b.vy;
			b.vy += b.gravity;
			if (b.life <= 0) hit.remove();
		}
		Iterator<WorldFx> wit = WORLD.iterator();
		while (wit.hasNext()) {
			WorldFx fx = wit.next();
			fx.life--;
			if (fx.life <= 0) wit.remove();
		}

		int swing = client.player.swingTime;
		if (client.player.swinging && swing > lastSwing) {
			boolean onTarget = client.hitResult instanceof EntityHitResult ehr
				&& ehr.getEntity() instanceof LivingEntity living
				&& living.isAlive()
				&& ehr.getEntity() != client.player;

			if (IceesConfig.pvpHitMarkers) {
				hitFlash = onTarget ? 1f : 0.4f;
			}

			if (onTarget) {
				EntityHitResult ehr = (EntityHitResult) client.hitResult;
				Entity target = ehr.getEntity();
				ItemStack main = client.player.getMainHandItem();
				WeaponKind kind = detect(main, (float) client.player.fallDistance);
				trigger(client, kind, target);
			}
		}
		lastSwing = swing;
	}

	private static WeaponKind detect(ItemStack main, float fallDistance) {
		if (main.is(Items.MACE)) {
			if (IceesConfig.pvpStunslam && fallDistance > 1.4f) return WeaponKind.STUNSLAM;
			return WeaponKind.MACE;
		}
		if (main.is(ItemTags.SPEARS)) return WeaponKind.SPEAR;
		if (main.is(ItemTags.SWORDS)) return WeaponKind.SWORD;
		if (main.is(ItemTags.AXES)) return WeaponKind.AXE;
		return WeaponKind.SWORD;
	}

	private static void trigger(Minecraft client, WeaponKind kind, Entity target) {
		int cx = client.getWindow().getGuiScaledWidth() / 2;
		int cy = client.getWindow().getGuiScaledHeight() / 2;
		Vec3 hitPos = target.position().add(0, target.getBbHeight() * 0.55, 0);

		switch (kind) {
			case STUNSLAM -> {
				if (!IceesConfig.pvpStunslam) return;
				int size = IceesConfig.pvpStunslamSize;
				slamPulse = 1f;
				spawnHudBurst(cx, cy, kind, 10 + size * 4);
				WORLD.add(WorldFx.ring(hitPos.x, target.getY(), hitPos.z, size, 22 + size * 4));
				WORLD.add(WorldFx.ring(hitPos.x, target.getY() + 0.05, hitPos.z, size, 16 + size * 3));
			}
			case MACE -> {
				if (!IceesConfig.pvpMace) return;
				int size = IceesConfig.pvpMaceSize;
				spawnHudBurst(cx, cy, kind, 14 + size * 4);
				double topY = hitPos.y + 6.0 + size * 1.4;
				WORLD.add(WorldFx.bolt(hitPos.x, topY, hitPos.z, hitPos.x, hitPos.y, hitPos.z, size, 14 + size * 2));
				for (int i = 0; i < 2 + size / 2; i++) {
					double ox = (RNG.nextDouble() - 0.5) * (0.8 + size * 0.25);
					double oz = (RNG.nextDouble() - 0.5) * (0.8 + size * 0.25);
					WORLD.add(WorldFx.bolt(
						hitPos.x + ox, hitPos.y + 3.5 + size * 0.6, hitPos.z + oz,
						hitPos.x, hitPos.y, hitPos.z, Math.max(1, size - 1), 10 + size));
				}
			}
			case SPEAR -> {
				if (!IceesConfig.pvpSpear) return;
				spearLife = 1f;
				spawnHudBurst(cx, cy, kind, 12 + IceesConfig.pvpSpearSize * 3);
				Vec3 from = client.player.getEyePosition(1f);
				WORLD.add(WorldFx.streak(from.x, from.y, from.z, hitPos.x, hitPos.y, hitPos.z, IceesConfig.pvpSpearSize, 10));
			}
			case SWORD -> {
				if (!IceesConfig.pvpSword) return;
				slashLife = 1f;
				slashAngle = (RNG.nextFloat() - 0.5f) * 50f;
				spawnHudBurst(cx, cy, kind, 10 + IceesConfig.pvpSwordSize * 3);
				WORLD.add(WorldFx.slash(hitPos.x, hitPos.y, hitPos.z, IceesConfig.pvpSwordSize, 12));
			}
			case AXE -> {
				if (!IceesConfig.pvpAxe) return;
				axeLife = 1f;
				spawnHudBurst(cx, cy, kind, 12 + IceesConfig.pvpAxeSize * 3);
				WORLD.add(WorldFx.cleave(hitPos.x, hitPos.y, hitPos.z, IceesConfig.pvpAxeSize, 14));
			}
		}
	}

	private static void spawnHudBurst(int cx, int cy, WeaponKind kind, int count) {
		float[] rgb = hitRgb();
		for (int i = 0; i < count; i++) {
			HudBurst b = new HudBurst();
			b.kind = kind;
			b.x = cx + RNG.nextFloat() * 12f - 6f;
			b.y = cy + RNG.nextFloat() * 12f - 6f;
			float ang = RNG.nextFloat() * Mth.TWO_PI;
			float spd = switch (kind) {
				case STUNSLAM -> 2.8f + RNG.nextFloat() * 4.5f;
				case MACE -> 2.4f + RNG.nextFloat() * 5f;
				case SPEAR -> 3.8f + RNG.nextFloat() * 5.5f;
				case SWORD -> 2.2f + RNG.nextFloat() * 3.8f;
				case AXE -> 2.6f + RNG.nextFloat() * 4.2f;
			};
			b.vx = Mth.cos(ang) * spd;
			b.vy = Mth.sin(ang) * spd - (kind == WeaponKind.STUNSLAM || kind == WeaponKind.MACE ? 1.1f : 0.2f);
			b.gravity = kind == WeaponKind.STUNSLAM || kind == WeaponKind.MACE ? 0.2f : 0.05f;
			b.life = b.maxLife = 12 + RNG.nextInt(12);
			b.size = 3f + RNG.nextFloat() * 5f + sizeOf(kind) * 0.7f;
			float shade = 0.75f + RNG.nextFloat() * 0.35f;
			if (kind == WeaponKind.MACE) {
				b.r = Math.min(1f, 0.55f * shade + rgb[0] * 0.5f);
				b.g = Math.min(1f, 0.75f * shade + rgb[1] * 0.35f);
				b.b = Math.min(1f, 1f * shade);
			} else if (kind == WeaponKind.STUNSLAM) {
				b.r = Math.min(1f, rgb[0] * 0.5f + 0.55f);
				b.g = Math.min(1f, rgb[1] * 0.45f + 0.35f);
				b.b = Math.min(1f, rgb[2] * 0.35f + 0.15f);
			} else {
				b.r = Math.min(1f, rgb[0] * shade);
				b.g = Math.min(1f, rgb[1] * shade);
				b.b = Math.min(1f, rgb[2] * shade);
			}
			HUD.add(b);
		}
	}

	private static int sizeOf(WeaponKind kind) {
		return switch (kind) {
			case STUNSLAM -> IceesConfig.pvpStunslamSize;
			case MACE -> IceesConfig.pvpMaceSize;
			case SPEAR -> IceesConfig.pvpSpearSize;
			case SWORD -> IceesConfig.pvpSwordSize;
			case AXE -> IceesConfig.pvpAxeSize;
		};
	}

	/* ——— HUD ——— */

	public static void renderHud(GuiGraphics g) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || client.level == null) return;
		if (client.options.hideGui) return;
		if (client.screen != null) return;

		int w = client.getWindow().getGuiScaledWidth();
		int h = client.getWindow().getGuiScaledHeight();
		int cx = w / 2;
		int cy = h / 2;

		if (IceesConfig.pvpCrosshair) drawCrosshair(g, cx, cy);
		if (IceesConfig.pvpHitMarkers && hitFlash > 0.02f) drawHitMarker(g, cx, cy, hitFlash);
		if (IceesConfig.pvpTargetCue) drawTargetCue(g, client, cx, cy);
		if (slamPulse > 0.02f && IceesConfig.pvpStunslam) drawSlamHud(g, cx, cy, slamPulse);
		if (slashLife > 0.02f && IceesConfig.pvpSword) drawSlashHud(g, cx, cy, slashLife);
		if (axeLife > 0.02f && IceesConfig.pvpAxe) drawAxeHud(g, cx, cy, axeLife);
		if (spearLife > 0.02f && IceesConfig.pvpSpear) drawSpearHud(g, cx, cy, spearLife);
		drawHudBursts(g);
	}

	private static void drawSlamHud(GuiGraphics g, int cx, int cy, float pulse) {
		float[] rgb = hitRgb();
		float a = pulse * 0.7f;
		int size = 18 + IceesConfig.pvpStunslamSize * 10;
		int r = (int) (size * (1.15f - pulse * 0.35f));
		int col = ARGB.colorFromFloat(a, rgb[0], rgb[1] * 0.7f, rgb[2] * 0.45f);
		int soft = ARGB.colorFromFloat(a * 0.35f, rgb[0], rgb[1], rgb[2]);
		ring2d(g, cx, cy, r, 3, col);
		ring2d(g, cx, cy, (int) (r * 0.65f), 2, soft);
		g.fill(cx - 2, cy - r - 8, cx + 2, cy - r + 4, col);
	}

	private static void drawSlashHud(GuiGraphics g, int cx, int cy, float life) {
		float[] rgb = hitRgb();
		float a = life * 0.9f;
		int col = ARGB.colorFromFloat(a, rgb[0], rgb[1], rgb[2]);
		int soft = ARGB.colorFromFloat(a * 0.35f, 1f, 1f, 1f);
		float base = slashAngle;
		int reach = 22 + IceesConfig.pvpSwordSize * 8;
		for (int i = -2; i <= 2; i++) {
			float ang = (base + i * 14f - (1f - life) * 40f) * Mth.DEG_TO_RAD;
			int x2 = cx + Math.round(Mth.cos(ang) * reach);
			int y2 = cy + Math.round(Mth.sin(ang) * reach * 0.55f);
			drawLine(g, cx, cy, x2, y2, Math.max(1, IceesConfig.pvpSwordSize / 2 + 1), i == 0 ? col : soft);
		}
	}

	private static void drawAxeHud(GuiGraphics g, int cx, int cy, float life) {
		float[] rgb = hitRgb();
		float a = life * 0.85f;
		int col = ARGB.colorFromFloat(a, Math.min(1f, rgb[0] + 0.15f), rgb[1] * 0.75f, rgb[2] * 0.55f);
		int reach = 20 + IceesConfig.pvpAxeSize * 7;
		float open = 35f + (1f - life) * 25f;
		for (float ang = -open; ang <= open; ang += 7f) {
			float rad = (ang - 90f) * Mth.DEG_TO_RAD;
			int x2 = cx + Math.round(Mth.cos(rad) * reach);
			int y2 = cy + Math.round(Mth.sin(rad) * reach);
			drawLine(g, cx + 4, cy + 6, x2, y2, 2, col);
		}
	}

	private static void drawSpearHud(GuiGraphics g, int cx, int cy, float life) {
		float[] rgb = hitRgb();
		float a = life * 0.95f;
		int col = ARGB.colorFromFloat(a, rgb[0] * 0.7f + 0.3f, rgb[1] * 0.8f + 0.2f, Math.min(1f, rgb[2] + 0.35f));
		int soft = ARGB.colorFromFloat(a * 0.4f, 1f, 1f, 1f);
		int len = 28 + IceesConfig.pvpSpearSize * 8;
		int thick = Math.max(1, IceesConfig.pvpSpearSize / 2);
		drawLine(g, cx - len, cy, cx + len, cy, thick + 1, soft);
		drawLine(g, cx - len, cy, cx + len, cy, thick, col);
		g.fill(cx + len - 2, cy - 3, cx + len + 4, cy + 3, ARGB.colorFromFloat(a, 1f, 1f, 1f));
	}

	private static void drawHudBursts(GuiGraphics g) {
		for (HudBurst b : HUD) {
			float fade = Math.min(1f, b.life / (float) Math.max(1, b.maxLife * 0.45f));
			float a = 0.85f * fade;
			int col = ARGB.colorFromFloat(a, b.r, b.g, b.b);
			int soft = ARGB.colorFromFloat(a * 0.35f, b.r, b.g, b.b);
			int s = Math.max(2, (int) b.size);
			int x = Math.round(b.x);
			int y = Math.round(b.y);
			switch (b.kind) {
				case MACE, STUNSLAM -> {
					g.fill(x - s, y - s / 2, x + s, y + s / 2, soft);
					g.fill(x - s / 2, y - s, x + s / 2, y + s, col);
				}
				case SPEAR -> {
					float len = Mth.sqrt(b.vx * b.vx + b.vy * b.vy);
					float dx = len < 0.01f ? 1f : b.vx / len;
					float dy = len < 0.01f ? 0f : b.vy / len;
					drawLine(g, x, y, Math.round(b.x - dx * s * 2.2f), Math.round(b.y - dy * s * 2.2f), Math.max(1, s / 3), col);
				}
				case SWORD, AXE -> {
					g.fill(x - s / 2, y - 1, x + s / 2, y + 2, soft);
					g.fill(x - 1, y - s / 2, x + 2, y + s / 2, col);
				}
			}
		}
	}

	/* ——— WORLD ——— */

	public static void renderWorld(WorldRenderContext context) {
		if (WORLD.isEmpty()) return;
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || client.level == null) return;
		MultiBufferSource buffers = context.consumers();
		PoseStack matrices = context.matrices();
		if (buffers == null || matrices == null) return;

		Camera camera = client.gameRenderer.getMainCamera();
		Vec3 cam = camera.position();
		VertexConsumer vc = buffers.getBuffer(RenderTypes.eyes(TEX_AURA));
		VertexConsumer hard = buffers.getBuffer(RenderTypes.eyes(TEX_HALO));
		float[] rgb = hitRgb();

		for (WorldFx fx : WORLD) {
			float life = fx.life / (float) Math.max(1, fx.maxLife);
			float a = Math.min(1f, 0.35f + life * 0.75f);
			float size = 0.06f + fx.size * 0.045f;

			switch (fx.type) {
				case BOLT -> drawBolt(matrices, vc, hard, cam, fx, rgb, a, size);
				case RING -> drawRing(matrices, hard, cam, fx, rgb, a * 0.85f, size);
				case STREAK -> drawStreak(matrices, hard, cam, fx, rgb, a, size);
				case SLASH, CLEAVE -> drawArc(matrices, hard, cam, fx, rgb, a, size, fx.type == FxType.CLEAVE);
			}
		}
	}

	private static void drawBolt(PoseStack m, VertexConsumer soft, VertexConsumer hard, Vec3 cam,
		WorldFx fx, float[] rgb, float a, float size) {
		int segs = 8 + fx.size;
		double x0 = fx.x0, y0 = fx.y0, z0 = fx.z0;
		double x1 = fx.x1, y1 = fx.y1, z1 = fx.z1;
		double px = x0, py = y0, pz = z0;
		long seed = Double.doubleToLongBits(x0 * 13.1 + y0 * 7.7 + z0);
		Random local = new Random(seed ^ (fx.life * 31L));

		for (int i = 1; i <= segs; i++) {
			float t = i / (float) segs;
			double nx = Mth.lerp(t, x0, x1);
			double ny = Mth.lerp(t, y0, y1);
			double nz = Mth.lerp(t, z0, z1);
			if (i < segs) {
				float jag = (0.18f + fx.size * 0.06f) * (1f - t * 0.35f);
				nx += (local.nextDouble() - 0.5) * jag * 2;
				nz += (local.nextDouble() - 0.5) * jag * 2;
				ny += (local.nextDouble() - 0.5) * jag * 0.35;
			}
			beam(m, soft, cam, px, py, pz, nx, ny, nz, size * 2.2f, rgb[0], Math.min(1f, rgb[1] + 0.25f), 1f, a * 0.35f);
			beam(m, hard, cam, px, py, pz, nx, ny, nz, size, 0.85f, 0.95f, 1f, a);
			px = nx; py = ny; pz = nz;
		}
		// impact bloom
		m.pushPose();
		m.translate(x1 - cam.x, y1 - cam.y, z1 - cam.z);
		float s = 0.18f + fx.size * 0.08f;
		box(m, soft, -s, -s, -s, s, s, s, rgb[0], rgb[1], 1f, a * 0.55f);
		m.popPose();
	}

	private static void drawRing(PoseStack m, VertexConsumer vc, Vec3 cam, WorldFx fx, float[] rgb, float a, float size) {
		float progress = 1f - fx.life / (float) Math.max(1, fx.maxLife);
		float radius = (0.4f + fx.size * 0.35f) * (0.4f + progress * 1.6f);
		int segments = 28;
		m.pushPose();
		m.translate(fx.x0 - cam.x, fx.y0 - cam.y + 0.05, fx.z0 - cam.z);
		for (int i = 0; i < segments; i++) {
			float a0 = i / (float) segments * Mth.TWO_PI;
			float a1 = (i + 1) / (float) segments * Mth.TWO_PI;
			float x0 = Mth.cos(a0) * radius, z0 = Mth.sin(a0) * radius;
			float x1 = Mth.cos(a1) * radius, z1 = Mth.sin(a1) * radius;
			float t = size * 0.35f;
			box(m, vc, x0 - t, 0, z0 - t, x1 + t, t * 0.6f, z1 + t, rgb[0], rgb[1] * 0.75f, rgb[2] * 0.5f, a * (1f - progress));
		}
		m.popPose();
	}

	private static void drawStreak(PoseStack m, VertexConsumer vc, Vec3 cam, WorldFx fx, float[] rgb, float a, float size) {
		beam(m, vc, cam, fx.x0, fx.y0, fx.z0, fx.x1, fx.y1, fx.z1, size * 1.4f,
			rgb[0] * 0.7f + 0.3f, rgb[1] * 0.8f + 0.2f, Math.min(1f, rgb[2] + 0.4f), a);
	}

	private static void drawArc(PoseStack m, VertexConsumer vc, Vec3 cam, WorldFx fx, float[] rgb, float a, float size, boolean cleave) {
		float progress = 1f - fx.life / (float) Math.max(1, fx.maxLife);
		float radius = 0.55f + fx.size * 0.22f;
		int steps = cleave ? 10 : 8;
		float start = cleave ? -55f : -40f;
		float end = cleave ? 55f : 45f;
		float sweep = Mth.lerp(progress, start, end);
		m.pushPose();
		m.translate(fx.x0 - cam.x, fx.y0 - cam.y, fx.z0 - cam.z);
		double prevX = 0, prevY = 0, prevZ = 0;
		boolean first = true;
		for (int i = 0; i <= steps; i++) {
			float t = i / (float) steps;
			float ang = (start + (sweep - start) * t) * Mth.DEG_TO_RAD;
			double x = Mth.sin(ang) * radius;
			double y = Mth.cos(ang) * radius * (cleave ? 0.55 : 0.35);
			double z = -0.2 - t * 0.15;
			if (!first) {
				beamLocal(m, vc, prevX, prevY, prevZ, x, y, z, size * (cleave ? 1.5f : 1.1f), rgb[0], rgb[1], rgb[2], a * (1f - progress * 0.4f));
			}
			first = false;
			prevX = x; prevY = y; prevZ = z;
		}
		m.popPose();
	}

	private static void beam(PoseStack m, VertexConsumer vc, Vec3 cam,
		double x0, double y0, double z0, double x1, double y1, double z1,
		float thickness, float r, float g, float b, float a) {
		m.pushPose();
		m.translate(x0 - cam.x, y0 - cam.y, z0 - cam.z);
		beamLocal(m, vc, 0, 0, 0, x1 - x0, y1 - y0, z1 - z0, thickness, r, g, b, a);
		m.popPose();
	}

	private static void beamLocal(PoseStack m, VertexConsumer vc,
		double x0, double y0, double z0, double x1, double y1, double z1,
		float thickness, float r, float g, float b, float a) {
		float t = thickness;
		// approximate thick segment with boxes along the line
		int steps = Math.max(2, (int) (Math.sqrt(x1 * x1 + y1 * y1 + z1 * z1) / 0.35));
		for (int i = 0; i < steps; i++) {
			float u0 = i / (float) steps;
			float u1 = (i + 1) / (float) steps;
			float ax = (float) Mth.lerp(u0, x0, x1);
			float ay = (float) Mth.lerp(u0, y0, y1);
			float az = (float) Mth.lerp(u0, z0, z1);
			float bx = (float) Mth.lerp(u1, x0, x1);
			float by = (float) Mth.lerp(u1, y0, y1);
			float bz = (float) Mth.lerp(u1, z0, z1);
			float mx = (ax + bx) * 0.5f, my = (ay + by) * 0.5f, mz = (az + bz) * 0.5f;
			box(m, vc, mx - t, my - t, mz - t, mx + t, my + t, mz + t, r, g, b, a);
		}
	}

	private static void box(PoseStack matrices, VertexConsumer vc,
		float x0, float y0, float z0, float x1, float y1, float z1,
		float r, float g, float b, float a) {
		PoseStack.Pose pose = matrices.last();
		Matrix4f mat = pose.pose();
		int light = LightTexture.FULL_BRIGHT;
		int overlay = OverlayTexture.NO_OVERLAY;
		float aa = Mth.clamp(a, 0f, 1f);
		quad(mat, pose, vc, light, overlay, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, r, g, b, aa);
		quad(mat, pose, vc, light, overlay, x1, y0, z0, x0, y0, z0, x0, y1, z0, x1, y1, z0, r, g, b, aa);
		quad(mat, pose, vc, light, overlay, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0, r, g, b, aa);
		quad(mat, pose, vc, light, overlay, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1, r, g, b, aa);
		quad(mat, pose, vc, light, overlay, x1, y0, z1, x1, y0, z0, x1, y1, z0, x1, y1, z1, r, g, b, aa);
		quad(mat, pose, vc, light, overlay, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0, r, g, b, aa);
	}

	private static void quad(Matrix4f m, PoseStack.Pose pose, VertexConsumer vc, int light, int overlay,
		float x0, float y0, float z0, float x1, float y1, float z1,
		float x2, float y2, float z2, float x3, float y3, float z3,
		float r, float g, float b, float a) {
		vc.addVertex(m, x0, y0, z0).setColor(r, g, b, a).setUv(0f, 1f).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
		vc.addVertex(m, x1, y1, z1).setColor(r, g, b, a).setUv(1f, 1f).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
		vc.addVertex(m, x2, y2, z2).setColor(r, g, b, a).setUv(1f, 0f).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
		vc.addVertex(m, x3, y3, z3).setColor(r, g, b, a).setUv(0f, 0f).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
	}

	/* ——— shared HUD helpers ——— */

	private static float[] hitRgb() {
		if (IceesConfig.pvpHitColour) {
			return IceesConfig.hsvToRgb(IceesConfig.pvpHitHue, IceesConfig.pvpHitSat / 100f, IceesConfig.pvpHitVal / 100f);
		}
		if (IceesConfig.pvpTint.enabled) return IceesConfig.pvpTint.apply(1f, 0.55f, 0.7f);
		return new float[]{1f, 0.55f, 0.7f};
	}

	private static void drawCrosshair(GuiGraphics g, int cx, int cy) {
		float[] rgb = hitRgb();
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
		float[] rgb = hitRgb();
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

	private static void ring2d(GuiGraphics g, int cx, int cy, int radius, int thick, int col) {
		for (int i = 0; i < 36; i++) {
			float a0 = i / 36f * Mth.TWO_PI;
			float a1 = (i + 1) / 36f * Mth.TWO_PI;
			int x0 = cx + Math.round(Mth.cos(a0) * radius);
			int y0 = cy + Math.round(Mth.sin(a0) * radius);
			int x1 = cx + Math.round(Mth.cos(a1) * radius);
			int y1 = cy + Math.round(Mth.sin(a1) * radius);
			drawLine(g, x0, y0, x1, y1, thick, col);
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

	private enum WeaponKind { STUNSLAM, MACE, SPEAR, SWORD, AXE }

	private enum FxType { BOLT, RING, STREAK, SLASH, CLEAVE }

	private static final class HudBurst {
		float x, y, vx, vy, gravity, size, r, g, b;
		int life, maxLife;
		WeaponKind kind = WeaponKind.SWORD;
	}

	private static final class WorldFx {
		FxType type;
		double x0, y0, z0, x1, y1, z1;
		int size, life, maxLife;

		static WorldFx bolt(double x0, double y0, double z0, double x1, double y1, double z1, int size, int life) {
			WorldFx fx = new WorldFx();
			fx.type = FxType.BOLT;
			fx.x0 = x0; fx.y0 = y0; fx.z0 = z0;
			fx.x1 = x1; fx.y1 = y1; fx.z1 = z1;
			fx.size = size; fx.life = fx.maxLife = life;
			return fx;
		}

		static WorldFx ring(double x, double y, double z, int size, int life) {
			WorldFx fx = new WorldFx();
			fx.type = FxType.RING;
			fx.x0 = x; fx.y0 = y; fx.z0 = z;
			fx.size = size; fx.life = fx.maxLife = life;
			return fx;
		}

		static WorldFx streak(double x0, double y0, double z0, double x1, double y1, double z1, int size, int life) {
			WorldFx fx = bolt(x0, y0, z0, x1, y1, z1, size, life);
			fx.type = FxType.STREAK;
			return fx;
		}

		static WorldFx slash(double x, double y, double z, int size, int life) {
			WorldFx fx = ring(x, y, z, size, life);
			fx.type = FxType.SLASH;
			return fx;
		}

		static WorldFx cleave(double x, double y, double z, int size, int life) {
			WorldFx fx = ring(x, y, z, size, life);
			fx.type = FxType.CLEAVE;
			return fx;
		}
	}
}
