package com.icees.visuals.cosmetics;

import com.icees.visuals.IceesVisualsClient;
import com.icees.visuals.config.IceesConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
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
 * Soft emissive cosmetics — wings, halo, cape, aura, jump rings, knife/sword.
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

		try {
			MultiBufferSource consumers = context.consumers();
			if (consumers == null) return;

			float partial = client.getDeltaTracker().getGameTimeDeltaPartialTick(false);
			float time = player.tickCount + partial;
			Vec3 cam = context.worldState().cameraRenderState.pos;

			double px = Mth.lerp(partial, player.xo, player.getX()) - cam.x;
			double py = Mth.lerp(partial, player.yo, player.getY()) - cam.y;
			double pz = Mth.lerp(partial, player.zo, player.getZ()) - cam.z;
			float bodyYaw = Mth.rotLerp(partial, player.yBodyRotO, player.yBodyRot);

			PoseStack matrices = context.matrices();
			if (matrices == null) {
				matrices = new PoseStack();
			}

			boolean firstPerson = client.options.getCameraType().isFirstPerson();

			matrices.pushPose();
			matrices.translate(px, py, pz);
			matrices.mulPose(Axis.YP.rotationDegrees(-bodyYaw));

			if (!firstPerson) {
				if (IceesConfig.cape) drawCape(matrices, consumers, time);
				if (IceesConfig.wings) drawWings(matrices, consumers, time);
				if (IceesConfig.halo) drawHalo(matrices, consumers, time);
				if (IceesConfig.aura) drawAura(matrices, consumers, time);
				if (IceesConfig.jumpCircles && !player.onGround()) drawJumpCircles(matrices, consumers, time);
				if (IceesConfig.knife) drawThirdPersonKnife(matrices, consumers, time);
			}
			matrices.popPose();

			if (firstPerson && IceesConfig.knife) {
				matrices.pushPose();
				drawFirstPersonKnife(matrices, consumers, time);
				matrices.popPose();
			}
		} catch (Throwable t) {
			disabledForSession = true;
			IceesVisualsClient.LOGGER.error("Cosmetics crashed — disabled for this session", t);
		}
	}

	private static void drawWings(PoseStack matrices, MultiBufferSource buffers, float time) {
		WingStyle style = IceesConfig.wingStyle;
		float beat = Mth.sin(time * 0.22f) * 0.4f;
		VertexConsumer soft = buffers.getBuffer(RenderTypes.entityTranslucentEmissive(TEX_WING_SOFT));
		VertexConsumer hard = buffers.getBuffer(RenderTypes.entityTranslucentEmissive(TEX_WING));

		for (int side : new int[]{-1, 1}) {
			matrices.pushPose();
			matrices.translate(side * 0.12f, 1.15f, 0.08f);
			matrices.mulPose(Axis.ZP.rotationDegrees(side * (-32f + beat * 22f)));
			matrices.mulPose(Axis.YP.rotationDegrees(side * 18f));
			quad(matrices, soft, 0f, 0.15f, -0.05f, side * 1.35f, 1.15f, -0.25f, style.r, style.g, style.b, 0.55f);
			quad(matrices, hard, 0f, 0.05f, 0f, side * 1.15f, 0.95f, -0.12f, style.r, style.g, style.b, 0.85f);
			matrices.popPose();
		}
	}

	private static void drawHalo(PoseStack matrices, MultiBufferSource buffers, float time) {
		HaloStyle style = IceesConfig.haloStyle;
		float pulse = style == HaloStyle.PULSE ? 1f + Mth.sin(time * 0.28f) * 0.12f : 1f;
		matrices.pushPose();
		matrices.translate(0, 2.15f, 0);
		matrices.mulPose(Axis.XP.rotationDegrees(78));
		if (style == HaloStyle.ORBIT || style == HaloStyle.NOVA) {
			matrices.mulPose(Axis.ZP.rotationDegrees(time * 2.5f));
		}
		float s = 0.55f * pulse;
		VertexConsumer vc = buffers.getBuffer(RenderTypes.entityTranslucentEmissive(TEX_HALO));
		quad(matrices, vc, -s, -s, 0, s, s, 0, style.r, style.g, style.b, 0.9f);
		float s2 = s * 1.45f;
		quad(matrices, vc, -s2, -s2, -0.02f, s2, s2, -0.02f, style.r, style.g, style.b, 0.35f);
		matrices.popPose();
	}

	private static void drawCape(PoseStack matrices, MultiBufferSource buffers, float time) {
		CapeStyle style = IceesConfig.capeStyle;
		float sway = Mth.sin(time * 0.16f) * 0.3f;
		matrices.pushPose();
		matrices.translate(0, 1.4f, 0.2f);
		matrices.mulPose(Axis.XP.rotationDegrees(10f + sway * 22f));
		VertexConsumer vc = buffers.getBuffer(RenderTypes.entityTranslucentEmissive(TEX_CAPE));
		float w = 0.42f;
		float h = -1.35f;
		PoseStack.Pose pose = matrices.last();
		Matrix4f m = pose.pose();
		int light = LightTexture.FULL_BRIGHT;
		int overlay = OverlayTexture.NO_OVERLAY;
		vc.addVertex(m, -w, 0, 0).setColor(style.r1, style.g1, style.b1, 0.85f).setUv(0f, 0f).setOverlay(overlay).setLight(light).setNormal(pose, 0, 0, 1);
		vc.addVertex(m, w, 0, 0).setColor(style.r1, style.g1, style.b1, 0.85f).setUv(1f, 0f).setOverlay(overlay).setLight(light).setNormal(pose, 0, 0, 1);
		vc.addVertex(m, w + sway * 0.25f, h, 0.15f).setColor(style.r2, style.g2, style.b2, 0.55f).setUv(1f, 1f).setOverlay(overlay).setLight(light).setNormal(pose, 0, 0, 1);
		vc.addVertex(m, -w + sway * 0.25f, h, 0.15f).setColor(style.r2, style.g2, style.b2, 0.55f).setUv(0f, 1f).setOverlay(overlay).setLight(light).setNormal(pose, 0, 0, 1);
		matrices.popPose();
	}

	private static void drawAura(PoseStack matrices, MultiBufferSource buffers, float time) {
		VertexConsumer vc = buffers.getBuffer(RenderTypes.entityTranslucentEmissive(TEX_AURA));
		for (int i = 0; i < 3; i++) {
			float a = time * 0.08f + i * ((float) Math.PI * 2f / 3f);
			float radius = 0.75f;
			float x = Mth.cos(a) * radius;
			float z = Mth.sin(a) * radius;
			float y = 0.9f + Mth.sin(time * 0.15f + i) * 0.15f;
			matrices.pushPose();
			matrices.translate(x, y, z);
			float s = 0.35f + 0.08f * Mth.sin(time * 0.2f + i);
			quad(matrices, vc, -s, -s, 0, s, s, 0, 1f, 0.7f, 0.85f, 0.45f);
			matrices.popPose();
		}
	}

	private static void drawJumpCircles(PoseStack matrices, MultiBufferSource buffers, float time) {
		VertexConsumer vc = buffers.getBuffer(RenderTypes.entityTranslucentEmissive(TEX_HALO));
		float phase = (time % 28f) / 28f;
		for (int ring = 0; ring < 2; ring++) {
			float p = (phase + ring * 0.5f) % 1f;
			float radius = 0.35f + p * 1.1f;
			float alpha = (1f - p) * 0.55f;
			matrices.pushPose();
			matrices.translate(0, 0.05f, 0);
			matrices.mulPose(Axis.XP.rotationDegrees(90));
			quad(matrices, vc, -radius, -radius, 0, radius, radius, 0, 1f, 0.75f, 0.88f, alpha);
			matrices.popPose();
		}
	}

	private static void drawThirdPersonKnife(PoseStack matrices, MultiBufferSource buffers, float time) {
		KnifeStyle style = IceesConfig.knifeStyle;
		matrices.pushPose();
		matrices.translate(0.45f, 0.95f, -0.15f);
		matrices.mulPose(Axis.YP.rotationDegrees(knifeSpin(style, time)));
		matrices.mulPose(Axis.ZP.rotationDegrees(25f + Mth.sin(time * 0.2f) * 10f));
		VertexConsumer vc = buffers.getBuffer(RenderTypes.entityTranslucentEmissive(TEX_KNIFE));
		quad(matrices, vc, -0.08f, -0.05f, 0, 0.08f, 0.55f, 0, 1f, 0.9f, 0.95f, 0.95f);
		VertexConsumer glow = buffers.getBuffer(RenderTypes.entityTranslucentEmissive(TEX_AURA));
		quad(matrices, glow, -0.18f, -0.05f, -0.02f, 0.18f, 0.6f, -0.02f, 1f, 0.7f, 0.85f, 0.35f);
		matrices.popPose();
	}

	private static void drawFirstPersonKnife(PoseStack matrices, MultiBufferSource buffers, float time) {
		KnifeStyle style = IceesConfig.knifeStyle;
		matrices.pushPose();
		matrices.translate(0.55f, -0.35f, 0.75f);
		matrices.mulPose(Axis.YP.rotationDegrees(100f));
		matrices.mulPose(Axis.XP.rotationDegrees(-15f + Mth.sin(time * 0.25f) * 8f));
		matrices.mulPose(Axis.ZP.rotationDegrees(knifeSpin(style, time)));
		VertexConsumer vc = buffers.getBuffer(RenderTypes.entityTranslucentEmissive(TEX_KNIFE));
		quad(matrices, vc, -0.06f, -0.08f, 0, 0.06f, 0.7f, 0, 1f, 0.92f, 0.96f, 1f);
		VertexConsumer glow = buffers.getBuffer(RenderTypes.entityTranslucentEmissive(TEX_AURA));
		quad(matrices, glow, -0.2f, -0.1f, -0.03f, 0.2f, 0.75f, -0.03f, 1f, 0.65f, 0.82f, 0.4f);
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
		vc.addVertex(m, x0, y0, z0).setColor(r, g, b, a).setUv(0f, 1f).setOverlay(overlay).setLight(light).setNormal(pose, 0, 0, 1);
		vc.addVertex(m, x1, y0, z1).setColor(r, g, b, a).setUv(1f, 1f).setOverlay(overlay).setLight(light).setNormal(pose, 0, 0, 1);
		vc.addVertex(m, x1, y1, z1).setColor(r, g, b, a).setUv(1f, 0f).setOverlay(overlay).setLight(light).setNormal(pose, 0, 0, 1);
		vc.addVertex(m, x0, y1, z0).setColor(r, g, b, a).setUv(0f, 0f).setOverlay(overlay).setLight(light).setNormal(pose, 0, 0, 1);
	}
}
