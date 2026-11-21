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
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
/**
 * Third-person cosmetics with distinct 3D models per style, double-sided so visible from every angle.
 */
public final class CosmeticRenderer {
	private static final Identifier TEX_WING = Identifier.fromNamespaceAndPath("icees-visuals", "textures/cosmetic/wing.png");
	private static final Identifier TEX_WING_SOFT = Identifier.fromNamespaceAndPath("icees-visuals", "textures/cosmetic/wing_soft.png");
	private static final Identifier TEX_HALO = Identifier.fromNamespaceAndPath("icees-visuals", "textures/cosmetic/halo.png");
	private static final Identifier TEX_CAPE = Identifier.fromNamespaceAndPath("icees-visuals", "textures/cosmetic/cape.png");
	private static final Identifier TEX_AURA = Identifier.fromNamespaceAndPath("icees-visuals", "textures/cosmetic/aura.png");

	private static boolean disabledForSession;
	private static float CR = 1f, CG = 1f, CB = 1f;
	private static float C2R = 1f, C2G = 1f, C2B = 1f;

	private CosmeticRenderer() {}

	private static float gradPhase(float time) {
		return 0.5f + 0.5f * Mth.sin(time * 0.12f);
	}

	private static float[] tintWings(float r, float g, float b, float time) {
		return IceesConfig.color(IceesConfig.wingTint, r, g, b, gradPhase(time));
	}

	private static float[] tintHalo(float r, float g, float b, float time) {
		return IceesConfig.color(IceesConfig.haloTint, r, g, b, gradPhase(time));
	}

	private static float[] tintCape(float r, float g, float b, float time) {
		return IceesConfig.color(IceesConfig.capeTint, r, g, b, gradPhase(time));
	}

	private static float[] tintAura(float r, float g, float b, float time) {
		return IceesConfig.color(IceesConfig.auraTint, r, g, b, gradPhase(time));
	}



	public static void render(WorldRenderContext context) {
		if (disabledForSession) return;
		Minecraft client = Minecraft.getInstance();
		LocalPlayer player = client.player;
		if (player == null || client.level == null) return;
		if (client.options.getCameraType().isFirstPerson()) return;

		try {
			MultiBufferSource consumers = context.consumers();
			if (consumers == null) return;
			PoseStack matrices = context.matrices();
			if (matrices == null) return;

			float partial = client.getDeltaTracker().getGameTimeDeltaPartialTick(false);
			float time = player.tickCount + partial;
			Camera camera = client.gameRenderer.getMainCamera();
			Vec3 cam = camera.position();

			double px = Mth.lerp(partial, player.xo, player.getX()) - cam.x;
			double py = Mth.lerp(partial, player.yo, player.getY()) - cam.y;
			double pz = Mth.lerp(partial, player.zo, player.getZ()) - cam.z;
			float bodyYaw = Mth.rotLerp(partial, player.yBodyRotO, player.yBodyRot);
			float headYaw = Mth.rotLerp(partial, player.yHeadRotO, player.yHeadRot);
			float headPitch = Mth.lerp(partial, player.xRotO, player.getXRot());
			float headYawRel = Mth.wrapDegrees(headYaw - bodyYaw);

			float camRelYaw = camera.yRot() - bodyYaw;
			float camPitch = camera.xRot();

			matrices.pushPose();
			matrices.translate(px, py, pz);
			// Same body frame as LivingEntityRenderer.setupRotations / HumanoidModel
			matrices.mulPose(Axis.YP.rotationDegrees(180f - bodyYaw));
			drawAll(matrices, consumers, player, time, camRelYaw, camPitch, headYawRel, headPitch, false);
			matrices.popPose();
		} catch (Throwable t) {
			disabledForSession = true;
			IceesVisualsClient.LOGGER.error("Cosmetics crashed — disabled for this session", t);
		}
	}

	/**
	 * Real 3D cosmetics inside the Orbit skin preview.
	 * Uses the same body/head angles as the rendered GUI entity (not world look).
	 */
	public static void renderInGuiPreview(PoseStack matrices, MultiBufferSource buffers,
		LivingEntityRenderState living) {
		if (disabledForSession || buffers == null || living == null) return;
		Minecraft client = Minecraft.getInstance();
		LocalPlayer player = client.player;
		if (player == null) return;
		float partial = client.getDeltaTracker().getGameTimeDeltaPartialTick(false);
		float time = player.tickCount + partial;
		// living.yRot / xRot are already the head pose HumanoidModel uses
		float headYawRel = living.yRot;
		float headPitch = living.xRot;
		matrices.pushPose();
		// Match LivingEntityRenderer.setupRotations so cape/hat sit on the entity
		matrices.mulPose(Axis.YP.rotationDegrees(180f - living.bodyRot));
		drawAll(matrices, buffers, player, time, 0f, 0f, headYawRel, headPitch, true);
		matrices.popPose();
	}

	private static void drawAll(PoseStack matrices, MultiBufferSource consumers, LocalPlayer player,
		float time, float camRelYaw, float camPitch, float headYawRel, float headPitch, boolean preview) {
		float bright = IceesConfig.brightnessFactor() * (preview ? 1.15f : 1f);
		// HumanoidModel crouch: head.y += 4.2, body.y += 3.2 (model units / 16)
		boolean crouch = !preview && player.isCrouching();
		float headDrop = crouch ? 4.2f / 16f : 0f;
		float bodyDrop = crouch ? 3.2f / 16f : 0f;
		if (IceesConfig.cape) drawCape(matrices, consumers, time, bright, IceesConfig.sizeFactor(IceesConfig.capeSize), bodyDrop);
		if (IceesConfig.wings) drawWings(matrices, consumers, time, bright, IceesConfig.sizeFactor(IceesConfig.wingSize), bodyDrop);
		if (IceesConfig.halo) drawHalo(matrices, consumers, time, bright, IceesConfig.sizeFactor(IceesConfig.haloSize), headDrop);
		if (IceesConfig.hat) drawHat(matrices, consumers, time, bright, IceesConfig.sizeFactor(IceesConfig.hatSize), headYawRel, headPitch, headDrop);
		if (IceesConfig.aura) drawAura(matrices, consumers, time, bright, IceesConfig.sizeFactor(IceesConfig.auraSize), camRelYaw, camPitch);
		if (IceesConfig.jumpCircles && (preview || !player.onGround())) {
			drawJumpCircles(matrices, consumers, time, bright, IceesConfig.sizeFactor(IceesConfig.jumpCircleSize));
		}
	}

	/* ===================== WINGS — distinct models ===================== */

	private static void drawWings(PoseStack matrices, MultiBufferSource buffers, float time, float bright, float thick, float bodyDrop) {
		WingStyle style = IceesConfig.wingStyle;
		float[] col = tintWings(style.r, style.g, style.b, time);
		CR = col[0]; CG = col[1]; CB = col[2];
		float beat = Mth.sin(time * 0.22f) * 0.35f;
		VertexConsumer soft = buffers.getBuffer(RenderTypes.eyes(TEX_WING_SOFT));
		VertexConsumer hard = buffers.getBuffer(RenderTypes.eyes(TEX_WING));
		for (int side : new int[]{-1, 1}) {
			matrices.pushPose();
			// Vanilla body: back=+Z. Local wing sheets use -Z — mirror onto the back.
			matrices.translate(side * 0.16f, 1.05f - bodyDrop, 0.15f);
			matrices.scale(1f, 1f, -1f);
			matrices.mulPose(Axis.ZP.rotationDegrees(side * (-36f + beat * 22f)));
			matrices.mulPose(Axis.YP.rotationDegrees(side * 18f));

			switch (style) {
				case PETAL -> wingPetal(matrices, soft, hard, side, bright, thick, time);
				case ANGEL, ARCH -> wingAngel(matrices, soft, hard, side, bright, thick, time);
				case DRAGON -> wingDragon(matrices, soft, hard, side, bright, thick, time);
				case CRYSTAL, MECHA -> wingCrystal(matrices, soft, hard, side, bright, thick, time);
				case PHOENIX -> wingPhoenix(matrices, soft, hard, side, bright, thick, time);
				case DRAGONFLY -> wingDragonfly(matrices, soft, hard, side, bright, thick, time);
				case FAIRY, BUTTERFLY -> wingFairy(matrices, soft, hard, side, bright, thick, time);
				case BAT -> wingBat(matrices, soft, hard, side, bright, thick, time);
				case LEAF -> wingLeaf(matrices, soft, hard, side, bright, thick, time);
			}
			matrices.popPose();
		}
	}

