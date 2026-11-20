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
 * Third-person cosmetics with distinct 3D models per style, double-sided so visible from every angle.
 */
public final class CosmeticRenderer {
	private static final Identifier TEX_WING = Identifier.fromNamespaceAndPath("icees-visuals", "textures/cosmetic/wing.png");
	private static final Identifier TEX_WING_SOFT = Identifier.fromNamespaceAndPath("icees-visuals", "textures/cosmetic/wing_soft.png");
	private static final Identifier TEX_HALO = Identifier.fromNamespaceAndPath("icees-visuals", "textures/cosmetic/halo.png");
	private static final Identifier TEX_CAPE = Identifier.fromNamespaceAndPath("icees-visuals", "textures/cosmetic/cape.png");
	private static final Identifier TEX_AURA = Identifier.fromNamespaceAndPath("icees-visuals", "textures/cosmetic/aura.png");
	private static final Identifier TEX_KNIFE = Identifier.fromNamespaceAndPath("icees-visuals", "textures/cosmetic/knife.png");

	private static boolean disabledForSession;
	private static float CR = 1f, CG = 1f, CB = 1f;
	private static float C2R = 1f, C2G = 1f, C2B = 1f;

	private CosmeticRenderer() {}

	private static float[] tint(float r, float g, float b) {
		return IceesConfig.color(r, g, b);
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

			// Camera yaw relative to body — for billboards in local space
			float camRelYaw = camera.yRot() - bodyYaw;
			float camPitch = camera.xRot();

			float bright = IceesConfig.brightnessFactor();
			float thick = IceesConfig.sizeFactor();

			matrices.pushPose();
			matrices.translate(px, py, pz);
			matrices.mulPose(Axis.YP.rotationDegrees(-bodyYaw));

			if (IceesConfig.cape) drawCape(matrices, consumers, time, bright, thick);
			if (IceesConfig.wings) drawWings(matrices, consumers, time, bright, thick);
			if (IceesConfig.halo) drawHalo(matrices, consumers, time, bright, thick);
			if (IceesConfig.aura) drawAura(matrices, consumers, time, bright, thick, camRelYaw, camPitch);
			if (IceesConfig.jumpCircles && !player.onGround()) drawJumpCircles(matrices, consumers, time, bright, thick);
			if (IceesConfig.knife) drawThirdPersonKnife(matrices, consumers, time, bright, thick, camRelYaw);

			matrices.popPose();
		} catch (Throwable t) {
			disabledForSession = true;
			IceesVisualsClient.LOGGER.error("Cosmetics crashed — disabled for this session", t);
		}
	}

	/* ===================== WINGS — distinct models ===================== */

	private static void drawWings(PoseStack matrices, MultiBufferSource buffers, float time, float bright, float thick) {
		WingStyle style = IceesConfig.wingStyle;
		float[] col = tint(style.r, style.g, style.b);
		CR = col[0]; CG = col[1]; CB = col[2];
		float beat = Mth.sin(time * 0.22f) * 0.35f;
		VertexConsumer soft = buffers.getBuffer(RenderTypes.eyes(TEX_WING_SOFT));
		VertexConsumer hard = buffers.getBuffer(RenderTypes.eyes(TEX_WING));

		for (int side : new int[]{-1, 1}) {
			matrices.pushPose();
			matrices.translate(side * 0.16f, 1.05f, 0.1f);
			matrices.mulPose(Axis.ZP.rotationDegrees(side * (-36f + beat * 22f)));
			matrices.mulPose(Axis.YP.rotationDegrees(side * 18f));

			switch (style) {
				case PETAL -> wingPetal(matrices, soft, hard, side, bright, thick, time);
				case ANGEL -> wingAngel(matrices, soft, hard, side, bright, thick, time);
				case DRAGON -> wingDragon(matrices, soft, hard, side, bright, thick, time);
				case CRYSTAL -> wingCrystal(matrices, soft, hard, side, bright, thick, time);
				case PHOENIX -> wingPhoenix(matrices, soft, hard, side, bright, thick, time);
			}
			matrices.popPose();
		}
	}

	/** Soft layered blossom feathers */
	private static void wingPetal(PoseStack m, VertexConsumer soft, VertexConsumer hard, int side, float bright, float thick, float time) {
		float s = side;
		for (int i = 0; i < 4; i++) {
			float t = i / 3f;
			m.pushPose();
			m.translate(0, 0.08f * i, -0.04f * i);
			m.mulPose(Axis.ZP.rotationDegrees(s * (-8f + i * 10f)));
			m.mulPose(Axis.YP.rotationDegrees(s * (6f + i * 4f)));
			float span = (0.7f + t * 0.7f) * thick;
			float tall = (0.55f + t * 0.55f) * thick;
			sheet(m, soft, 0, 0.05f, 0, s * span, tall, -0.08f - t * 0.1f, CR, CG, CB, 0.55f * bright);
			sheet(m, hard, 0, 0, 0.02f, s * span * 0.85f, tall * 0.8f, -0.04f, CR, CG, CB, 0.9f * bright);
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

	/** Angular crystal shards */
	private static void wingCrystal(PoseStack m, VertexConsumer soft, VertexConsumer hard, int side, float bright, float thick, float time) {
		float s = side;
		float[][] shards = {
			{0.15f, 0.2f, 1.0f, 1.1f},
			{0.4f, 0.0f, 1.25f, 0.85f},
			{0.7f, 0.25f, 1.45f, 1.0f},
			{0.35f, 0.55f, 0.95f, 1.35f},
			{0.9f, 0.4f, 1.35f, 0.7f}
		};
		for (float[] sh : shards) {
			m.pushPose();
			m.mulPose(Axis.YP.rotationDegrees(s * (8f + sh[0] * 20f)));
			m.mulPose(Axis.ZP.rotationDegrees(s * (-5f + sh[1] * 15f)));
			sheet(m, hard, s * sh[0] * thick, sh[1] * thick, 0, s * sh[2] * thick, sh[3] * thick, -0.12f, CR, CG, CB, 0.95f * bright);
			sheet(m, soft, s * sh[0] * thick, sh[1] * thick, 0.04f, s * sh[2] * thick, sh[3] * thick, 0.04f, CR, CG, CB, 0.4f * bright);
			// edge visible from side
			sheetYZ(m, hard, s * sh[2] * thick, sh[1] * thick, -0.06f, s * sh[2] * thick, sh[3] * thick, 0.06f, CR, CG, CB, 0.5f * bright);
			m.popPose();
		}
	}

	/** Rising phoenix flame feathers */
	private static void wingPhoenix(PoseStack m, VertexConsumer soft, VertexConsumer hard, int side, float bright, float thick, float time) {
		float s = side;
		float flicker = Mth.sin(time * 0.4f) * 0.08f;
		for (int i = 0; i < 5; i++) {
			float t = i / 4f;
			m.pushPose();
			m.translate(s * 0.08f * i, 0.12f * i + flicker * (1f - t), -0.06f * i);
			m.mulPose(Axis.ZP.rotationDegrees(s * (-20f + i * 8f)));
			float span = (0.55f + t * 0.9f) * thick;
			float tall = (0.5f + t * 0.85f) * thick;
			sheet(m, soft, 0, 0, 0, s * span, tall, -0.1f, CR, Math.max(0.2f, CG * (0.7f + t * 0.3f)), CB * 0.5f, 0.55f * bright);
			sheet(m, hard, 0, 0.05f, 0.03f, s * span * 0.75f, tall * 0.9f, -0.05f, CR, CG, CB, 0.95f * bright);
			m.popPose();
		}
	}

	/* ===================== HALO — distinct models ===================== */

	private static void drawHalo(PoseStack matrices, MultiBufferSource buffers, float time, float bright, float thick) {
		HaloStyle style = IceesConfig.haloStyle;
		float[] c = tint(style.r, style.g, style.b); CR = c[0]; CG = c[1]; CB = c[2];
		VertexConsumer vc = buffers.getBuffer(RenderTypes.eyes(TEX_HALO));
		VertexConsumer glow = buffers.getBuffer(RenderTypes.eyes(TEX_AURA));
		matrices.pushPose();
		matrices.translate(0, 2.05f + 0.08f * thick, 0);

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
		}
		matrices.popPose();
	}

	/** Torus-like ring built from segments — visible from every side */
	private static void ring3D(PoseStack matrices, VertexConsumer vc, float radius, float tube, float r, float g, float b, float bright) {
		int segments = 16;
		for (int i = 0; i < segments; i++) {
			float a0 = i / (float) segments * ((float) Math.PI * 2f);
			float a1 = (i + 1) / (float) segments * ((float) Math.PI * 2f);
			float x0 = Mth.cos(a0) * radius, z0 = Mth.sin(a0) * radius;
			float x1 = Mth.cos(a1) * radius, z1 = Mth.sin(a1) * radius;
			// horizontal band
			sheet(matrices, vc, x0, -tube, z0, x1, tube, z1, r, g, b, 0.95f * bright);
			// vertical lip so front/back/side all see something
			sheetYZ(matrices, vc, x0, -tube, z0 - tube, x0, tube, z0 + tube, r, g, b, 0.55f * bright);
		}
	}

	/* ===================== CAPE — distinct models ===================== */

	private static void drawCape(PoseStack matrices, MultiBufferSource buffers, float time, float bright, float thick) {
		CapeStyle style = IceesConfig.capeStyle;
		float[] c1 = tint(style.r1, style.g1, style.b1); CR = c1[0]; CG = c1[1]; CB = c1[2];
		float[] c2 = tint(style.r2, style.g2, style.b2); C2R = c2[0]; C2G = c2[1]; C2B = c2[2];
		float sway = Mth.sin(time * 0.16f) * 0.2f;
		VertexConsumer vc = buffers.getBuffer(RenderTypes.eyes(TEX_CAPE));
		VertexConsumer glow = buffers.getBuffer(RenderTypes.eyes(TEX_AURA));

		matrices.pushPose();
		matrices.translate(0, 1.35f, -0.22f);
		matrices.mulPose(Axis.XP.rotationDegrees(10f + sway * 16f));

		switch (style) {
			case SAKURA, BLOOM -> capeFlow(matrices, vc, glow, style, bright, thick, sway, 1);
			case AURORA -> {
				for (int i = -1; i <= 1; i++) {
					matrices.pushPose();
					matrices.translate(i * 0.22f * thick, 0, i * 0.02f);
					matrices.mulPose(Axis.ZP.rotationDegrees(i * 8f));
					capeFlow(matrices, vc, glow, style, bright, thick * 0.7f, sway + i * 0.05f, 1);
					matrices.popPose();
				}
			}
			case EMBER -> capeJagged(matrices, vc, glow, style, bright, thick, sway);
			case FROST, PRISM -> capeFacets(matrices, vc, glow, style, bright, thick, sway);
			case NIGHT -> capeFlow(matrices, vc, glow, style, bright, thick * 1.25f, sway, 1);
			case TIDE -> {
				for (int layer = 0; layer < 3; layer++) {
					matrices.pushPose();
					matrices.translate(0, -0.05f * layer, -0.04f * layer);
					matrices.mulPose(Axis.XP.rotationDegrees(layer * 4f + sway * 5f));
					capeFlow(matrices, vc, glow, style, bright * (1f - layer * 0.15f), thick * (1f - layer * 0.1f), sway, 1);
					matrices.popPose();
				}
			}
			case GOLD -> {
				// shoulder plates
				sheet(matrices, vc, -0.45f * thick, 0.05f, 0.05f, -0.15f * thick, 0.25f, -0.05f, CR, CG, CB, bright);
				sheet(matrices, vc, 0.15f * thick, 0.05f, 0.05f, 0.45f * thick, 0.25f, -0.05f, CR, CG, CB, bright);
				capeFlow(matrices, vc, glow, style, bright, thick, sway, 1);
			}
		}
		matrices.popPose();
	}

	private static void capeFlow(PoseStack m, VertexConsumer vc, VertexConsumer glow, CapeStyle style, float bright, float thick, float sway, int panels) {
		float w = 0.4f * thick;
		float len = -1.3f * thick;
		// front face + back face
		sheet(m, vc, -w, 0, 0, w, len, -0.22f, CR, CG, CB, 0.95f * bright);
		// side edges for side-view visibility
		sheetYZ(m, vc, -w, 0, 0.02f, -w, len, -0.22f, C2R, C2G, C2B, 0.7f * bright);
		sheetYZ(m, vc, w, 0, 0.02f, w, len, -0.22f, C2R, C2G, C2B, 0.7f * bright);
		sheet(m, glow, -w * 1.1f, 0.02f, 0.03f, w * 1.1f, len * 1.02f, -0.28f, C2R, C2G, C2B, 0.3f * bright);
	}

	private static void capeJagged(PoseStack m, VertexConsumer vc, VertexConsumer glow, CapeStyle style, float bright, float thick, float sway) {
		float w = 0.42f * thick;
		int tips = 5;
		for (int i = 0; i < tips; i++) {
			float t0 = i / (float) tips;
			float t1 = (i + 1) / (float) tips;
			float x0 = -w + t0 * 2f * w;
			float x1 = -w + t1 * 2f * w;
			float mid = (x0 + x1) * 0.5f;
			float tipY = -1.15f * thick - (i % 2 == 0 ? 0.25f : 0.05f) * thick;
			sheet(m, vc, x0, 0, 0, mid, tipY, -0.2f, CR, CG, CB, bright);
			sheet(m, vc, mid, tipY, -0.2f, x1, 0, 0, C2R, C2G, C2B, bright);
		}
	}

	private static void capeFacets(PoseStack m, VertexConsumer vc, VertexConsumer glow, CapeStyle style, float bright, float thick, float sway) {
		float w = 0.4f * thick;
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 3; col++) {
				float x0 = -w + col * (2f * w / 3f);
				float x1 = x0 + (2f * w / 3f);
				float y0 = -row * (1.2f * thick / 3f);
				float y1 = y0 - (1.2f * thick / 3f);
				float z = -0.05f * row - 0.08f * (col % 2);
				sheet(m, vc, x0, y0, z, x1, y1, z - 0.08f, CR, CG, CB, 0.9f * bright);
				sheetYZ(m, vc, x0, y0, z + 0.03f, x0, y1, z - 0.08f, C2R, C2G, C2B, 0.55f * bright);
			}
		}
	}

	/* ===================== AURA ===================== */

	private static void drawAura(PoseStack matrices, MultiBufferSource buffers, float time, float bright, float thick, float camRelYaw, float camPitch) {
		AuraStyle style = IceesConfig.auraStyle;
		float[] c = tint(style.r, style.g, style.b); CR = c[0]; CG = c[1]; CB = c[2];
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
		VertexConsumer vc = buffers.getBuffer(RenderTypes.eyes(TEX_HALO));
		float[] c = tint(1f, 0.75f, 0.88f);
		float phase = (time % 28f) / 28f;
		for (int ring = 0; ring < 2; ring++) {
			float p = (phase + ring * 0.5f) % 1f;
			matrices.pushPose();
			matrices.translate(0, 0.05f, 0);
			ring3D(matrices, vc, (0.35f + p * 1.1f) * thick, 0.04f, c[0], c[1], c[2], (1f - p) * 0.75f * bright);
			matrices.popPose();
		}
	}

	private static void drawThirdPersonKnife(PoseStack matrices, MultiBufferSource buffers, float time, float bright, float thick, float camRelYaw) {
		KnifeStyle style = IceesConfig.knifeStyle;
		float[] c = tint(1f, 0.9f, 0.95f); CR = c[0]; CG = c[1]; CB = c[2];
		matrices.pushPose();
		matrices.translate(0.42f, 0.85f, -0.05f);
		matrices.mulPose(Axis.YP.rotationDegrees(knifeSpin(style, time)));
		matrices.mulPose(Axis.ZP.rotationDegrees(20f + Mth.sin(time * 0.2f) * 10f));
		float hs = 0.55f * thick;
		VertexConsumer vc = buffers.getBuffer(RenderTypes.eyes(TEX_KNIFE));
		sheet(matrices, vc, -0.08f * thick, -0.05f, 0, 0.08f * thick, hs, 0, CR, CG, CB, Math.min(1f, bright));
		sheetYZ(matrices, vc, 0, -0.05f, -0.06f * thick, 0, hs, 0.06f * thick, CR, CG, CB, 0.85f * bright);
		VertexConsumer glow = buffers.getBuffer(RenderTypes.eyes(TEX_AURA));
		sheet(matrices, glow, -0.18f * thick, -0.08f, -0.02f, 0.18f * thick, hs * 1.1f, -0.02f, CR, CG, CB, 0.5f * bright);
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

	/** Camera-facing orb made of 3 crossed double-sided planes */
	private static void billboardOrb(PoseStack matrices, VertexConsumer vc,
		float x, float y, float z, float size, float r, float g, float b, float a, float time) {
		matrices.pushPose();
		matrices.translate(x, y, z);
		float s = size;
		sheet(matrices, vc, -s, -s, 0, s, s, 0, r, g, b, a);
		sheetYZ(matrices, vc, 0, -s, -s, 0, s, s, r, g, b, a);
		// diagonal cross for fullness
		matrices.mulPose(Axis.YP.rotationDegrees(45));
		sheet(matrices, vc, -s * 0.85f, -s * 0.85f, 0, s * 0.85f, s * 0.85f, 0, r, g, b, a * 0.7f);
		matrices.popPose();
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
