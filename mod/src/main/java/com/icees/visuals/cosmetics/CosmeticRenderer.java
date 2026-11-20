package com.icees.visuals.cosmetics;

import com.icees.visuals.IceesVisualsClient;
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

/**
 * Third-person only glow cosmetics. Cape hangs from the shoulders down the back.
 */
public final class CosmeticRenderer {
	private static final Identifier TEX_WING = Identifier.fromNamespaceAndPath("icees-visuals", "textures/cosmetic/wing.png");
	private static final Identifier TEX_WING_SOFT = Identifier.fromNamespaceAndPath("icees-visuals", "textures/cosmetic/wing_soft.png");
	private static final Identifier TEX_HALO = Identifier.fromNamespaceAndPath("icees-visuals", "textures/cosmetic/halo.png");
	private static final Identifier TEX_CAPE = Identifier.fromNamespaceAndPath("icees-visuals", "textures/cosmetic/cape.png");
	private static final Identifier TEX_AURA = Identifier.fromNamespaceAndPath("icees-visuals", "textures/cosmetic/aura.png");
	private static final Identifier TEX_KNIFE = Identifier.fromNamespaceAndPath("icees-visuals", "textures/cosmetic/knife.png");

	private static boolean disabledForSession;

	private CosmeticRenderer() {}

	public static void render(WorldRenderContext context) {
		if (disabledForSession) return;
		Minecraft client = Minecraft.getInstance();
		LocalPlayer player = client.player;
		if (player == null || client.level == null) return;

		// Never draw body cosmetics in first person (was sticking to camera / head)
		if (client.options.getCameraType().isFirstPerson()) return;

		try {
			MultiBufferSource consumers = context.consumers();
			if (consumers == null) return;

			float partial = client.getDeltaTracker().getGameTimeDeltaPartialTick(false);
			float time = player.tickCount + partial;

			Camera camera = client.gameRenderer.getMainCamera();
			Vec3 cam = camera.position();

			double px = Mth.lerp(partial, player.xo, player.getX()) - cam.x;
			double py = Mth.lerp(partial, player.yo, player.getY()) - cam.y;
			double pz = Mth.lerp(partial, player.zo, player.getZ()) - cam.z;
			float bodyYaw = Mth.rotLerp(partial, player.yBodyRotO, player.yBodyRot);

			PoseStack matrices = context.matrices();
			if (matrices == null) {
				// Without the world pose stack, camera-relative verts are unreliable — skip
				return;
			}

			float bright = IceesConfig.brightnessFactor();
			float thick = IceesConfig.thicknessFactor();

			matrices.pushPose();
			matrices.translate(px, py, pz);
			matrices.mulPose(Axis.YP.rotationDegrees(-bodyYaw));

			if (IceesConfig.cape) drawCape(matrices, consumers, time, bright, thick);
			if (IceesConfig.wings) drawWings(matrices, consumers, time, bright, thick);
			if (IceesConfig.halo) drawHalo(matrices, consumers, time, bright, thick);
			if (IceesConfig.aura) drawAura(matrices, consumers, time, bright, thick);
			if (IceesConfig.jumpCircles && !player.onGround()) drawJumpCircles(matrices, consumers, time, bright, thick);
			if (IceesConfig.knife) drawThirdPersonKnife(matrices, consumers, time, bright, thick);

			matrices.popPose();
		} catch (Throwable t) {
			disabledForSession = true;
			IceesVisualsClient.LOGGER.error("Cosmetics crashed — disabled for this session", t);
		}
	}

	private static void drawWings(PoseStack matrices, MultiBufferSource buffers, float time, float bright, float thick) {
		WingStyle style = IceesConfig.wingStyle;
		float beat = Mth.sin(time * 0.22f) * 0.4f;
		VertexConsumer soft = buffers.getBuffer(RenderTypes.eyes(TEX_WING_SOFT));
		VertexConsumer hard = buffers.getBuffer(RenderTypes.eyes(TEX_WING));
		float span = 1.35f * thick;
		float tall = 1.15f * thick;

		for (int side : new int[]{-1, 1}) {
			matrices.pushPose();
			matrices.translate(side * 0.18f, 1.05f, 0.12f);
			matrices.mulPose(Axis.ZP.rotationDegrees(side * (-38f + beat * 24f)));
			matrices.mulPose(Axis.YP.rotationDegrees(side * 22f));
			quad(matrices, soft, 0f, 0.1f, -0.05f, side * span, tall, -0.3f, style.r, style.g, style.b, 0.75f * bright);
			quad(matrices, hard, 0f, 0.0f, 0f, side * span * 0.9f, tall * 0.85f, -0.12f, style.r, style.g, style.b, Math.min(1f, 1.05f * bright));
			matrices.popPose();
		}
	}