	/** Soft layered blossom feathers — overlapping so no gaps */
	private static void wingPetal(PoseStack m, VertexConsumer soft, VertexConsumer hard, int side, float bright, float thick, float time) {
		float s = side;
		for (int i = 0; i < 5; i++) {
			float t = i / 4f;
			m.pushPose();
			m.translate(0, 0.035f * i, -0.018f * i);
			m.mulPose(Axis.ZP.rotationDegrees(s * (-6f + i * 7f)));
			m.mulPose(Axis.YP.rotationDegrees(s * (4f + i * 3f)));
			float span = (0.75f + t * 0.65f) * thick;
			float tall = (0.6f + t * 0.5f) * thick;
			sheet(m, soft, -s * 0.04f * thick, 0.02f, 0.02f, s * span * 1.05f, tall * 1.05f, -0.1f - t * 0.06f, CR, CG, CB, 0.5f * bright);
			sheet(m, hard, 0, 0, 0.01f, s * span, tall, -0.04f, CR, CG, CB, 0.92f * bright);
			m.popPose();
		}
	}

	/** Tall arched angel wings */
	private static void wingAngel(PoseStack m, VertexConsumer soft, VertexConsumer hard, int side, float bright, float thick, float time) {
		float s = side;
		// upper arch
		sheet(m, soft, 0, 0.2f, 0, s * 1.1f * thick, 1.5f * thick, -0.2f, CR, CG, CB, 0.5f * bright);
		sheet(m, hard, 0, 0.1f, 0.03f, s * 0.95f * thick, 1.35f * thick, -0.1f, CR, CG, CB, 0.95f * bright);
		// lower feather cascade
		for (int i = 0; i < 3; i++) {
			m.pushPose();
			m.translate(s * 0.15f * i, -0.15f * i, -0.05f * i);
			sheet(m, soft, 0, 0, 0, s * (0.9f - i * 0.15f) * thick, (0.7f - i * 0.12f) * thick, -0.12f, CR, CG, CB, 0.45f * bright);
			m.popPose();
		}
		// thickness plane (visible from front/back)
		sheetYZ(m, hard, s * 0.05f, 0.1f, -0.05f, s * 0.05f, 1.2f * thick, -0.25f, CR, CG, CB, 0.35f * bright);
	}

	/** Segmented dragon membrane */
	private static void wingDragon(PoseStack m, VertexConsumer soft, VertexConsumer hard, int side, float bright, float thick, float time) {
		float s = side;
		// bone spine
		sheet(m, hard, 0, 0.9f * thick, 0, s * 1.4f * thick, 1.0f * thick, -0.05f, CR, CG, CB, 1f * bright);
		// membrane panels between "bones"
		for (int i = 0; i < 4; i++) {
			float a = i / 3f;
			float x0 = s * a * 1.35f * thick;
			float x1 = s * (a + 0.28f) * 1.35f * thick;
			float yTop = 0.95f * thick - a * 0.15f;
			float yBot = 0.15f - a * 0.35f * thick;
			sheet(m, soft, x0, yBot, -0.02f, x1, yTop, -0.18f - a * 0.08f, CR, CG, CB, 0.7f * bright);
			sheet(m, hard, x0, yBot + 0.05f, 0.02f, x1, yTop - 0.05f, -0.1f, CR, CG, CB, 0.85f * bright);
		}
		// claw tip
		sheet(m, hard, s * 1.25f * thick, 0.85f * thick, 0, s * 1.55f * thick, 1.05f * thick, -0.15f, CR, CG, CB, bright);
	}

	/** Angular crystal shards — dense fan, overlapping so no gaps */
	private static void wingCrystal(PoseStack m, VertexConsumer soft, VertexConsumer hard, int side, float bright, float thick, float time) {
		float s = side;
		int blades = 9;
		for (int i = 0; i < blades; i++) {
			float t = i / (float) (blades - 1);
			m.pushPose();
			// Tight fan — small angles so blades sit on top of each other
			m.mulPose(Axis.YP.rotationDegrees(s * (2f + t * 5f)));
			m.mulPose(Axis.ZP.rotationDegrees(s * (-10f + t * 20f)));
			float x0 = s * 0.03f * thick;
			float x1 = s * (1.0f + t * 0.4f) * thick;
			float y0 = (-0.1f + t * 0.4f) * thick;
			float y1 = (0.65f + t * 0.35f) * thick;
			sheet(m, soft, x0, y0 - 0.18f * thick, 0.04f, x1, y1 + 0.18f * thick, -0.12f, CR, CG, CB, 0.5f * bright);
			sheet(m, hard, x0, y0, 0, x1 * 0.95f, y1, -0.06f, CR, CG, CB, 0.95f * bright);
			sheetYZ(m, hard, x1 * 0.9f, y0, -0.05f, x1 * 0.9f, y1, 0.05f, CR, CG, CB, 0.45f * bright);
			m.popPose();
		}
	}

	/** Teal dragonfly panes — packed together, rounded soft fill */
	private static void wingDragonfly(PoseStack m, VertexConsumer soft, VertexConsumer hard, int side, float bright, float thick, float time) {
		float s = side;
		float flutter = Mth.sin(time * 0.55f + side) * 6f;
		m.mulPose(Axis.ZP.rotationDegrees(s * flutter * 0.25f));
		int panes = 8;
		for (int i = 0; i < panes; i++) {
			float t = i / (float) (panes - 1);
			float y0 = (-0.2f + t * 0.7f) * thick;
			float y1 = y0 + 0.7f * thick; // heavy overlap
			float x0 = s * 0.03f * thick;
			float x1 = s * (1.3f + t * 0.2f) * thick;
			float z = -0.03f - t * 0.02f;
			sheet(m, soft, x0, y0 - 0.12f * thick, z + 0.04f, x1, y1 + 0.12f * thick, z - 0.06f, CR, CG, CB, 0.55f * bright);
			sheet(m, hard, x0, y0, z, x1, y1, z - 0.02f, CR * 0.85f, CG * 0.95f, CB, 0.85f * bright);
			sheetYZ(m, hard, x1, y0, z - 0.02f, x1, y1, z + 0.02f, 0.05f, 0.1f, 0.12f, 0.45f * bright);
		}
	}

	/** Soft layered fairy wings — smaller, more rounded. */
	private static void wingFairy(PoseStack m, VertexConsumer soft, VertexConsumer hard, int side, float bright, float thick, float time) {
		float s = side;
		float flutter = Mth.sin(time * 0.7f) * 10f;
		m.mulPose(Axis.ZP.rotationDegrees(s * flutter * 0.4f));
		for (int i = 0; i < 3; i++) {
			float t = i / 2f;
			m.pushPose();
			m.translate(0, 0.12f * i, -0.03f * i);
			m.mulPose(Axis.YP.rotationDegrees(s * (12f + i * 8f)));
			float span = (0.85f + t * 0.35f) * thick;
			float tall = (0.7f - t * 0.15f) * thick;
			sheet(m, soft, 0, 0.1f, 0, s * span, tall, -0.1f, CR, CG, CB, 0.5f * bright);
			sheet(m, hard, 0, 0.05f, 0.02f, s * span * 0.85f, tall * 0.85f, -0.05f, CR, CG, CB, 0.85f * bright);
			sheet(m, hard, s * span * 0.3f, 0.15f, -0.02f, s * span * 0.35f, tall * 0.7f, -0.08f, 0.4f, 0.25f, 0.55f, 0.45f * bright);
			m.popPose();
		}
	}

	/** Leathery bat / demon wings. */
	private static void wingBat(PoseStack m, VertexConsumer soft, VertexConsumer hard, int side, float bright, float thick, float time) {
		float s = side;
		float flap = Mth.sin(time * 0.28f) * 0.2f;
		sheet(m, hard, 0, 0.85f * thick + flap, 0, s * 1.5f * thick, 0.95f * thick + flap, -0.08f, CR * 0.7f, CG * 0.7f, CB * 0.75f, bright);
		for (int i = 0; i < 4; i++) {
			float a = i / 3f;
			float x0 = s * a * 1.4f * thick;
			float x1 = s * (a + 0.32f) * 1.4f * thick;
			float yTop = 0.9f * thick + flap - a * 0.1f;
			float yBot = -0.15f - a * 0.45f * thick + flap * 0.5f;
			sheet(m, soft, x0, yBot, -0.04f, x1, yTop, -0.2f - a * 0.06f, CR, CG, CB, 0.75f * bright);
			sheet(m, hard, x0, yBot + 0.05f, 0, x1, yTop - 0.05f, -0.12f, CR * 0.85f, CG * 0.85f, CB * 0.9f, 0.9f * bright);
		}
		sheet(m, hard, s * 1.35f * thick, 0.7f * thick, 0, s * 1.6f * thick, 0.95f * thick, -0.12f, CR, CG, CB, bright);
	}

