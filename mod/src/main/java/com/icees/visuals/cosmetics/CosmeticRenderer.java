package com.icees.visuals.cosmetics;

import com.icees.visuals.IceesVisualsClient;
import com.icees.visuals.config.IceesConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

/**
 * Third-person sakura cosmetics. Wrapped for crash safety.
 */
public final class CosmeticRenderer {
	private static boolean disabledForSession = false;

	private CosmeticRenderer() {
	}

	public static void render(WorldRenderContext context, ClientPlayerEntity player) {
		if (disabledForSession) {
			return;
		}
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.options.getPerspective().isFirstPerson()) {
			return; // FP knife overlay disabled — was a common crash path with world matrices
		}
		MatrixStack matrices = context.matrixStack();
		if (matrices == null) {
			return;
		}

		try {
			float tickDelta = context.tickCounter().getTickDelta(false);
			float time = player.age + tickDelta;
			Vec3d camera = context.camera().getPos();

			double x = MathHelper.lerp(tickDelta, player.lastRenderX, player.getX()) - camera.x;
			double y = MathHelper.lerp(tickDelta, player.lastRenderY, player.getY()) - camera.y;
			double z = MathHelper.lerp(tickDelta, player.lastRenderZ, player.getZ()) - camera.z;
			float bodyYaw = MathHelper.lerpAngleDegrees(tickDelta, player.prevBodyYaw, player.bodyYaw);

			matrices.push();
			try {
				matrices.translate(x, y, z);
				matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-bodyYaw));

				RenderSystem.enableBlend();
				RenderSystem.defaultBlendFunc();
				RenderSystem.depthMask(false);
				RenderSystem.setShader(GameRenderer::getPositionColorProgram);
				RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
				RenderSystem.disableCull();

				if (IceesConfig.cape) {
					drawCape(matrices, time);
				}
				if (IceesConfig.wings) {
					drawWings(matrices, time);
				}
				if (IceesConfig.halo) {
					drawHalo(matrices, time);
				}
				if (IceesConfig.aura) {
					drawAura(matrices, time);
				}
				if (IceesConfig.jumpCircles && !player.isOnGround()) {
					drawJumpCircles(matrices, time);
				}
			} finally {
				RenderSystem.enableCull();
				RenderSystem.depthMask(true);
				RenderSystem.disableBlend();
				RenderSystem.defaultBlendFunc();
				RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
				matrices.pop();
			}
		} catch (Throwable t) {
			disabledForSession = true;
			IceesConfig.wings = false;
			IceesConfig.halo = false;
			IceesConfig.cape = false;
			IceesConfig.aura = false;
			IceesConfig.jumpCircles = false;
			IceesVisualsClient.LOGGER.error("Cosmetics crashed — disabled for this session. Check logs.", t);
		}
	}

	private static void drawWings(MatrixStack matrices, float time) {
		WingStyle style = IceesConfig.wingStyle;
		float beat = MathHelper.sin(time * 0.25f) * 0.35f;
		for (int side : new int[]{-1, 1}) {
			matrices.push();
			matrices.translate(side * 0.15, 1.1, 0.05);
			matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(side * (-35 + beat * 20)));
			Matrix4f m = matrices.peek().getPositionMatrix();
			BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
			float r = style.r, g = style.g, b = style.b;
			buf.vertex(m, 0, 0, 0).color(r, g, b, 0.8f);
			buf.vertex(m, side * 1.25f, 0.5f, -0.15f).color(r, g, b, 0.12f);
			buf.vertex(m, side * 0.3f, 1.05f, 0.08f).color(1f, 0.9f, 0.95f, 0.5f);
			buf.vertex(m, 0, 0, 0).color(r, g, b, 0.8f);
			buf.vertex(m, side * 0.3f, -0.15f, 0.04f).color(r, g, b, 0.15f);
			buf.vertex(m, side * 1.1f, 0.12f, -0.12f).color(r, g, b, 0.08f);
			BufferRenderer.drawWithGlobalProgram(buf.end());
			matrices.pop();
		}
	}

	private static void drawHalo(MatrixStack matrices, float time) {
		matrices.push();
		matrices.translate(0, 2.1, 0);
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(70));
		Matrix4f m = matrices.peek().getPositionMatrix();
		float pulse = IceesConfig.haloStyle == HaloStyle.PULSE ? 1f + MathHelper.sin(time * 0.3f) * 0.08f : 1f;
		BufferBuilder ring = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
		for (int i = 0; i <= 32; i++) {
			float a = i / 32f * ((float) Math.PI * 2);
			float radius = 0.34f * pulse;
			ring.vertex(m, MathHelper.cos(a) * radius, MathHelper.sin(a) * radius, 0)
				.color(1f, 0.72f, 0.84f, 0.9f);
		}
		BufferRenderer.drawWithGlobalProgram(ring.end());
		matrices.pop();
	}

	private static void drawCape(MatrixStack matrices, float time) {
		CapeStyle style = IceesConfig.capeStyle;
		float sway = MathHelper.sin(time * 0.18f) * 0.25f;
		matrices.push();
		matrices.translate(0, 1.35, 0.18);
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(8 + sway * 20));
		Matrix4f m = matrices.peek().getPositionMatrix();
		BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
		for (int i = 0; i <= 8; i++) {
			float t = i / 8f;
			float y = -t * 1.3f;
			float w = 0.4f + t * 0.1f;
			float xOff = sway * t * 0.35f;
			float r = MathHelper.lerp(t, style.r2, style.r1);
			float g = MathHelper.lerp(t, style.g2, style.g1);
			float b = MathHelper.lerp(t, style.b2, style.b1);
			buf.vertex(m, -w + xOff, y, t * 0.12f).color(r, g, b, 0.8f);
			buf.vertex(m, w + xOff, y, t * 0.12f).color(r, g, b, 0.8f);
		}
		BufferRenderer.drawWithGlobalProgram(buf.end());
		matrices.pop();
	}

	private static void drawAura(MatrixStack matrices, float time) {
		Matrix4f m = matrices.peek().getPositionMatrix();
		BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
		for (int i = 0; i <= 40; i++) {
			float a = i / 40f * ((float) Math.PI * 2) + time * 0.05f;
			float radius = 0.7f;
			buf.vertex(m, MathHelper.cos(a) * radius, 1.0f, MathHelper.sin(a) * radius)
				.color(1f, 0.65f, 0.8f, 0.3f);
		}
		BufferRenderer.drawWithGlobalProgram(buf.end());
	}

	private static void drawJumpCircles(MatrixStack matrices, float time) {
		Matrix4f m = matrices.peek().getPositionMatrix();
		float phase = (time % 30f) / 30f;
		for (int ring = 0; ring < 2; ring++) {
			float p = (phase + ring / 2f) % 1f;
			BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
			float radius = 0.3f + p * 1.0f;
			for (int i = 0; i <= 24; i++) {
				float a = i / 24f * ((float) Math.PI * 2);
				buf.vertex(m, MathHelper.cos(a) * radius, 0.05f, MathHelper.sin(a) * radius)
					.color(1f, 0.75f, 0.88f, 1f - p);
			}
			BufferRenderer.drawWithGlobalProgram(buf.end());
		}
	}
}