	private static void drawHalo(PoseStack matrices, MultiBufferSource buffers, float time, float bright, float thick) {
		HaloStyle style = IceesConfig.haloStyle;
		float pulse = style == HaloStyle.PULSE ? 1f + Mth.sin(time * 0.28f) * 0.12f : 1f;
		matrices.pushPose();
		// Above the head
		matrices.translate(0, 2.05f + 0.1f * thick, 0);
		matrices.mulPose(Axis.XP.rotationDegrees(90));
		if (style == HaloStyle.ORBIT || style == HaloStyle.NOVA) {
			matrices.mulPose(Axis.ZP.rotationDegrees(time * 2.5f));
		}
		float s = 0.42f * pulse * thick;
		VertexConsumer vc = buffers.getBuffer(RenderTypes.eyes(TEX_HALO));
		quad(matrices, vc, -s, -s, 0, s, s, 0, style.r, style.g, style.b, Math.min(1f, 1.1f * bright));
		float s2 = s * 1.55f;
		quad(matrices, vc, -s2, -s2, 0.01f, s2, s2, 0.01f, style.r, style.g, style.b, 0.55f * bright);
		matrices.popPose();
	}

	/**
	 * Cape on the BACK: attach at upper back / shoulders, hang downward behind the body.
	 * Player local +Z is forward in our yaw-aligned space after -bodyYaw, so back is -Z.
	 */
	private static void drawCape(PoseStack matrices, MultiBufferSource buffers, float time, float bright, float thick) {
		CapeStyle style = IceesConfig.capeStyle;
		float sway = Mth.sin(time * 0.16f) * 0.22f;
		matrices.pushPose();
		// Shoulder / upper-back attachment (not head!)
		matrices.translate(0, 1.35f, -0.22f);
		// Tip cape slightly away from back + sway
		matrices.mulPose(Axis.XP.rotationDegrees(12f + sway * 18f));

		VertexConsumer vc = buffers.getBuffer(RenderTypes.eyes(TEX_CAPE));
		float w = 0.38f * thick;
		float len = -1.25f * thick; // hang DOWN (-Y)
		PoseStack.Pose pose = matrices.last();
		Matrix4f m = pose.pose();
		int light = LightTexture.FULL_BRIGHT;
		int overlay = OverlayTexture.NO_OVERLAY;
		float aTop = Math.min(1f, 0.95f * bright);
		float aBot = Math.min(1f, 0.7f * bright);

		// Top edge at shoulders, bottom edge lower + slightly further back (-Z)
		vc.addVertex(m, -w, 0, 0).setColor(style.r1, style.g1, style.b1, aTop).setUv(0f, 0f).setOverlay(overlay).setLight(light).setNormal(pose, 0, 0, -1);
		vc.addVertex(m, w, 0, 0).setColor(style.r1, style.g1, style.b1, aTop).setUv(1f, 0f).setOverlay(overlay).setLight(light).setNormal(pose, 0, 0, -1);
		vc.addVertex(m, w + sway * 0.2f, len, -0.25f).setColor(style.r2, style.g2, style.b2, aBot).setUv(1f, 1f).setOverlay(overlay).setLight(light).setNormal(pose, 0, 0, -1);
		vc.addVertex(m, -w + sway * 0.2f, len, -0.25f).setColor(style.r2, style.g2, style.b2, aBot).setUv(0f, 1f).setOverlay(overlay).setLight(light).setNormal(pose, 0, 0, -1);

		// Soft glow layer behind cape
		VertexConsumer glow = buffers.getBuffer(RenderTypes.eyes(TEX_AURA));
		float gw = w * 1.15f;
		float glen = len * 1.05f;
		glow.addVertex(m, -gw, 0.05f, 0.02f).setColor(style.r1, style.g1, style.b1, 0.35f * bright).setUv(0f, 0f).setOverlay(overlay).setLight(light).setNormal(pose, 0, 0, -1);
		glow.addVertex(m, gw, 0.05f, 0.02f).setColor(style.r1, style.g1, style.b1, 0.35f * bright).setUv(1f, 0f).setOverlay(overlay).setLight(light).setNormal(pose, 0, 0, -1);
		glow.addVertex(m, gw, glen, -0.3f).setColor(style.r2, style.g2, style.b2, 0.2f * bright).setUv(1f, 1f).setOverlay(overlay).setLight(light).setNormal(pose, 0, 0, -1);
		glow.addVertex(m, -gw, glen, -0.3f).setColor(style.r2, style.g2, style.b2, 0.2f * bright).setUv(0f, 1f).setOverlay(overlay).setLight(light).setNormal(pose, 0, 0, -1);

		matrices.popPose();
	}