	/** Rising phoenix flame feathers — tight layers */
	private static void wingPhoenix(PoseStack m, VertexConsumer soft, VertexConsumer hard, int side, float bright, float thick, float time) {
		float s = side;
		float flicker = Mth.sin(time * 0.4f) * 0.06f;
		for (int i = 0; i < 6; i++) {
			float t = i / 5f;
			m.pushPose();
			m.translate(s * 0.04f * i, 0.05f * i + flicker * (1f - t), -0.03f * i);
			m.mulPose(Axis.ZP.rotationDegrees(s * (-16f + i * 6f)));
			float span = (0.6f + t * 0.85f) * thick;
			float tall = (0.55f + t * 0.8f) * thick;
			sheet(m, soft, -s * 0.03f * thick, 0, 0.02f, s * span * 1.08f, tall * 1.05f, -0.1f, CR, Math.max(0.2f, CG * (0.7f + t * 0.3f)), CB * 0.5f, 0.5f * bright);
			sheet(m, hard, 0, 0.03f, 0.02f, s * span, tall, -0.05f, CR, CG, CB, 0.95f * bright);
			m.popPose();
		}
	}

	/** Soft layered leaf wings — tight overlap */
	private static void wingLeaf(PoseStack m, VertexConsumer soft, VertexConsumer hard, int side, float bright, float thick, float time) {
		float s = side;
		float sway = Mth.sin(time * 0.18f) * 0.06f;
		for (int i = 0; i < 6; i++) {
			float t = i / 5f;
			m.pushPose();
			m.translate(0, 0.03f * i + sway * t, -0.015f * i);
			m.mulPose(Axis.ZP.rotationDegrees(s * (-10f + i * 5f)));
			float span = (0.5f + t * 0.8f) * thick;
			float tall = (0.4f + t * 0.65f) * thick;
			sheet(m, soft, -s * 0.03f * thick, 0, 0.02f, s * span * 1.08f, tall * 1.05f, -0.07f, CR, CG, CB, 0.55f * bright);
			sheet(m, hard, 0, 0.01f, 0.01f, s * span, tall, -0.03f, CR * 0.85f, CG, CB * 0.7f, 0.95f * bright);
			m.popPose();
		}
	}

	/* ===================== HATS ===================== */

	private static float hatGradT = 0.5f;

	private static float[] hatColor(float baseR, float baseG, float baseB) {
		return IceesConfig.color(IceesConfig.hatTint, baseR, baseG, baseB, hatGradT);
	}

	private static void drawHat(PoseStack matrices, MultiBufferSource buffers, float time, float bright, float thick,
		float headYawRel, float headPitch, float headDrop) {
		hatGradT = gradPhase(time);
		HatStyle style = IceesConfig.hatStyle;
		VertexConsumer vc = buffers.getBuffer(RenderTypes.eyes(TEX_CAPE));
		matrices.pushPose();
		// Full head follow — same pitch sign as HumanoidModel.head.xRot so look-up tips
		// the hat back and look-down tips it forward (not onto the chest).
		matrices.translate(0, 1.5f - headDrop, 0);
		float yawRad = headYawRel * Mth.DEG_TO_RAD;
		float pitchRad = headPitch * Mth.DEG_TO_RAD;
		matrices.mulPose(new Quaternionf().rotationZYX(0f, yawRad, pitchRad));
		matrices.translate(0, 0.22f + 0.04f * thick, 0);
		float s = 0.85f + thick * 0.2f;

		switch (style) {
			case STRAW_RED -> drawVoxelStrawHat(matrices, vc, s, bright, 0.86f, 0.12f, 0.12f);
			case STRAW_BROWN -> drawVoxelStrawHat(matrices, vc, s, bright, 0.42f, 0.26f, 0.14f);
			case CHINA -> drawChinaHat(matrices, vc, s, bright);
			case TOP_HAT -> drawTopHat(matrices, vc, s, bright);
			case BEANIE -> drawBeanie(matrices, vc, s, bright);
			case BERET -> drawBeret(matrices, vc, s, bright);
			case CROWN_BRIM -> drawCrownBrim(matrices, vc, s, bright);
		}
		matrices.popPose();
	}

	private static void drawTopHat(PoseStack m, VertexConsumer vc, float s, float bright) {
		float a = Math.min(1f, 0.95f * bright);
		float[] tint = hatColor(0.12f, 0.10f, 0.14f);
		float r = tint[0], g = tint[1], b = tint[2];
		float[] band = hatColor(0.75f, 0.2f, 0.28f);
		float u = 0.08f * s;
		MeshPrims.cylinder(m, vc, LightTexture.FULL_BRIGHT, 0, 0, 0, 0.38f * s, u, 20, r, g, b, a);
		MeshPrims.cylinder(m, vc, LightTexture.FULL_BRIGHT, 0, u, 0, 0.22f * s, 0.48f * s, 16, r * 1.08f, g * 1.08f, b * 1.08f, a);
		MeshPrims.cylinder(m, vc, LightTexture.FULL_BRIGHT, 0, u + 0.02f * s, 0, 0.24f * s, 0.08f * s, 16, band[0], band[1], band[2], a);
	}

	private static void drawBeanie(PoseStack m, VertexConsumer vc, float s, float bright) {
		float a = Math.min(1f, 0.95f * bright);
		float[] tint = hatColor(0.85f, 0.35f, 0.50f);
		float r = tint[0], g = tint[1], b = tint[2];
		for (int i = 0; i < 5; i++) {
			float t = i / 4f;
			float rad = (0.30f - t * 0.08f) * s;
			float y0 = i * 0.07f * s;
			float y1 = y0 + 0.09f * s;
			MeshPrims.cylinder(m, vc, LightTexture.FULL_BRIGHT, 0, y0, 0, rad, y1 - y0, 16, r * (1f - t * 0.08f), g * (1f - t * 0.08f), b * (1f - t * 0.08f), a);
		}
		billboardOrb(m, vc, 0, 0.42f * s, 0, 0.09f * s, Math.min(1f, r + 0.15f), Math.min(1f, g + 0.15f), Math.min(1f, b + 0.1f), a, 0f);
	}

	private static void drawBeret(PoseStack m, VertexConsumer vc, float s, float bright) {
		float a = Math.min(1f, 0.95f * bright);
		float[] tint = hatColor(0.55f, 0.18f, 0.22f);
		float r = tint[0], g = tint[1], b = tint[2];
		MeshPrims.cylinder(m, vc, LightTexture.FULL_BRIGHT, 0, 0.02f * s, 0, 0.36f * s, 0.12f * s, 20, r, g, b, a);
		MeshPrims.cylinder(m, vc, LightTexture.FULL_BRIGHT, 0.12f * s, 0.08f * s, 0, 0.14f * s, 0.1f * s, 12, r * 0.9f, g * 0.9f, b * 0.9f, a);
	}

	private static void drawCrownBrim(PoseStack m, VertexConsumer vc, float s, float bright) {
		float a = Math.min(1f, 0.95f * bright);
		float[] tint = hatColor(1f, 0.82f, 0.4f);
		float r = tint[0], g = tint[1], b = tint[2];
		MeshPrims.cylinder(m, vc, LightTexture.FULL_BRIGHT, 0, 0, 0, 0.34f * s, 0.08f * s, 20, r, g, b, a);
		for (int i = 0; i < 8; i++) {
			float ang = i / 8f * ((float) Math.PI * 2f);
			float x = Mth.cos(ang) * 0.26f * s;
			float z = Mth.sin(ang) * 0.26f * s;
			MeshPrims.cylinder(m, vc, LightTexture.FULL_BRIGHT, x, 0.05f * s, z, 0.05f * s, 0.24f * s, 8, r, g, b, a);
		}
	}

