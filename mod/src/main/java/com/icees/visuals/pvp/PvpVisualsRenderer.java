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
 * PvP cosmetics — all combat FX are 3D world-space (shockwaves, debris, arcs).
 * No screen-space hit overlays.
 */
public final class PvpVisualsRenderer {
	private static final Identifier TEX = Identifier.fromNamespaceAndPath("icees-visuals", "textures/cosmetic/halo.png");
	private static final Random RNG = new Random();

	private static final List<Shockwave> WAVES = new ArrayList<>();
	private static final List<Debris> DEBRIS = new ArrayList<>();
	private static final List<WorldMark> MARKS = new ArrayList<>();
	private static final List<ArcFx> ARCS = new ArrayList<>();
	private static int lastSwing;

	private PvpVisualsRenderer() {}

	public static void tick(Minecraft client) {
		if (client.player == null || client.level == null) {
			WAVES.clear();
			DEBRIS.clear();
			MARKS.clear();
			ARCS.clear();
			return;
		}

		Iterator<Shockwave> wi = WAVES.iterator();
		while (wi.hasNext()) {
			Shockwave w = wi.next();
			w.life--;
			w.radius += w.grow;
			if (w.life <= 0) wi.remove();
		}
		Iterator<Debris> di = DEBRIS.iterator();
		while (di.hasNext()) {
			Debris d = di.next();
			d.x += d.vx;
			d.y += d.vy;
			d.z += d.vz;
			d.vy -= 0.045f;
			d.life--;
			if (d.life <= 0 || d.y < d.groundY - 0.5) di.remove();
		}
		Iterator<WorldMark> mi = MARKS.iterator();
		while (mi.hasNext()) {
			WorldMark m = mi.next();
			m.life--;
			if (m.life <= 0) mi.remove();
		}
		Iterator<ArcFx> ai = ARCS.iterator();
		while (ai.hasNext()) {
			ArcFx a = ai.next();
			a.life--;
			if (a.life <= 0) ai.remove();
		}

		int swing = client.player.swingTime;
		if (client.player.swinging && swing > lastSwing) {
			boolean onTarget = client.hitResult instanceof EntityHitResult ehr
				&& ehr.getEntity() instanceof LivingEntity living
				&& living.isAlive()
				&& ehr.getEntity() != client.player;
			if (onTarget) {
				Entity target = ((EntityHitResult) client.hitResult).getEntity();
				trigger(client, detect(client.player.getMainHandItem(), (float) client.player.fallDistance), target);
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
		if (main.is(ItemTags.AXES)) return WeaponKind.AXE;
		if (main.is(ItemTags.SWORDS)) return WeaponKind.SWORD;
		return WeaponKind.SWORD;
	}

	private static void trigger(Minecraft client, WeaponKind kind, Entity target) {
		double gx = target.getX();
		double gy = target.getY();
		double gz = target.getZ();
		Vec3 hit = target.position().add(0, target.getBbHeight() * 0.55, 0);
		float[] rgb = hitRgb();

		if (IceesConfig.pvpHitMarkers) {
			spawnHitMark(hit.x, hit.y, hit.z, rgb);
		}

		switch (kind) {
			case STUNSLAM -> {
				if (!IceesConfig.pvpStunslam) return;
				int size = IceesConfig.pvpStunslamSize;
				spawnShockwave(gx, gy, gz, size + 1, 3);
				spawnDebris(gx, gy + 0.1, gz, 18 + size * 8, size, true);
			}
			case MACE -> {
				if (!IceesConfig.pvpMace) return;
				int size = IceesConfig.pvpMaceSize;
				spawnShockwave(gx, gy, gz, size, 2);
				if (IceesConfig.pvpHitParticles || IceesConfig.pvpMaceParticles) {
					spawnDebris(gx, gy + 0.05, gz, 14 + size * 6, size, true);
				}
			}
			case SPEAR -> {
				if (!IceesConfig.pvpSpear) return;
				Vec3 from = client.player.getEyePosition(1f);
				ARCS.add(ArcFx.streak(from.x, from.y, from.z, hit.x, hit.y, hit.z, IceesConfig.pvpSpearSize, 12));
				if (IceesConfig.pvpHitParticles || IceesConfig.pvpSpearParticles) {
					spawnDebris(hit.x, hit.y, hit.z, 8 + IceesConfig.pvpSpearSize * 3, IceesConfig.pvpSpearSize, false);
				}
			}
			case SWORD -> {
				if (!IceesConfig.pvpSword) return;
				ARCS.add(ArcFx.slash(hit.x, hit.y, hit.z, IceesConfig.pvpSwordSize, false, 14));
				if (IceesConfig.pvpHitParticles) {
					spawnDebris(hit.x, hit.y, hit.z, 8 + IceesConfig.pvpSwordSize * 3, IceesConfig.pvpSwordSize, false);
				}
			}
			case AXE -> {
				if (!IceesConfig.pvpAxe) return;
				ARCS.add(ArcFx.slash(hit.x, hit.y, hit.z, IceesConfig.pvpAxeSize, true, 16));
				if (IceesConfig.pvpHitParticles) {
					spawnDebris(hit.x, hit.y, hit.z, 10 + IceesConfig.pvpAxeSize * 4, IceesConfig.pvpAxeSize, false);
				}
			}
		}
	}

	private static void spawnShockwave(double x, double y, double z, int size, int rings) {
		float base = 0.35f + size * 0.22f;
		for (int i = 0; i < rings; i++) {
			Shockwave w = new Shockwave();
			w.x = x;
			w.y = y + 0.02 + i * 0.04;
			w.z = z;
			w.radius = 0.15f + i * 0.08f;
			w.grow = 0.12f + size * 0.035f - i * 0.015f;
			w.thickness = 0.08f + size * 0.025f;
			w.height = 0.06f + size * 0.02f;
			w.life = w.maxLife = 16 + size * 3 - i * 2;
			w.delay = i * 2;
			WAVES.add(w);
		}
	}

	private static void spawnDebris(double x, double y, double z, int count, int size, boolean dirt) {
		if (!IceesConfig.pvpHitParticles && !dirt) return;
		float mul = 0.55f + IceesConfig.pvpParticleSize * 0.28f;
		count = Math.max(6, Math.round(count * mul));
		float[] rgb = hitRgb();
		for (int i = 0; i < count; i++) {
			Debris d = new Debris();
			float ang = RNG.nextFloat() * Mth.TWO_PI;
			float spd = (0.08f + RNG.nextFloat() * 0.18f) * (0.7f + size * 0.15f);
			d.x = x;
			d.y = y + RNG.nextFloat() * 0.3f;
			d.z = z;
			d.vx = Mth.cos(ang) * spd;
			d.vy = 0.12f + RNG.nextFloat() * 0.22f;
			d.vz = Mth.sin(ang) * spd;
			d.groundY = y;
			d.size = (0.04f + RNG.nextFloat() * 0.07f) * mul;
			d.life = d.maxLife = 18 + RNG.nextInt(16);
			if (dirt) {
				float shade = 0.35f + RNG.nextFloat() * 0.35f;
				d.r = shade * 0.85f + 0.1f;
				d.g = shade * 0.55f + 0.05f;
				d.b = shade * 0.28f;
			} else {
				float s = 0.7f + RNG.nextFloat() * 0.3f;
				d.r = Math.min(1f, rgb[0] * s);
				d.g = Math.min(1f, rgb[1] * s);
				d.b = Math.min(1f, rgb[2] * s);
			}
			DEBRIS.add(d);
		}
	}

	private static void spawnHitMark(double x, double y, double z, float[] rgb) {
		WorldMark m = new WorldMark();
		m.x = x;
		m.y = y;
		m.z = z;
		m.style = IceesConfig.pvpHitAnim;
		m.size = 0.25f + IceesConfig.pvpHitAnimSize * 0.12f;
		m.life = m.maxLife = 10 + IceesConfig.pvpHitAnimSize * 2;
		m.r = rgb[0];
		m.g = rgb[1];
		m.b = rgb[2];
		MARKS.add(m);
	}

	/* ——— WORLD RENDER ——— */

	public static void renderWorld(WorldRenderContext context) {
		if (WAVES.isEmpty() && DEBRIS.isEmpty() && MARKS.isEmpty() && ARCS.isEmpty()) return;
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || client.level == null) return;
		MultiBufferSource buffers = context.consumers();
		PoseStack matrices = context.matrices();
		if (buffers == null || matrices == null) return;

		Camera camera = client.gameRenderer.getMainCamera();
		Vec3 cam = camera.position();
		VertexConsumer vc = buffers.getBuffer(RenderTypes.eyes(TEX));
		float[] rgb = hitRgb();

		for (Shockwave w : WAVES) {
			if (w.delay > 0) {
				w.delay--;
				continue;
			}
			float t = w.life / (float) Math.max(1, w.maxLife);
			float a = Math.min(1f, t * 1.2f) * 0.85f;
			drawShockRing(matrices, vc, cam, w, Math.min(1f, rgb[0] + 0.35f), Math.min(1f, rgb[1] + 0.35f), Math.min(1f, rgb[2] + 0.35f), a);
		}
		for (Debris d : DEBRIS) {
			float t = d.life / (float) Math.max(1, d.maxLife);
			float a = Math.min(1f, 0.4f + t);
			matrices.pushPose();
			matrices.translate(d.x - cam.x, d.y - cam.y, d.z - cam.z);
			float s = d.size;
			box(matrices, vc, -s, -s, -s, s, s, s, d.r, d.g, d.b, a);
			matrices.popPose();
		}
		for (WorldMark m : MARKS) {
			float t = m.life / (float) Math.max(1, m.maxLife);
			float a = Math.min(1f, t * 1.4f);
			drawHitMark3d(matrices, vc, cam, m, a);
		}
		for (ArcFx arc : ARCS) {
			float t = arc.life / (float) Math.max(1, arc.maxLife);
			float a = Math.min(1f, 0.35f + t);
			if (arc.streak) {
				beam(matrices, vc, cam, arc.x0, arc.y0, arc.z0, arc.x1, arc.y1, arc.z1,
					0.04f + arc.size * 0.03f, rgb[0], rgb[1], Math.min(1f, rgb[2] + 0.3f), a);
			} else {
				drawSlashArc(matrices, vc, cam, arc, rgb, a);
			}
		}
	}

	private static void drawShockRing(PoseStack m, VertexConsumer vc, Vec3 cam, Shockwave w,
		float r, float g, float b, float a) {
		int segs = 36;
		float rad = w.radius;
		float thick = w.thickness;
		float h = w.height;
		m.pushPose();
		m.translate(w.x - cam.x, w.y - cam.y, w.z - cam.z);
		for (int i = 0; i < segs; i++) {
			float a0 = i / (float) segs * Mth.TWO_PI;
			float a1 = (i + 1) / (float) segs * Mth.TWO_PI;
			float c0 = Mth.cos(a0), s0 = Mth.sin(a0);
			float c1 = Mth.cos(a1), s1 = Mth.sin(a1);
			float in = rad - thick * 0.5f;
			float out = rad + thick * 0.5f;
			// outer vertical face
			box(m, vc,
				c0 * out - thick * 0.15f, 0, s0 * out - thick * 0.15f,
				c1 * out + thick * 0.15f, h, s1 * out + thick * 0.15f,
				r, g, b, a);
			// inner fill band
			box(m, vc,
				c0 * in, h * 0.15f, s0 * in,
				c1 * out, h * 0.85f, s1 * out,
				1f, 1f, 1f, a * 0.55f);
		}
		m.popPose();
	}

	private static void drawHitMark3d(PoseStack m, VertexConsumer vc, Vec3 cam, WorldMark mark, float a) {
		m.pushPose();
		m.translate(mark.x - cam.x, mark.y - cam.y, mark.z - cam.z);
		float s = mark.size * (0.7f + (1f - mark.life / (float) mark.maxLife) * 0.5f);
		float r = mark.r, g = mark.g, b = mark.b;
		switch (mark.style) {
			case CROSS -> {
				beamLocal(m, vc, -s, -s, 0, s, s, 0, 0.04f, 1f, 1f, 1f, a);
				beamLocal(m, vc, s, -s, 0, -s, s, 0, 0.04f, r, g, b, a);
				beamLocal(m, vc, 0, -s, -s, 0, s, s, 0.03f, r, g, b, a * 0.7f);
			}
			case RING -> {
				Shockwave tmp = new Shockwave();
				tmp.radius = s;
				tmp.thickness = 0.06f + IceesConfig.pvpHitAnimSize * 0.02f;
				tmp.height = 0.05f;
				tmp.x = tmp.y = tmp.z = 0;
				for (int i = 0; i < 24; i++) {
					float a0 = i / 24f * Mth.TWO_PI;
					float a1 = (i + 1) / 24f * Mth.TWO_PI;
					float x0 = Mth.cos(a0) * s, z0 = Mth.sin(a0) * s;
					float x1 = Mth.cos(a1) * s, z1 = Mth.sin(a1) * s;
					box(m, vc, x0 - 0.03f, -0.02f, z0 - 0.03f, x1 + 0.03f, 0.06f, z1 + 0.03f, r, g, b, a);
				}
			}
			case SLASH -> {
				beamLocal(m, vc, -s, -s * 0.3f, 0.05f, s, s * 0.3f, -0.05f, 0.05f, r, g, b, a);
				beamLocal(m, vc, -s * 0.6f, s * 0.5f, 0, s * 0.2f, -s * 0.6f, 0, 0.035f, 1f, 1f, 1f, a * 0.8f);
			}
			case FLASH -> {
				box(m, vc, -s * 0.15f, -s, -s * 0.15f, s * 0.15f, s, s * 0.15f, 1f, 1f, 1f, a);
				box(m, vc, -s, -s * 0.15f, -s * 0.15f, s, s * 0.15f, s * 0.15f, r, g, b, a);
				box(m, vc, -s * 0.15f, -s * 0.15f, -s, s * 0.15f, s * 0.15f, s, r, g, b, a * 0.85f);
			}
			case SPARKS -> {
				int n = 6 + IceesConfig.pvpHitAnimSize;
				for (int i = 0; i < n; i++) {
					float ang = i / (float) n * Mth.TWO_PI;
					float len = s * (0.5f + (i % 3) * 0.2f);
					beamLocal(m, vc, 0, 0, 0, Mth.cos(ang) * len, Mth.sin(ang * 1.3f) * len * 0.4f, Mth.sin(ang) * len,
						0.03f, i % 2 == 0 ? r : 1f, i % 2 == 0 ? g : 1f, i % 2 == 0 ? b : 1f, a);
				}
			}
		}
		m.popPose();
	}

	private static void drawSlashArc(PoseStack m, VertexConsumer vc, Vec3 cam, ArcFx arc, float[] rgb, float a) {
		float progress = 1f - arc.life / (float) Math.max(1, arc.maxLife);
		float radius = 0.45f + arc.size * 0.2f;
		int steps = arc.heavy ? 12 : 9;
		float start = arc.heavy ? -60f : -42f;
		float end = arc.heavy ? 60f : 48f;
		float sweep = Mth.lerp(progress, start, end);
		m.pushPose();
		m.translate(arc.x0 - cam.x, arc.y0 - cam.y, arc.z0 - cam.z);
		double px = 0, py = 0, pz = 0;
		boolean first = true;
		for (int i = 0; i <= steps; i++) {
			float t = i / (float) steps;
			float ang = (start + (sweep - start) * t) * Mth.DEG_TO_RAD;
			double x = Mth.sin(ang) * radius;
			double y = Mth.cos(ang) * radius * (arc.heavy ? 0.55 : 0.38);
			double z = -0.15 - t * 0.12;
			if (!first) {
				beamLocal(m, vc, px, py, pz, x, y, z, 0.05f + arc.size * 0.02f, rgb[0], rgb[1], rgb[2], a * (1f - progress * 0.35f));
			}
			first = false;
			px = x; py = y; pz = z;
		}
		m.popPose();
	}

	/* ——— optional HUD: crosshair / target cue only ——— */

	public static void renderHud(GuiGraphics g) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || client.level == null) return;
		if (client.options.hideGui || client.screen != null) return;
		int cx = client.getWindow().getGuiScaledWidth() / 2;
		int cy = client.getWindow().getGuiScaledHeight() / 2;
		if (IceesConfig.pvpCrosshair) drawCrosshair(g, cx, cy);
		if (IceesConfig.pvpTargetCue) drawTargetCue(g, client, cx, cy);
	}

	private static float[] hitRgb() {
		if (IceesConfig.pvpHitColour) {
			return IceesConfig.hsvToRgb(IceesConfig.pvpHitHue, IceesConfig.pvpHitSat / 100f, IceesConfig.pvpHitVal / 100f);
		}
		if (IceesConfig.pvpTint.enabled) return IceesConfig.pvpTint.apply(1f, 0.55f, 0.7f);
		return new float[]{1f, 0.85f, 0.9f};
	}

	private static void drawCrosshair(GuiGraphics g, int cx, int cy) {
		float[] rgb = hitRgb();
		int gap = 3 + IceesConfig.pvpCrosshairSize;
		int len = 4 + IceesConfig.pvpCrosshairSize;
		int thick = Math.max(1, IceesConfig.pvpCrosshairSize / 2);
		int col = ARGB.colorFromFloat(0.95f, rgb[0], rgb[1], rgb[2]);
		g.fill(cx - gap - len, cy - thick, cx - gap, cy + thick, col);
		g.fill(cx + gap, cy - thick, cx + gap + len, cy + thick, col);
		g.fill(cx - thick, cy - gap - len, cx + thick, cy - gap, col);
		g.fill(cx - thick, cy + gap, cx + thick, cy + gap + len, col);
	}

	private static void drawTargetCue(GuiGraphics g, Minecraft client, int cx, int cy) {
		HitResult hit = client.hitResult;
		if (!(hit instanceof EntityHitResult ehr)) return;
		Entity e = ehr.getEntity();
		if (!(e instanceof LivingEntity living) || !living.isAlive() || e == client.player) return;
		float[] rgb = hitRgb();
		int ring = ARGB.colorFromFloat(0.5f, rgb[0], rgb[1], rgb[2]);
		int r = 14 + IceesConfig.pvpCrosshairSize;
		g.fill(cx - r, cy - r, cx - r + 5, cy - r + 2, ring);
		g.fill(cx - r, cy - r, cx - r + 2, cy - r + 5, ring);
		g.fill(cx + r - 5, cy - r, cx + r, cy - r + 2, ring);
		g.fill(cx + r - 2, cy - r, cx + r, cy - r + 5, ring);
		g.fill(cx - r, cy + r - 2, cx - r + 5, cy + r, ring);
		g.fill(cx - r, cy + r - 5, cx - r + 2, cy + r, ring);
		g.fill(cx + r - 5, cy + r - 2, cx + r, cy + r, ring);
		g.fill(cx + r - 2, cy + r - 5, cx + r, cy + r, ring);
		String name = living.getName().getString();
		if (name.length() > 18) name = name.substring(0, 18) + "…";
		int tw = client.font.width(name);
		g.drawString(client.font, name, cx - tw / 2, cy + r + 6, ARGB.colorFromFloat(0.85f, rgb[0], rgb[1], rgb[2]), false);
	}

	/* ——— primitives ——— */

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
		double dx = x1 - x0, dy = y1 - y0, dz = z1 - z0;
		double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
		int steps = Math.max(2, (int) (len / 0.28));
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
			float t = thickness;
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

	private enum WeaponKind { STUNSLAM, MACE, SPEAR, SWORD, AXE }

	private static final class Shockwave {
		double x, y, z;
		float radius, grow, thickness, height;
		int life, maxLife, delay;
	}

	private static final class Debris {
		double x, y, z, groundY;
		float vx, vy, vz, size, r, g, b;
		int life, maxLife;
	}

	private static final class WorldMark {
		double x, y, z;
		float size, r, g, b;
		int life, maxLife;
		HitAnimStyle style = HitAnimStyle.CROSS;
	}

	private static final class ArcFx {
		double x0, y0, z0, x1, y1, z1;
		int size, life, maxLife;
		boolean streak, heavy;

		static ArcFx streak(double x0, double y0, double z0, double x1, double y1, double z1, int size, int life) {
			ArcFx a = new ArcFx();
			a.x0 = x0; a.y0 = y0; a.z0 = z0;
			a.x1 = x1; a.y1 = y1; a.z1 = z1;
			a.size = size; a.life = a.maxLife = life;
			a.streak = true;
			return a;
		}

		static ArcFx slash(double x, double y, double z, int size, boolean heavy, int life) {
			ArcFx a = new ArcFx();
			a.x0 = x; a.y0 = y; a.z0 = z;
			a.size = size; a.life = a.maxLife = life;
			a.heavy = heavy;
			return a;
		}
	}
}
