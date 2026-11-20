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

	private static float[] tintWings(float r, float g, float b) {
		return IceesConfig.color(IceesConfig.wingTint, r, g, b);
	}

	private static float[] tintHalo(float r, float g, float b) {
		return IceesConfig.color(IceesConfig.haloTint, r, g, b);
	}

	private static float[] tintCape(float r, float g, float b) {
		return IceesConfig.color(IceesConfig.capeTint, r, g, b);
	}

	private static float[] tintAura(float r, float g, float b) {
		return IceesConfig.color(IceesConfig.auraTint, r, g, b);
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
			if (IceesConfig.hat) drawHat(matrices, consumers, time, bright, thick);
			if (IceesConfig.aura) drawAura(matrices, consumers, time, bright, thick, camRelYaw, camPitch);
			if (IceesConfig.jumpCircles && !player.onGround()) drawJumpCircles(matrices, consumers, time, bright, thick);
			if (IceesConfig.knifeActive(player)) drawThirdPersonKnife(matrices, consumers, time, bright, thick, camRelYaw);

			matrices.popPose();
		} catch (Throwable t) {
			disabledForSession = true;
			IceesVisualsClient.LOGGER.error("Cosmetics crashed — disabled for this session", t);
		}
	}

	/* ===================== WINGS — distinct models ===================== */

	private static void drawWings(PoseStack matrices, MultiBufferSource buffers, float time, float bright, float thick) {
		WingStyle style = IceesConfig.wingStyle;
		float[] col = tintWings(style.r, style.g, style.b);
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
				case DRAGONFLY -> wingDragonfly(matrices, soft, hard, side, bright, thick, time);
				case FAIRY -> wingFairy(matrices, soft, hard, side, bright, thick, time);
				case BAT -> wingBat(matrices, soft, hard, side, bright, thick, time);
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

	/** Teal translucent insect / dragonfly panes with vein ridges (ref style). */
	private static void wingDragonfly(PoseStack m, VertexConsumer soft, VertexConsumer hard, int side, float bright, float thick, float time) {
		float s = side;
		float flutter = Mth.sin(time * 0.55f + side) * 8f;
		m.mulPose(Axis.ZP.rotationDegrees(s * flutter * 0.35f));
		// Four long panes per side (upper + lower pairs)
		float[][] panes = {
			{0.05f, 0.35f, 1.55f, 0.95f, -0.05f},
			{0.15f, 0.05f, 1.65f, 0.55f, -0.12f},
			{0.08f, -0.15f, 1.35f, 0.25f, -0.08f},
			{0.20f, -0.35f, 1.25f, -0.05f, -0.15f}
		};
		for (float[] p : panes) {
			float x0 = s * p[0] * thick, y0 = p[1] * thick, x1 = s * p[2] * thick, y1 = p[3] * thick, z = p[4];
			sheet(m, soft, x0, y0, z, x1, y1, z - 0.04f, CR, CG, CB, 0.42f * bright);
			sheet(m, hard, x0, y0, z + 0.01f, x1, y1, z - 0.02f, CR * 0.85f, CG * 0.95f, CB, 0.75f * bright);
			// dark vein edges
			sheet(m, hard, x0, y0, z, x0 + s * 0.04f * thick, y1, z - 0.03f, 0.05f, 0.12f, 0.14f, 0.85f * bright);
			sheet(m, hard, x0, (y0 + y1) * 0.5f - 0.02f, z, x1, (y0 + y1) * 0.5f + 0.02f, z - 0.03f, 0.08f, 0.18f, 0.2f, 0.55f * bright);
			sheetYZ(m, hard, x1, y0, z - 0.02f, x1, y1, z + 0.02f, 0.05f, 0.1f, 0.12f, 0.5f * bright);
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

	/* ===================== HATS — straw (natural) + china cone ===================== */

	private static void drawHat(PoseStack matrices, MultiBufferSource buffers, float time, float bright, float thick) {
		HatStyle style = IceesConfig.hatStyle;
		VertexConsumer vc = buffers.getBuffer(RenderTypes.eyes(TEX_CAPE));
		matrices.pushPose();
		matrices.translate(0, 1.72f + 0.04f * thick, 0);
		float s = 0.85f + thick * 0.2f;

		// Original straw colours only — never HSV
		switch (style) {
			case STRAW_RED -> drawVoxelStrawHat(matrices, vc, s, bright, 0.86f, 0.12f, 0.12f);
			case STRAW_BROWN -> drawVoxelStrawHat(matrices, vc, s, bright, 0.42f, 0.26f, 0.14f);
		}
		matrices.popPose();
	}

	/**
	 * Blockbench / DualPrac-style voxel straw hat — stepped brim, box crown, band.
	 * Natural straw colours only (no HSV).
	 */
	private static void drawVoxelStrawHat(PoseStack m, VertexConsumer vc, float s, float bright,
		float bandR, float bandG, float bandB) {
		float u = 0.0625f * s; // 1px in player-model space
		float a = Math.min(1f, 0.98f * bright);
		float s0r = 0.93f, s0g = 0.80f, s0b = 0.48f; // light straw
		float s1r = 0.82f, s1g = 0.66f, s1b = 0.34f; // mid
		float s2r = 0.70f, s2g = 0.54f, s2b = 0.26f; // dark

		// Stepped circular brim (voxel ring masks)
		int[][] brim = {
			// dx,dz half-extent in px — each ring from outside in
			{8, 8}, {9, 7}, {9, 6}, {8, 5}, {7, 4}
		};
		// Filled stepped disk: for each cell in [-9..9]^2 keep if approx circle
		for (int ix = -9; ix <= 9; ix++) {
			for (int iz = -9; iz <= 9; iz++) {
				int ax = Math.abs(ix), az = Math.abs(iz);
				boolean on = (ax <= 8 && az <= 8) || (ax <= 9 && az <= 6) || (ax <= 7 && az <= 9) || (ax <= 6 && az <= 9);
				// punch jagged outer rim like the references
				if (ax + az >= 16) on = false;
				if (ax == 9 && az == 5) on = true;
				if (ax == 5 && az == 9) on = true;
				if (ax == 8 && az == 7) on = true;
				if (ax == 7 && az == 8) on = true;
				// hole under crown
				if (ax <= 3 && az <= 3) on = false;
				if (!on) continue;
				float x0 = ix * u, z0 = iz * u;
				float x1 = x0 + u, z1 = z0 + u;
				boolean weave = ((ix + iz) & 1) == 0;
				float rr = weave ? s0r : s1r, gg = weave ? s0g : s1g, bb = weave ? s0b : s1b;
				// top
				KnifeModels.box(m, vc, LightTexture.FULL_BRIGHT, x0, 0, z0, x1, u * 0.9f, z1, rr, gg, bb, a);
				// underside
				KnifeModels.box(m, vc, LightTexture.FULL_BRIGHT, x0, -u * 0.35f, z0, x1, 0, z1, s2r, s2g, s2b, a * 0.95f);
			}
		}

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
					KnifeModels.box(m, vc, LightTexture.FULL_BRIGHT,
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
				KnifeModels.box(m, vc, LightTexture.FULL_BRIGHT,
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
			KnifeModels.box(m, vc, LightTexture.FULL_BRIGHT,
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
		float bambooR = 0.78f, bambooG = 0.68f, bambooB = 0.42f;
		float tipR = 0.55f, tipG = 0.45f, tipB = 0.28f;
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

	private static void drawHalo(PoseStack matrices, MultiBufferSource buffers, float time, float bright, float thick) {
		HaloStyle style = IceesConfig.haloStyle;
		float[] c = tintHalo(style.r, style.g, style.b); CR = c[0]; CG = c[1]; CB = c[2];
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
			case DOUBLE -> {
				ring3D(matrices, vc, 0.42f * thick, 0.05f * thick, CR, CG, CB, bright);
				ring3D(matrices, glow, 0.28f * thick, 0.04f * thick, CR, CG, CB, bright * 0.7f);
			}
			case CRESCENT -> {
				for (int i = 0; i < 10; i++) {
					float a = -0.6f + i / 9f * 3.6f;
					float x = Mth.cos(a) * 0.4f * thick;
					float z = Mth.sin(a) * 0.4f * thick;
					KnifeModels.box(matrices, vc, LightTexture.FULL_BRIGHT,
						x - 0.05f * thick, -0.04f, z - 0.05f * thick,
						x + 0.05f * thick, 0.04f, z + 0.05f * thick,
						CR, CG, CB, Math.min(1f, bright));
				}
			}
			case HORNS -> {
				for (int side : new int[]{-1, 1}) {
					KnifeModels.box(matrices, vc, LightTexture.FULL_BRIGHT,
						side * 0.22f * thick, 0.05f, -0.06f,
						side * 0.38f * thick, 0.28f * thick, 0.06f,
						CR, CG, CB, bright);
					KnifeModels.box(matrices, vc, LightTexture.FULL_BRIGHT,
						side * 0.32f * thick, 0.25f * thick, -0.05f,
						side * 0.48f * thick, 0.48f * thick, 0.05f,
						CR, CG, CB, bright);
				}
			}
			case DIAMOND -> {
				billboardOrb(matrices, vc, 0, 0.08f, 0, 0.28f * thick, CR, CG, CB, bright, time);
				ring3D(matrices, glow, 0.38f * thick, 0.03f, CR, CG, CB, bright * 0.55f);
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
		float[] c1 = tintCape(style.r1, style.g1, style.b1); CR = c1[0]; CG = c1[1]; CB = c1[2];
		float[] c2 = tintCape(style.r2, style.g2, style.b2); C2R = c2[0]; C2G = c2[1]; C2B = c2[2];
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
		float[] c = tintAura(style.r, style.g, style.b); CR = c[0]; CG = c[1]; CB = c[2];
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
				for (int i = 0; i < style.count; i++) {
					float a = i * ((float) Math.PI * 2f / style.count) + time * style.spin;
					float x = Mth.cos(a) * 0.55f * thick;
					float z = Mth.sin(a) * 0.55f * thick;
					KnifeModels.box(matrices, vc, LightTexture.FULL_BRIGHT,
						x - 0.04f * thick, 0.1f, z - 0.04f * thick,
						x + 0.04f * thick, 2.0f * thick, z + 0.04f * thick,
						CR, CG, CB, aMul * 0.7f);
				}
			}
			case SHIELD -> {
				int n = Math.max(6, style.count);
				for (int i = 0; i < n; i++) {
					float a = i * ((float) Math.PI * 2f / n);
					float x = Mth.cos(a) * 0.85f * thick;
					float z = Mth.sin(a) * 0.85f * thick;
					KnifeModels.box(matrices, vc, LightTexture.FULL_BRIGHT,
						x - 0.08f * thick, 0.4f, z - 0.08f * thick,
						x + 0.08f * thick, 1.6f, z + 0.08f * thick,
						CR, CG, CB, aMul * 0.55f);
				}
			}
			case RIBBON -> {
				for (int i = 0; i < style.count; i++) {
					float t = (time * 0.1f + i / (float) style.count) % 1f;
					float a = t * Mth.TWO_PI * 2f + time * style.spin;
					float radius = 0.35f * thick + t * 0.4f * thick;
					float y = 0.3f + t * 1.5f;
					KnifeModels.box(matrices, vc, LightTexture.FULL_BRIGHT,
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
					KnifeModels.box(matrices, vc, LightTexture.FULL_BRIGHT, x - hs, y - hs, z - hs, x + hs, y + hs, z + hs, CR, CG, CB, aMul);
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
		float[] c = tintAura(1f, 0.75f, 0.88f);
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
		float[] c = IceesConfig.knifeBladeColor(time);
		float ks = IceesConfig.knifeSizeFactor();
		matrices.pushPose();
		matrices.translate(0.38f * ks, 0.72f, -0.1f);
		matrices.mulPose(Axis.YP.rotationDegrees(knifeSpin(style, time)));
		matrices.mulPose(Axis.ZP.rotationDegrees(22f + Mth.sin(time * 0.15f) * 5f));
		matrices.mulPose(Axis.XP.rotationDegrees(-12f));
		matrices.scale(ks * 1.15f, ks * 1.15f, ks * 1.15f);
		VertexConsumer vc = buffers.getBuffer(RenderTypes.eyes(TEX_KNIFE));
		float r = Math.min(1f, c[0] * bright);
		float g = Math.min(1f, c[1] * bright);
		float b = Math.min(1f, c[2] * bright);
		KnifeModels.draw(matrices, vc, LightTexture.FULL_BRIGHT, style, r, g, b, time);
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
		KnifeModels.box(matrices, vc, LightTexture.FULL_BRIGHT, -s * 0.28f, -s * 0.28f, -s * 0.28f, s * 0.28f, s * 0.28f, s * 0.28f, er, eg, eb, aa * 0.85f);
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