	/** Voxel straw hat — body + band respect hatTint. */
	private static void drawVoxelStrawHat(PoseStack m, VertexConsumer vc, float s, float bright,
		float bandR, float bandG, float bandB) {
		float u = 0.0625f * s;
		float a = Math.min(1f, 0.98f * bright);
		float[] light = hatColor(0.93f, 0.80f, 0.48f);
		float[] mid = hatColor(0.82f, 0.66f, 0.34f);
		float[] dark = hatColor(0.70f, 0.54f, 0.26f);
		float[] band = hatColor(bandR, bandG, bandB);
		float s0r = light[0], s0g = light[1], s0b = light[2];
		float s1r = mid[0], s1g = mid[1], s1b = mid[2];
		float s2r = dark[0], s2g = dark[1], s2b = dark[2];
		bandR = band[0]; bandG = band[1]; bandB = band[2];

		// Stepped circular brim (voxel ring masks)
		int[][] brim = {
			// dx,dz half-extent in px — each ring from outside in
			{8, 8}, {9, 7}, {9, 6}, {8, 5}, {7, 4}
		};
		// Rounder brim: filled disc with slight overlap between cells (no gaps)
		for (int ix = -9; ix <= 9; ix++) {
			for (int iz = -9; iz <= 9; iz++) {
				float dist = Mth.sqrt(ix * ix + iz * iz);
				if (dist > 9.2f) continue;
				// hole under crown
				if (dist < 3.2f) continue;
				float x0 = ix * u - u * 0.08f, z0 = iz * u - u * 0.08f;
				float x1 = x0 + u * 1.16f, z1 = z0 + u * 1.16f;
				boolean weave = ((ix + iz) & 1) == 0;
				float rr = weave ? s0r : s1r, gg = weave ? s0g : s1g, bb = weave ? s0b : s1b;
				MeshPrims.box(m, vc, LightTexture.FULL_BRIGHT, x0, 0, z0, x1, u * 0.95f, z1, rr, gg, bb, a);
				MeshPrims.box(m, vc, LightTexture.FULL_BRIGHT, x0, -u * 0.35f, z0, x1, 0, z1, s2r, s2g, s2b, a * 0.95f);
			}
		}
		// soft outer rim ring — fills jagged edge
		MeshPrims.cylinder(m, vc, LightTexture.FULL_BRIGHT, 0, 0, 0, 9.1f * u, u * 0.7f, 24, s1r, s1g, s1b, a * 0.85f);

		// Crown box (6x6 x 4px tall)
		float cw = 3.2f * u;
		float ch = 4.2f * u;
		float y0 = u * 0.85f;
		// sides with weave checker
		for (int face = 0; face < 4; face++) {
			m.pushPose();
			m.mulPose(Axis.YP.rotationDegrees(face * 90f));
			for (int ix = -3; ix < 3; ix++) {
				for (int iy = 0; iy < 4; iy++) {
					boolean weave = ((ix + iy) & 1) == 0;
					float rr = weave ? s0r : s1r, gg = weave ? s0g : s1g, bb = weave ? s0b : s1b;
					MeshPrims.box(m, vc, LightTexture.FULL_BRIGHT,
						ix * u, y0 + iy * u, cw - u * 0.05f,
						(ix + 1) * u, y0 + (iy + 1) * u, cw + u * 0.55f,
						rr, gg, bb, a);
				}
			}
			m.popPose();
		}
		// crown top checker
		for (int ix = -3; ix < 3; ix++) {
			for (int iz = -3; iz < 3; iz++) {
				boolean weave = ((ix + iz) & 1) == 0;
				float rr = weave ? s0r : s1r, gg = weave ? s0g : s1g, bb = weave ? s0b : s1b;
				MeshPrims.box(m, vc, LightTexture.FULL_BRIGHT,
					ix * u, y0 + ch - u * 0.1f, iz * u,
					(ix + 1) * u, y0 + ch + u * 0.5f, (iz + 1) * u,
					rr, gg, bb, a);
			}
		}

		// Band around crown base (1px tall)
		float bandY0 = y0;
		float bandY1 = y0 + u * 1.15f;
		float br = cw + u * 0.15f;
		for (int face = 0; face < 4; face++) {
			m.pushPose();
			m.mulPose(Axis.YP.rotationDegrees(face * 90f));
			MeshPrims.box(m, vc, LightTexture.FULL_BRIGHT,
				-cw, bandY0, br - u * 0.2f,
				cw, bandY1, br + u * 0.7f,
				bandR, bandG, bandB, a);
			m.popPose();
		}
	}

	/** Conical China / rice hat. */
	private static void drawChinaHat(PoseStack m, VertexConsumer vc, float s, float bright) {
		float r0 = 0.62f * s;
		float h = 0.42f * s;
		float[] bamboo = hatColor(0.78f, 0.68f, 0.42f);
		float[] tip = hatColor(0.55f, 0.45f, 0.28f);
		float bambooR = bamboo[0], bambooG = bamboo[1], bambooB = bamboo[2];
		float tipR = tip[0], tipG = tip[1], tipB = tip[2];
		float a = Math.min(1f, 0.95f * bright);
		int rings = 6;
		int segs = 16;

		for (int ring = 0; ring < rings; ring++) {
			float t0 = ring / (float) rings;
			float t1 = (ring + 1) / (float) rings;
			float y0 = t0 * h;
			float y1 = t1 * h;
			float rad0 = r0 * (1f - t0);
			float rad1 = r0 * (1f - t1);
			float shade = 1f - t0 * 0.25f;
			for (int i = 0; i < segs; i++) {
				float a0 = i / (float) segs * ((float) Math.PI * 2f);
				float a1 = (i + 1) / (float) segs * ((float) Math.PI * 2f);
				float x00 = Mth.cos(a0) * rad0, z00 = Mth.sin(a0) * rad0;
				float x01 = Mth.cos(a1) * rad0, z01 = Mth.sin(a1) * rad0;
				float x10 = Mth.cos(a0) * rad1, z10 = Mth.sin(a0) * rad1;
				float x11 = Mth.cos(a1) * rad1, z11 = Mth.sin(a1) * rad1;
				sheet(m, vc, x00, y0, z00, x10, y1, z10, bambooR * shade, bambooG * shade, bambooB * shade, a);
				sheet(m, vc, x10, y1, z10, x11, y1, z11, bambooR * shade, bambooG * shade, bambooB * shade, a);
				sheet(m, vc, x11, y1, z11, x01, y0, z01, bambooR * shade, bambooG * shade, bambooB * shade, a);
				sheet(m, vc, x01, y0, z01, x00, y0, z00, bambooR * shade, bambooG * shade, bambooB * shade, a);
			}
		}
		// tip knob
		billboardOrb(m, vc, 0, h + 0.02f, 0, 0.05f * s, tipR, tipG, tipB, a, 0f);
		// underside dark
		for (int i = 0; i < segs; i++) {
			float a0 = i / (float) segs * ((float) Math.PI * 2f);
			float a1 = (i + 1) / (float) segs * ((float) Math.PI * 2f);
			float x0 = Mth.cos(a0) * r0, z0 = Mth.sin(a0) * r0;
			float x1 = Mth.cos(a1) * r0, z1 = Mth.sin(a1) * r0;
			sheet(m, vc, 0, 0.01f, 0, x0, 0.01f, z0, tipR, tipG, tipB, a * 0.7f);
			sheet(m, vc, x0, 0.01f, z0, x1, 0.01f, z1, tipR, tipG, tipB, a * 0.7f);
			sheet(m, vc, x1, 0.01f, z1, 0, 0.01f, 0, tipR, tipG, tipB, a * 0.7f);
		}
	}

	/* ===================== HALO — distinct models ===================== */