	private static void drawAura(PoseStack matrices, MultiBufferSource buffers, float time, float bright, float thick) {
		AuraStyle style = IceesConfig.auraStyle;
		VertexConsumer vc = buffers.getBuffer(RenderTypes.eyes(TEX_AURA));
		float aMul = Math.min(1f, 0.85f * bright);

		switch (style) {
			case PULSE -> {
				float pulse = 0.55f + 0.45f * (0.5f + 0.5f * Mth.sin(time * 0.25f));
				float s = 1.1f * thick * pulse;
				matrices.pushPose();
				matrices.translate(0, 1.0f, 0);
				quad(matrices, vc, -s, -s, 0, s, s, 0, style.r, style.g, style.b, aMul * 0.7f);
				float s2 = s * 1.4f;
				quad(matrices, vc, -s2, -s2, 0.02f, s2, s2, 0.02f, style.r, style.g, style.b, aMul * 0.35f);
				matrices.popPose();
			}
			case RING -> {
				matrices.pushPose();
				matrices.translate(0, 0.08f, 0);
				matrices.mulPose(Axis.XP.rotationDegrees(90));
				float radius = 0.85f * thick;
				quad(matrices, vc, -radius, -radius, 0, radius, radius, 0, style.r, style.g, style.b, aMul);
				float r2 = radius * 1.35f;
				quad(matrices, vc, -r2, -r2, 0.01f, r2, r2, 0.01f, style.r, style.g, style.b, aMul * 0.4f);
				matrices.popPose();
			}
			case TWIN -> {
				for (int side : new int[]{-1, 1}) {
					matrices.pushPose();
					matrices.translate(side * 0.85f * thick, 1.05f, 0);
					float s = 0.55f * thick;
					quad(matrices, vc, -s, -s, 0, s, s, 0, style.r, style.g, style.b, aMul);
					matrices.popPose();
				}
			}
			case HEARTBEAT -> {
				float beat = Mth.sin(time * 0.55f);
				float pulse = beat > 0 ? 0.7f + beat * 0.5f : 0.55f + beat * 0.15f;
				float s = 0.95f * thick * pulse;
				matrices.pushPose();
				matrices.translate(0, 1.0f, 0);
				quad(matrices, vc, -s, -s, 0, s, s, 0, style.r, style.g, style.b, aMul * 0.8f);
				matrices.popPose();
			}
			case FLAME -> {
				int n = style.count;
				for (int i = 0; i < n; i++) {
					float a = time * style.spin + i * ((float) Math.PI * 2f / n);
					float radius = 0.45f * thick;
					float x = Mth.cos(a) * radius;
					float z = Mth.sin(a) * radius;
					float y = 0.4f + ((time * 0.08f + i * 0.2f) % 1.4f);
					float fade = 1f - (y - 0.4f) / 1.4f;
					matrices.pushPose();
					matrices.translate(x, y, z);
					float s = (0.22f + 0.1f * fade) * thick;
					quad(matrices, vc, -s, -s, 0, s, s, 0, style.r, style.g, style.b, aMul * fade);
					matrices.popPose();
				}
			}
			case SPIRAL -> {
				int n = style.count;
				for (int i = 0; i < n; i++) {
					float t = (time * 0.12f + i / (float) n) % 1f;
					float a = t * ((float) Math.PI * 4f) + time * style.spin;
					float radius = 0.35f * thick + t * 0.55f * thick;
					float x = Mth.cos(a) * radius;
					float z = Mth.sin(a) * radius;
					float y = 0.2f + t * 1.8f;
					matrices.pushPose();
					matrices.translate(x, y, z);
					float s = 0.28f * thick * (1f - t * 0.4f);
					quad(matrices, vc, -s, -s, 0, s, s, 0, style.r, style.g, style.b, aMul * (1f - t * 0.5f));
					matrices.popPose();
				}
			}
			default -> {
				// ORBIT, FROST, BLOOM, SPARKS — orbiting orbs
				int n = Math.max(2, style.count);
				for (int i = 0; i < n; i++) {
					float a = time * (0.08f + style.spin) + i * ((float) Math.PI * 2f / n);
					float radius = (style == AuraStyle.SPARKS ? 0.95f : 0.75f) * thick;
					float x = Mth.cos(a) * radius;
					float z = Mth.sin(a) * radius;
					float y = 0.85f + Mth.sin(time * 0.15f + i) * 0.18f;
					if (style == AuraStyle.BLOOM) y += Mth.sin(time * 0.3f + i) * 0.1f;
					matrices.pushPose();
					matrices.translate(x, y, z);
					float s = (style == AuraStyle.SPARKS ? 0.18f : 0.32f) * thick;
					s += 0.06f * Mth.sin(time * 0.25f + i);
					quad(matrices, vc, -s, -s, 0, s, s, 0, style.r, style.g, style.b, aMul);
					matrices.popPose();
				}
			}
		}
	}

