package com.icees.visuals.cosmetics;

import com.icees.visuals.config.IceesConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
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

/** Soft glowing trail left behind while moving — cosmetics. */
public final class MovementTrailRenderer {
	private static final Identifier TEX = Identifier.fromNamespaceAndPath("icees-visuals", "textures/cosmetic/aura.png");
	private static final List<Point> POINTS = new ArrayList<>();
	private static double lastX, lastY, lastZ;
	private static boolean hasLast;

	private MovementTrailRenderer() {}

	public static void tick(Minecraft client) {
		if (!IceesConfig.trail || client.player == null || client.level == null) {
			POINTS.clear();
			hasLast = false;
			return;
		}
		LocalPlayer player = client.player;
		double x = player.getX();
		double y = player.getY() + 0.15;
		double z = player.getZ();
		double speed = player.getDeltaMovement().horizontalDistance();

		if (hasLast) {
			double dx = x - lastX, dy = y - lastY, dz = z - lastZ;
			double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
			if (dist > 0.08 || speed > 0.02) {
				int steps = Math.max(1, (int) (dist / 0.22));
				for (int i = 1; i <= steps; i++) {
					float t = i / (float) steps;
					POINTS.add(new Point(
						lastX + dx * t,
						lastY + dy * t + 0.05,
						lastZ + dz * t,
						28 + (int) (speed * 40)
					));
				}
			}
		}
		lastX = x; lastY = y; lastZ = z;
		hasLast = true;

		while (POINTS.size() > 64) POINTS.remove(0);
		Iterator<Point> it = POINTS.iterator();
		while (it.hasNext()) {
			Point p = it.next();
			p.life--;
			if (p.life <= 0) it.remove();
		}
	}

	public static void render(WorldRenderContext context) {
		if (!IceesConfig.trail || POINTS.isEmpty()) return;
		Minecraft client = Minecraft.getInstance();
		if (client.player == null) return;
		MultiBufferSource consumers = context.consumers();
		PoseStack matrices = context.matrices();
		if (consumers == null || matrices == null) return;

		Camera camera = client.gameRenderer.getMainCamera();
		Vec3 cam = camera.position();
		float bright = IceesConfig.brightnessFactor();
		float[] col = IceesConfig.color(IceesConfig.trailTint, 0.55f, 0.85f, 1.0f);
		VertexConsumer vc = consumers.getBuffer(RenderTypes.eyes(TEX));

		for (Point p : POINTS) {
			float fade = p.life / (float) p.maxLife;
			float size = (0.12f + 0.1f * fade) * IceesConfig.sizeFactor(IceesConfig.auraSize);
			float a = fade * 0.7f * bright;
			if (a < 0.03f) continue;

			matrices.pushPose();
			matrices.translate(p.x - cam.x, p.y - cam.y, p.z - cam.z);
			matrices.mulPose(camera.rotation());
			matrices.mulPose(Axis.ZP.rotationDegrees(p.life * 3f));
			quad(matrices, vc, -size, -size, size, size, col[0], col[1], col[2], a);
			quad(matrices, vc, -size * 1.4f, -size * 1.4f, size * 1.4f, size * 1.4f, col[0], col[1], col[2], a * 0.25f);
			matrices.popPose();
		}
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

	private static final class Point {
		double x, y, z;
		int life, maxLife;
		Point(double x, double y, double z, int life) {
			this.x = x; this.y = y; this.z = z;
			this.life = life; this.maxLife = life;
		}
	}
}