	/** Halo stays level (no head look) but drops with sneak. */
	private static void drawHalo(PoseStack matrices, MultiBufferSource buffers, float time, float bright, float thick,
		float headDrop) {
		HaloStyle style = IceesConfig.haloStyle;
		float[] c = tintHalo(style.r, style.g, style.b, time); CR = c[0]; CG = c[1]; CB = c[2];
		VertexConsumer vc = buffers.getBuffer(RenderTypes.eyes(TEX_HALO));
		VertexConsumer glow = buffers.getBuffer(RenderTypes.eyes(TEX_AURA));
		matrices.pushPose();
		matrices.translate(0, 1.5f - headDrop + 0.55f + 0.08f * thick, 0);

		switch (style) {
			case BLOSSOM -> {
				float r = 0.38f * thick;
				ring3D(matrices, vc, r, 0.06f, CR, CG, CB, bright);
				for (int i = 0; i < 6; i++) {
					float a = i / 6f * ((float) Math.PI * 2f) + time * 0.05f;
					matrices.pushPose();
					matrices.mulPose(Axis.YP.rotationDegrees(a * Mth.RAD_TO_DEG));
					matrices.translate(0, 0, r);
					sheet(matrices, glow, -0.12f * thick, -0.05f, 0, 0.12f * thick, 0.2f * thick, 0.15f * thick, CR, CG, CB, 0.8f * bright);
					matrices.popPose();
				}
			}
			case RING -> {
				ring3D(matrices, vc, 0.42f * thick, 0.08f * thick, CR, CG, CB, bright);
			}
			case ORBIT -> {
				ring3D(matrices, glow, 0.35f * thick, 0.04f, CR, CG, CB, bright * 0.5f);
				for (int i = 0; i < 3; i++) {
					float a = time * 0.12f + i * ((float) Math.PI * 2f / 3f);
					float x = Mth.cos(a) * 0.45f * thick;
					float z = Mth.sin(a) * 0.45f * thick;
					billboardOrb(matrices, vc, x, Mth.sin(a * 2) * 0.08f, z, 0.12f * thick, CR, CG, CB, bright, time);
				}
			}
			case CROWN -> {
				ring3D(matrices, vc, 0.32f * thick, 0.05f, CR, CG, CB, bright);
				for (int i = 0; i < 7; i++) {
					float a = i / 7f * ((float) Math.PI * 2f);
					matrices.pushPose();
					matrices.mulPose(Axis.YP.rotationDegrees(a * Mth.RAD_TO_DEG));
					matrices.translate(0, 0.05f, 0.3f * thick);
					sheet(matrices, vc, -0.05f, 0, 0, 0.05f, 0.35f * thick, 0.02f, CR, CG, CB, bright);
					sheetYZ(matrices, vc, 0, 0, -0.04f, 0, 0.35f * thick, 0.04f, CR, CG, CB, 0.7f * bright);
					matrices.popPose();
				}
			}
			case PULSE -> {
				float pulse = 1f + Mth.sin(time * 0.28f) * 0.18f;
				ring3D(matrices, vc, 0.4f * thick * pulse, 0.07f * thick, CR, CG, CB, bright);
				billboardOrb(matrices, glow, 0, 0, 0, 0.35f * thick * pulse, CR, CG, CB, bright * 0.45f, time);
			}
			case NOVA -> {
				matrices.mulPose(Axis.YP.rotationDegrees(time * 2.2f));
				for (int i = 0; i < 8; i++) {
					float a = i / 8f * ((float) Math.PI * 2f);
					matrices.pushPose();
					matrices.mulPose(Axis.YP.rotationDegrees(a * Mth.RAD_TO_DEG));
					sheet(matrices, vc, -0.04f, -0.04f, 0, 0.04f, 0.04f, 0.55f * thick, CR, CG, CB, bright);
					sheet(matrices, glow, -0.08f, -0.08f, 0, 0.08f, 0.08f, 0.35f * thick, CR, CG, CB, 0.4f * bright);
					matrices.popPose();
				}
				billboardOrb(matrices, vc, 0, 0, 0, 0.15f * thick, CR, CG, CB, bright, time);
			}
			case DOUBLE -> {
				ring3D(matrices, vc, 0.42f * thick, 0.05f * thick, CR, CG, CB, bright);
				ring3D(matrices, glow, 0.28f * thick, 0.04f * thick, CR, CG, CB, bright * 0.7f);
			}
			case CRESCENT -> {
				for (int i = 0; i < 10; i++) {
					float a = -0.6f + i / 9f * 3.6f;
					float x = Mth.cos(a) * 0.4f * thick;
					float z = Mth.sin(a) * 0.4f * thick;
					MeshPrims.box(matrices, vc, LightTexture.FULL_BRIGHT,
						x - 0.05f * thick, -0.04f, z - 0.05f * thick,
						x + 0.05f * thick, 0.04f, z + 0.05f * thick,
						CR, CG, CB, Math.min(1f, bright));
				}
			}
			case HORNS -> {
				for (int side : new int[]{-1, 1}) {
					MeshPrims.box(matrices, vc, LightTexture.FULL_BRIGHT,
						side * 0.22f * thick, 0.05f, -0.06f,
						side * 0.38f * thick, 0.28f * thick, 0.06f,
						CR, CG, CB, bright);
					MeshPrims.box(matrices, vc, LightTexture.FULL_BRIGHT,
						side * 0.32f * thick, 0.25f * thick, -0.05f,
						side * 0.48f * thick, 0.48f * thick, 0.05f,
						CR, CG, CB, bright);
				}
			}
			case DIAMOND -> {
				billboardOrb(matrices, vc, 0, 0.08f, 0, 0.28f * thick, CR, CG, CB, bright, time);
				ring3D(matrices, glow, 0.38f * thick, 0.03f, CR, CG, CB, bright * 0.55f);
			}
			case STAR -> {
				for (int i = 0; i < 5; i++) {
					float a = i / 5f * ((float) Math.PI * 2f) + time * 0.04f;
					matrices.pushPose();
					matrices.mulPose(Axis.YP.rotationDegrees(a * Mth.RAD_TO_DEG));
					sheet(matrices, vc, -0.05f, 0, 0.05f, 0.05f, 0.32f * thick, 0.42f * thick, CR, CG, CB, bright);
					matrices.popPose();
				}
				ring3D(matrices, glow, 0.28f * thick, 0.03f, CR, CG, CB, bright * 0.5f);
			}
			case HEX -> {
				for (int i = 0; i < 6; i++) {
					float a0 = i / 6f * ((float) Math.PI * 2f);
					float a1 = (i + 1) / 6f * ((float) Math.PI * 2f);
					float r = 0.4f * thick;
					float x0 = Mth.cos(a0) * r, z0 = Mth.sin(a0) * r;
					float x1 = Mth.cos(a1) * r, z1 = Mth.sin(a1) * r;
					sheet(matrices, vc, x0, -0.04f, z0, x1, 0.04f, z1, CR, CG, CB, bright);
				}
			}
			case SPIRAL -> {
				for (int i = 0; i < 10; i++) {
					float t = i / 10f;
					float a = t * ((float) Math.PI * 4f) + time * 0.1f;
					float rad = 0.15f * thick + t * 0.3f * thick;
					billboardOrb(matrices, vc, Mth.cos(a) * rad, t * 0.12f * thick, Mth.sin(a) * rad, 0.07f * thick, CR, CG, CB, bright, time);
				}
			}
			case FLAME -> {
				for (int i = 0; i < 7; i++) {
					float a = i / 7f * ((float) Math.PI * 2f);
					matrices.pushPose();
					matrices.mulPose(Axis.YP.rotationDegrees(a * Mth.RAD_TO_DEG + time * 4f));
					sheet(matrices, glow, -0.06f, 0, 0.2f * thick, 0.06f, 0.4f * thick + Mth.sin(time * 0.3f + i) * 0.05f, 0.35f * thick, CR, CG, CB, bright);
					matrices.popPose();
				}
			}
		}
		matrices.popPose();
	}

	/** Smooth torus — overlapping segments so no gaps, more round */
	private static void ring3D(PoseStack matrices, VertexConsumer vc, float radius, float tube, float r, float g, float b, float bright) {
		int segments = 28;
		float tubeSoft = tube * 1.25f;
		for (int i = 0; i < segments; i++) {
			float a0 = i / (float) segments * ((float) Math.PI * 2f);
			float a1 = (i + 1.15f) / (float) segments * ((float) Math.PI * 2f); // slight overlap
			float x0 = Mth.cos(a0) * radius, z0 = Mth.sin(a0) * radius;
			float x1 = Mth.cos(a1) * radius, z1 = Mth.sin(a1) * radius;
			sheet(matrices, vc, x0, -tubeSoft, z0, x1, tubeSoft, z1, r, g, b, 0.95f * bright);
			sheetYZ(matrices, vc, x0, -tubeSoft, z0 - tubeSoft, x0, tubeSoft, z0 + tubeSoft, r, g, b, 0.55f * bright);
			// soft inner glow disc bits — fills the ring body
			billboardOrb(matrices, vc, x0, 0, z0, tube * 1.1f, r, g, b, 0.35f * bright, 0f);
		}
	}

	/* ===================== CAPE — distinct models ===================== */

	private static void drawCape(PoseStack matrices, MultiBufferSource buffers, float time, float bright, float thick, float bodyDrop) {
		CapeStyle style = IceesConfig.capeStyle;
		float[] c1 = tintCape(style.r1, style.g1, style.b1, time); CR = c1[0]; CG = c1[1]; CB = c1[2];
		float[] c2 = tintCape(style.r2, style.g2, style.b2, time + 1.7f); C2R = c2[0]; C2G = c2[1]; C2B = c2[2];
		float sway = Mth.sin(time * 0.16f) * 0.2f;
		VertexConsumer vc = buffers.getBuffer(RenderTypes.eyes(TEX_CAPE));
		VertexConsumer glow = buffers.getBuffer(RenderTypes.eyes(TEX_AURA));
		// Vanilla body frame: back = +Z
		float back = 0.22f;

		matrices.pushPose();
		matrices.translate(0, 1.35f - bodyDrop, back);
		matrices.mulPose(Axis.XP.rotationDegrees(10f + sway * 16f));

		switch (style) {
			case SAKURA, BLOOM -> capeFlow(matrices, vc, glow, style, bright, thick, sway, back);
			case AURORA -> {
				for (int i = -1; i <= 1; i++) {
					matrices.pushPose();
					matrices.translate(i * 0.22f * thick, 0, i * 0.02f * Math.signum(back));
					matrices.mulPose(Axis.ZP.rotationDegrees(i * 8f));
					capeFlow(matrices, vc, glow, style, bright, thick * 0.7f, sway + i * 0.05f, back);
					matrices.popPose();
				}
			}
			case EMBER -> capeJagged(matrices, vc, glow, style, bright, thick, sway, back);
			case FROST, PRISM -> capeFacets(matrices, vc, glow, style, bright, thick, sway, back);
			case NIGHT -> capeFlow(matrices, vc, glow, style, bright, thick * 1.25f, sway, back);
			case TIDE -> {
				for (int layer = 0; layer < 3; layer++) {
					matrices.pushPose();
					matrices.translate(0, -0.05f * layer, Math.signum(back) * -0.04f * layer);
					matrices.mulPose(Axis.XP.rotationDegrees(layer * 4f + sway * 5f));
					capeFlow(matrices, vc, glow, style, bright * (1f - layer * 0.15f), thick * (1f - layer * 0.1f), sway, back);
					matrices.popPose();
				}
			}
			case GOLD, ROSE -> {
				sheet(matrices, vc, -0.45f * thick, 0.05f, -back * 0.2f, -0.15f * thick, 0.25f, back * 0.25f, CR, CG, CB, bright);
				sheet(matrices, vc, 0.15f * thick, 0.05f, -back * 0.2f, 0.45f * thick, 0.25f, back * 0.25f, CR, CG, CB, bright);
				capeFlow(matrices, vc, glow, style, bright, thick, sway, back);
			}
			case VOID, MIST -> capeFlow(matrices, vc, glow, style, bright, thick * 1.15f, sway, back);
			case LEAF -> capeJagged(matrices, vc, glow, style, bright, thick, sway, back);
		}
		matrices.popPose();
	}