	private static void drawJumpCircles(PoseStack matrices, MultiBufferSource buffers, float time, float bright, float thick) {
		VertexConsumer vc = buffers.getBuffer(RenderTypes.eyes(TEX_HALO));
		float phase = (time % 28f) / 28f;
		for (int ring = 0; ring < 2; ring++) {
			float p = (phase + ring * 0.5f) % 1f;
			float radius = (0.35f + p * 1.1f) * thick;
			float alpha = (1f - p) * 0.75f * bright;
			matrices.pushPose();
			matrices.translate(0, 0.05f, 0);
			matrices.mulPose(Axis.XP.rotationDegrees(90));
			quad(matrices, vc, -radius, -radius, 0, radius, radius, 0, 1f, 0.75f, 0.88f, alpha);
			matrices.popPose();
		}
	}

	private static void drawThirdPersonKnife(PoseStack matrices, MultiBufferSource buffers, float time, float bright, float thick) {
		KnifeStyle style = IceesConfig.knifeStyle;
		matrices.pushPose();
		// Hip / hand side — not head
		matrices.translate(0.42f, 0.85f, -0.05f);
		matrices.mulPose(Axis.YP.rotationDegrees(knifeSpin(style, time)));
		matrices.mulPose(Axis.ZP.rotationDegrees(20f + Mth.sin(time * 0.2f) * 10f));
		float hs = 0.55f * thick;
		VertexConsumer vc = buffers.getBuffer(RenderTypes.eyes(TEX_KNIFE));
		quad(matrices, vc, -0.1f * thick, -0.05f, 0, 0.1f * thick, hs, 0, 1f, 0.92f, 0.96f, Math.min(1f, bright));
		VertexConsumer glow = buffers.getBuffer(RenderTypes.eyes(TEX_AURA));
		quad(matrices, glow, -0.22f * thick, -0.08f, -0.03f, 0.22f * thick, hs * 1.1f, -0.03f, 1f, 0.7f, 0.85f, 0.55f * bright);
		matrices.popPose();
	}

	private static float knifeSpin(KnifeStyle style, float time) {
		return switch (style) {
			case BUTTERFLY, KARAMBIT, CURVED -> time * 9f;
			case BAYONET, LONG_BLADE -> Mth.sin(time * 0.2f) * 28f;
			case STILETTO, SHORT_BLADE -> Mth.sin(time * 0.4f) * 45f;
			case URSUS -> time * 6f + Mth.sin(time * 0.5f) * 20f;
		};
	}

	private static void quad(
		PoseStack matrices, VertexConsumer vc,
		float x0, float y0, float z0,
		float x1, float y1, float z1,
		float r, float g, float b, float a
	) {
		PoseStack.Pose pose = matrices.last();
		Matrix4f m = pose.pose();
		int light = LightTexture.FULL_BRIGHT;
		int overlay = OverlayTexture.NO_OVERLAY;
		float aa = Mth.clamp(a, 0f, 1f);
		vc.addVertex(m, x0, y0, z0).setColor(r, g, b, aa).setUv(0f, 1f).setOverlay(overlay).setLight(light).setNormal(pose, 0, 0, 1);
		vc.addVertex(m, x1, y0, z1).setColor(r, g, b, aa).setUv(1f, 1f).setOverlay(overlay).setLight(light).setNormal(pose, 0, 0, 1);
		vc.addVertex(m, x1, y1, z1).setColor(r, g, b, aa).setUv(1f, 0f).setOverlay(overlay).setLight(light).setNormal(pose, 0, 0, 1);
		vc.addVertex(m, x0, y1, z0).setColor(r, g, b, aa).setUv(0f, 0f).setOverlay(overlay).setLight(light).setNormal(pose, 0, 0, 1);
	}
}