	private static void capeFlow(PoseStack m, VertexConsumer vc, VertexConsumer glow, CapeStyle style, float bright, float thick, float sway, float back) {
		float w = 0.4f * thick;
		float len = -1.3f * thick;
		float zFar = back;
		float zGlow = back * (0.28f / 0.22f);
		sheet(m, vc, -w, 0, 0, w, len, zFar, CR, CG, CB, 0.95f * bright);
		sheetYZ(m, vc, -w, 0, -Math.signum(back) * 0.02f, -w, len, zFar, C2R, C2G, C2B, 0.7f * bright);
		sheetYZ(m, vc, w, 0, -Math.signum(back) * 0.02f, w, len, zFar, C2R, C2G, C2B, 0.7f * bright);
		sheet(m, glow, -w * 1.1f, 0.02f, -Math.signum(back) * 0.03f, w * 1.1f, len * 1.02f, zGlow, C2R, C2G, C2B, 0.3f * bright);
	}

	private static void capeJagged(PoseStack m, VertexConsumer vc, VertexConsumer glow, CapeStyle style, float bright, float thick, float sway, float back) {
		float w = 0.42f * thick;
		int tips = 5;
		float zHang = back * (0.2f / 0.22f);
		for (int i = 0; i < tips; i++) {
			float t0 = i / (float) tips;
			float t1 = (i + 1) / (float) tips;
			float x0 = -w + t0 * 2f * w;
			float x1 = -w + t1 * 2f * w;
			float mid = (x0 + x1) * 0.5f;
			float tipY = -1.15f * thick - (i % 2 == 0 ? 0.25f : 0.05f) * thick;
			sheet(m, vc, x0, 0, 0, mid, tipY, zHang, CR, CG, CB, bright);
			sheet(m, vc, mid, tipY, zHang, x1, 0, 0, C2R, C2G, C2B, bright);
		}
	}

	private static void capeFacets(PoseStack m, VertexConsumer vc, VertexConsumer glow, CapeStyle style, float bright, float thick, float sway, float back) {
		float w = 0.4f * thick;
		float zSign = Math.signum(back);
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 3; col++) {
				float x0 = -w + col * (2f * w / 3f);
				float x1 = x0 + (2f * w / 3f);
				float y0 = -row * (1.2f * thick / 3f);
				float y1 = y0 - (1.2f * thick / 3f);
				float z = zSign * (0.05f * row + 0.08f * (col % 2));
				sheet(m, vc, x0, y0, z, x1, y1, z + zSign * 0.08f, CR, CG, CB, 0.9f * bright);
				sheetYZ(m, vc, x0, y0, z - zSign * 0.03f, x0, y1, z + zSign * 0.08f, C2R, C2G, C2B, 0.55f * bright);
			}
		}
	}

	/* ===================== AURA ===================== */

	private static void drawAura(PoseStack matrices, MultiBufferSource buffers, float time, float bright, float thick, float camRelYaw, float camPitch) {
		AuraStyle style = IceesConfig.auraStyle;
		float[] c = tintAura(style.r, style.g, style.b, time); CR = c[0]; CG = c[1]; CB = c[2];
		VertexConsumer vc = buffers.getBuffer(RenderTypes.eyes(TEX_AURA));
		float aMul = Math.min(1f, 0.9f * bright);

		switch (style) {
			case PULSE -> {
				float pulse = 0.55f + 0.45f * (0.5f + 0.5f * Mth.sin(time * 0.25f));
				billboardOrb(matrices, vc, 0, 1.0f, 0, 1.0f * thick * pulse, CR, CG, CB, aMul * 0.7f, time);
			}
			case RING -> {
				matrices.pushPose();
				matrices.translate(0, 0.08f, 0);
				ring3D(matrices, vc, 0.85f * thick, 0.06f * thick, CR, CG, CB, aMul);
				matrices.popPose();
			}
			case TWIN -> {
				billboardOrb(matrices, vc, -0.85f * thick, 1.05f, 0, 0.55f * thick, CR, CG, CB, aMul, time);
				billboardOrb(matrices, vc, 0.85f * thick, 1.05f, 0, 0.55f * thick, CR, CG, CB, aMul, time);
			}
			case HEARTBEAT -> {
				float beat = Mth.sin(time * 0.55f);
				float pulse = beat > 0 ? 0.7f + beat * 0.5f : 0.55f + beat * 0.15f;
				billboardOrb(matrices, vc, 0, 1.0f, 0, 0.95f * thick * pulse, CR, CG, CB, aMul * 0.85f, time);
			}
			case FLAME -> {
				for (int i = 0; i < style.count; i++) {
					float a = time * style.spin + i * ((float) Math.PI * 2f / style.count);
					float y = 0.35f + ((time * 0.08f + i * 0.2f) % 1.5f);
					float fade = 1f - (y - 0.35f) / 1.5f;
					billboardOrb(matrices, vc, Mth.cos(a) * 0.4f * thick, y, Mth.sin(a) * 0.4f * thick,
						(0.2f + 0.12f * fade) * thick, CR, CG, CB, aMul * fade, time);
				}
			}
			case SPIRAL -> {
				for (int i = 0; i < style.count; i++) {
					float t = (time * 0.12f + i / (float) style.count) % 1f;
					float a = t * ((float) Math.PI * 4f) + time * style.spin;
					float radius = 0.3f * thick + t * 0.55f * thick;
					billboardOrb(matrices, vc, Mth.cos(a) * radius, 0.2f + t * 1.8f, Mth.sin(a) * radius,
						0.26f * thick * (1f - t * 0.4f), CR, CG, CB, aMul * (1f - t * 0.5f), time);
				}
			}
			case DRAGONFLY -> {
				for (int i = 0; i < style.count; i++) {
					float a = time * style.spin + i * ((float) Math.PI * 2f / style.count);
					float x = Mth.cos(a) * 0.7f * thick;
					float z = Mth.sin(a) * 0.7f * thick;
					matrices.pushPose();
					matrices.translate(x, 1.0f + Mth.sin(time * 0.2f + i) * 0.1f, z);
					matrices.mulPose(Axis.YP.rotationDegrees(a * Mth.RAD_TO_DEG));
					sheet(matrices, vc, -0.25f * thick, -0.02f, 0, 0.25f * thick, 0.02f, -0.35f * thick, CR, CG, CB, aMul);
					sheet(matrices, vc, -0.02f, -0.15f * thick, 0, 0.02f, 0.15f * thick, -0.3f * thick, CR * 0.7f, CG * 0.8f, CB, aMul * 0.8f);
					matrices.popPose();
				}
			}
			case BEACON -> {
				int n = Math.max(14, style.count + 10);
				for (int i = 0; i < n; i++) {
					float a = i * ((float) Math.PI * 2f / n) + time * style.spin;
					float x = Mth.cos(a) * 0.42f * thick;
					float z = Mth.sin(a) * 0.42f * thick;
					MeshPrims.cylinder(matrices, vc, LightTexture.FULL_BRIGHT,
						x, 0.1f, z, 0.2f * thick, 1.85f * thick, 12, CR, CG, CB, aMul * 0.5f);
				}
			}
			case SHIELD -> {
				int n = Math.max(18, style.count + 10);
				for (int i = 0; i < n; i++) {
					float a = i * ((float) Math.PI * 2f / n);
					float x = Mth.cos(a) * 0.7f * thick;
					float z = Mth.sin(a) * 0.7f * thick;
					MeshPrims.cylinder(matrices, vc, LightTexture.FULL_BRIGHT,
						x, 0.35f, z, 0.22f * thick, 1.3f * thick, 12, CR, CG, CB, aMul * 0.48f);
				}
			}
			case RIBBON -> {
				for (int i = 0; i < style.count; i++) {
					float t = (time * 0.1f + i / (float) style.count) % 1f;
					float a = t * Mth.TWO_PI * 2f + time * style.spin;
					float radius = 0.35f * thick + t * 0.4f * thick;
					float y = 0.3f + t * 1.5f;
					MeshPrims.box(matrices, vc, LightTexture.FULL_BRIGHT,
						Mth.cos(a) * radius - 0.06f, y, Mth.sin(a) * radius - 0.02f,
						Mth.cos(a) * radius + 0.06f, y + 0.12f, Mth.sin(a) * radius + 0.02f,
						CR, CG, CB, aMul * (1f - t * 0.4f));
				}
			}
			case CUBE -> {
				for (int i = 0; i < style.count; i++) {
					float a = time * style.spin + i * ((float) Math.PI * 2f / style.count);
					float x = Mth.cos(a) * 0.8f * thick;
					float z = Mth.sin(a) * 0.8f * thick;
					float y = 0.9f + Mth.sin(time * 0.2f + i) * 0.2f;
					float hs = 0.1f * thick;
					MeshPrims.box(matrices, vc, LightTexture.FULL_BRIGHT, x - hs, y - hs, z - hs, x + hs, y + hs, z + hs, CR, CG, CB, aMul);
				}
			}
			case MOON -> {
				float pulse = 0.85f + 0.15f * Mth.sin(time * 0.12f);
				billboardOrb(matrices, vc, 0, 1.0f, 0, 1.15f * thick * pulse, CR, CG, CB, aMul * 0.55f, time);
				ring3D(matrices, vc, 0.7f * thick, 0.03f * thick, CR, CG, CB, aMul * 0.4f);
			}
			case LOTUS -> {
				matrices.pushPose();
				matrices.translate(0, 0.1f, 0);
				for (int i = 0; i < style.count; i++) {
					float a = i / (float) style.count * ((float) Math.PI * 2f) + time * style.spin;
					matrices.pushPose();
					matrices.mulPose(Axis.YP.rotationDegrees(a * Mth.RAD_TO_DEG));
					sheet(matrices, vc, -0.12f * thick, 0, 0.2f * thick, 0.12f * thick, 0.35f * thick, 0.75f * thick, CR, CG, CB, aMul * 0.85f);
					matrices.popPose();
				}
				matrices.popPose();
			}
			case PETAL_STORM, STORM -> {
				int n = Math.max(2, style.count);
				for (int i = 0; i < n; i++) {
					float a = time * (0.1f + style.spin) + i * ((float) Math.PI * 2f / n);
					float radius = (style == AuraStyle.STORM ? 0.9f : 0.7f) * thick;
					float y = 0.4f + ((time * 0.15f + i * 0.17f) % 1.6f);
					float s = (style == AuraStyle.STORM ? 0.12f : 0.22f) * thick;
					billboardOrb(matrices, vc, Mth.cos(a) * radius, y, Mth.sin(a) * radius, s, CR, CG, CB, aMul, time);
				}
			}
			default -> {
				int n = Math.max(2, style.count);
				for (int i = 0; i < n; i++) {
					float a = time * (0.08f + style.spin) + i * ((float) Math.PI * 2f / n);
					float radius = (style == AuraStyle.SPARKS ? 0.95f : 0.75f) * thick;
					float y = 0.85f + Mth.sin(time * 0.15f + i) * 0.18f;
					float s = (style == AuraStyle.SPARKS ? 0.16f : 0.3f) * thick;
					billboardOrb(matrices, vc, Mth.cos(a) * radius, y, Mth.sin(a) * radius, s, CR, CG, CB, aMul, time);
				}
			}
		}
	}

	private static void drawJumpCircles(PoseStack matrices, MultiBufferSource buffers, float time, float bright, float thick) {
		JumpCircleStyle style = IceesConfig.jumpCircleStyle;
		VertexConsumer vc = buffers.getBuffer(RenderTypes.eyes(TEX_HALO));
		VertexConsumer glow = buffers.getBuffer(RenderTypes.eyes(TEX_AURA));
		float[] c = IceesConfig.color(IceesConfig.jumpTint, 1f, 0.75f, 0.88f, gradPhase(time));
		float speed = 0.55f + IceesConfig.jumpCircleSpeed * 0.18f;
		float size = thick;
		int rings = Math.max(1, IceesConfig.jumpCircleCount);
		float cycle = 22f / speed;
		float phase = (time % cycle) / cycle;

		matrices.pushPose();
		matrices.translate(0, 0.05f, 0);

		switch (style) {
			case PULSE -> {
				float p = 0.55f + 0.45f * (0.5f + 0.5f * Mth.sin(time * 0.2f * speed));
				ring3D(matrices, vc, 0.7f * size * p, 0.05f * size, c[0], c[1], c[2], 0.8f * bright);
			}
			case DOUBLE -> {
				for (int ring = 0; ring < Math.max(2, rings); ring++) {
					float p = (phase + ring * (1f / Math.max(2, rings))) % 1f;
					ring3D(matrices, vc, (0.3f + p * 1.2f) * size, 0.06f * size, c[0], c[1], c[2], (1f - p) * 0.85f * bright);
					ring3D(matrices, glow, (0.25f + p * 1.15f) * size, 0.03f * size, c[0], c[1], c[2], (1f - p) * 0.4f * bright);
				}
			}
			case HEX, SQUARE -> {
				int sides = style == JumpCircleStyle.HEX ? 6 : 4;
				for (int ring = 0; ring < rings; ring++) {
					float p = (phase + ring / (float) rings) % 1f;
					float rad = (0.35f + p * 1.15f) * size;
					float a = (1f - p) * 0.8f * bright;
					for (int i = 0; i < sides; i++) {
						float a0 = i / (float) sides * ((float) Math.PI * 2f) + (style == JumpCircleStyle.SQUARE ? (float) Math.PI / 4f : 0f);
						float a1 = (i + 1) / (float) sides * ((float) Math.PI * 2f) + (style == JumpCircleStyle.SQUARE ? (float) Math.PI / 4f : 0f);
						sheet(matrices, vc, Mth.cos(a0) * rad, -0.03f, Mth.sin(a0) * rad,
							Mth.cos(a1) * rad, 0.03f, Mth.sin(a1) * rad, c[0], c[1], c[2], a);
					}
				}
			}
			case STAR -> {
				for (int ring = 0; ring < rings; ring++) {
					float p = (phase + ring / (float) rings) % 1f;
					float rad = (0.35f + p * 1.1f) * size;
					float a = (1f - p) * 0.85f * bright;
					for (int i = 0; i < 5; i++) {
						float ang = i / 5f * ((float) Math.PI * 2f) + time * 0.05f;
						sheet(matrices, vc, 0, 0, 0, Mth.cos(ang) * rad, 0.04f, Mth.sin(ang) * rad, c[0], c[1], c[2], a);
					}
					ring3D(matrices, glow, rad * 0.55f, 0.025f * size, c[0], c[1], c[2], a * 0.5f);
				}
			}
			case SPIRAL -> {
				for (int i = 0; i < 18; i++) {
					float t = (phase + i / 18f) % 1f;
					float a = t * ((float) Math.PI * 4f) + time * 0.08f * speed;
					float rad = (0.25f + t * 1.1f) * size;
					billboardOrb(matrices, vc, Mth.cos(a) * rad, 0.02f, Mth.sin(a) * rad, 0.07f * size, c[0], c[1], c[2], (1f - t) * bright, time);
				}
			}
			case SPARKS -> {
				for (int ring = 0; ring < rings; ring++) {
					float p = (phase + ring / (float) rings) % 1f;
					float rad = (0.3f + p * 1.2f) * size;
					ring3D(matrices, vc, rad, 0.03f * size, c[0], c[1], c[2], (1f - p) * 0.7f * bright);
					for (int i = 0; i < 8; i++) {
						float a = i / 8f * ((float) Math.PI * 2f) + time * 0.1f;
						billboardOrb(matrices, glow, Mth.cos(a) * rad, 0.04f, Mth.sin(a) * rad, 0.06f * size, c[0], c[1], c[2], (1f - p) * bright, time);
					}
				}
			}
			case HEART -> {
				for (int ring = 0; ring < rings; ring++) {
					float p = (phase + ring / (float) rings) % 1f;
					float s = (0.25f + p * 0.9f) * size;
					float a = (1f - p) * 0.8f * bright;
					drawHeartRing(matrices, vc, s, c[0], c[1], c[2], a);
				}
			}
			case ORBIT -> {
				float rad = 0.75f * size;
				ring3D(matrices, glow, rad, 0.025f * size, c[0], c[1], c[2], 0.45f * bright);
				for (int i = 0; i < 6 + rings; i++) {
					float a = time * 0.15f * speed + i * ((float) Math.PI * 2f / (6 + rings));
					billboardOrb(matrices, vc, Mth.cos(a) * rad, 0.03f, Mth.sin(a) * rad, 0.08f * size, c[0], c[1], c[2], bright, time);
				}
			}
			case WAVE -> {
				for (int ring = 0; ring < rings; ring++) {
					float p = (phase + ring / (float) rings) % 1f;
					float rad = (0.35f + p * 1.15f) * size;
					float a = (1f - p) * 0.75f * bright;
					for (int i = 0; i < 16; i++) {
						float a0 = i / 16f * ((float) Math.PI * 2f);
						float a1 = (i + 1) / 16f * ((float) Math.PI * 2f);
						float y0 = Mth.sin(a0 * 3f + time * 0.2f) * 0.05f * size;
						float y1 = Mth.sin(a1 * 3f + time * 0.2f) * 0.05f * size;
						sheet(matrices, vc, Mth.cos(a0) * rad, y0, Mth.sin(a0) * rad,
							Mth.cos(a1) * rad, y1 + 0.04f, Mth.sin(a1) * rad, c[0], c[1], c[2], a);
					}
				}
			}
			case PRISM -> {
				for (int ring = 0; ring < rings; ring++) {
					float p = (phase + ring / (float) rings) % 1f;
					float rad = (0.3f + p * 1.1f) * size;
					float a = (1f - p) * 0.8f * bright;
					for (int i = 0; i < 8; i++) {
						float ang = i / 8f * ((float) Math.PI * 2f);
						billboardOrb(matrices, vc, Mth.cos(ang) * rad, 0.03f, Mth.sin(ang) * rad, 0.1f * size, c[0], c[1], c[2], a, time);
					}
					ring3D(matrices, glow, rad * 0.7f, 0.03f * size, c[0], c[1], c[2], a * 0.45f);
				}
			}
			default -> { // RIPPLE
				for (int ring = 0; ring < rings; ring++) {
					float p = (phase + ring / (float) rings) % 1f;
					ring3D(matrices, vc, (0.35f + p * 1.15f) * size, 0.04f * size, c[0], c[1], c[2], (1f - p) * 0.8f * bright);
				}
			}
		}
		matrices.popPose();
	}

	private static void drawHeartRing(PoseStack matrices, VertexConsumer vc, float s, float r, float g, float b, float a) {
		for (int i = 0; i < 14; i++) {
			float t0 = i / 14f * ((float) Math.PI * 2f);
			float t1 = (i + 1) / 14f * ((float) Math.PI * 2f);
			float x0 = heartX(t0) * s, z0 = heartY(t0) * s;
			float x1 = heartX(t1) * s, z1 = heartY(t1) * s;
			sheet(matrices, vc, x0, -0.02f, z0, x1, 0.04f, z1, r, g, b, a);
		}
	}

	private static float heartX(float t) {
		return 0.16f * Mth.sin(t) * Mth.sin(t) * Mth.sin(t);
	}

	private static float heartY(float t) {
		return 0.13f * Mth.cos(t) - 0.05f * Mth.cos(2f * t) - 0.02f * Mth.cos(3f * t) - 0.01f * Mth.cos(4f * t);
	}



	/* ===================== PRIMITIVES (always double-sided) ===================== */

	/** XY/diagonal sheet, both faces */
	private static void sheet(PoseStack matrices, VertexConsumer vc,
		float x0, float y0, float z0, float x1, float y1, float z1,
		float r, float g, float b, float a) {
		quadOnce(matrices, vc, x0, y0, z0, x1, y0, z1, x1, y1, z1, x0, y1, z0, r, g, b, a);
		quadOnce(matrices, vc, x0, y1, z0, x1, y1, z1, x1, y0, z1, x0, y0, z0, r, g, b, a);
	}

	/** YZ plane sheet (side view), both faces */
	private static void sheetYZ(PoseStack matrices, VertexConsumer vc,
		float x0, float y0, float z0, float x1, float y1, float z1,
		float r, float g, float b, float a) {
		quadOnce(matrices, vc, x0, y0, z0, x0, y0, z1, x1, y1, z1, x1, y1, z0, r, g, b, a);
		quadOnce(matrices, vc, x1, y1, z0, x1, y1, z1, x0, y0, z1, x0, y0, z0, r, g, b, a);
	}

	/** Solid octahedron crystal — real 3D volume, not a flat diamond billboard. */
	private static void billboardOrb(PoseStack matrices, VertexConsumer vc,
		float x, float y, float z, float size, float r, float g, float b, float a, float time) {
		matrices.pushPose();
		matrices.translate(x, y, z);
		if (time != 0f) matrices.mulPose(Axis.YP.rotationDegrees(time * 18f));
		float s = size;
		float h = s * 1.15f;
		float aa = Mth.clamp(a, 0f, 1f);
		float er = Math.min(1f, r + 0.15f), eg = Math.min(1f, g + 0.15f), eb = Math.min(1f, b + 0.12f);
		float sr = r * 0.65f, sg = g * 0.65f, sb = b * 0.7f;
		// top pyramid (4 faces)
		tri(matrices, vc, 0, h, 0,  s, 0, 0,  0, 0, s, er, eg, eb, aa);
		tri(matrices, vc, 0, h, 0,  0, 0, s, -s, 0, 0, r, g, b, aa);
		tri(matrices, vc, 0, h, 0, -s, 0, 0,  0, 0,-s, sr, sg, sb, aa);
		tri(matrices, vc, 0, h, 0,  0, 0,-s,  s, 0, 0, r, g, b, aa);
		// bottom pyramid
		tri(matrices, vc, 0,-h, 0,  0, 0, s,  s, 0, 0, r, g, b, aa);
		tri(matrices, vc, 0,-h, 0, -s, 0, 0,  0, 0, s, sr, sg, sb, aa);
		tri(matrices, vc, 0,-h, 0,  0, 0,-s, -s, 0, 0, r, g, b, aa);
		tri(matrices, vc, 0,-h, 0,  s, 0, 0,  0, 0,-s, er, eg, eb, aa);
		// core glow box
		MeshPrims.box(matrices, vc, LightTexture.FULL_BRIGHT, -s * 0.28f, -s * 0.28f, -s * 0.28f, s * 0.28f, s * 0.28f, s * 0.28f, er, eg, eb, aa * 0.85f);
		matrices.popPose();
	}

	private static void tri(PoseStack matrices, VertexConsumer vc,
		float x0, float y0, float z0, float x1, float y1, float z1, float x2, float y2, float z2,
		float r, float g, float b, float a) {
		PoseStack.Pose pose = matrices.last();
		Matrix4f m = pose.pose();
		int light = LightTexture.FULL_BRIGHT;
		int overlay = OverlayTexture.NO_OVERLAY;
		vc.addVertex(m, x0, y0, z0).setColor(r, g, b, a).setUv(0.5f, 0f).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
		vc.addVertex(m, x1, y1, z1).setColor(r, g, b, a).setUv(0f, 1f).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
		vc.addVertex(m, x2, y2, z2).setColor(r, g, b, a).setUv(1f, 1f).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
		// degenerate 4th for quad consumer pipelines
		vc.addVertex(m, x0, y0, z0).setColor(r, g, b, a).setUv(0.5f, 0f).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
	}

	private static void quadOnce(PoseStack matrices, VertexConsumer vc,
		float x0, float y0, float z0,
		float x1, float y1, float z1,
		float x2, float y2, float z2,
		float x3, float y3, float z3,
		float r, float g, float b, float a) {
		PoseStack.Pose pose = matrices.last();
		Matrix4f m = pose.pose();
		int light = LightTexture.FULL_BRIGHT;
		int overlay = OverlayTexture.NO_OVERLAY;
		float aa = Mth.clamp(a, 0f, 1f);
		vc.addVertex(m, x0, y0, z0).setColor(r, g, b, aa).setUv(0f, 1f).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
		vc.addVertex(m, x1, y1, z1).setColor(r, g, b, aa).setUv(1f, 1f).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
		vc.addVertex(m, x2, y2, z2).setColor(r, g, b, aa).setUv(1f, 0f).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
		vc.addVertex(m, x3, y3, z3).setColor(r, g, b, aa).setUv(0f, 0f).setOverlay(overlay).setLight(light).setNormal(pose, 0, 1, 0);
	}
}
