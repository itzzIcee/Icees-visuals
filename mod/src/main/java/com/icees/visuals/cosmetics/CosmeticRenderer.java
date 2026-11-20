package com.icees.visuals.cosmetics;

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
 * First/third-person sakura cosmetics: wings, halo, cape, aura, jump circles, knife preview.
 */
public final class CosmeticRenderer {
	private CosmeticRenderer() {
	}

	public static void render(WorldRenderContext context, ClientPlayerEntity player) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.options.getPerspective().isFirstPerson() && player == client.player) {
			renderFirstPersonKnife(context, player);
			return;
		}

		MatrixStack matrices = context.matrixStack();
		Vec3d camera = context.camera().getPos();
		float tickDelta = context.tickCounter().getTickDelta(false);
		float time = player.age + tickDelta;

		double x = MathHelper.lerp(tickDelta, player.lastRenderX, player.getX()) - camera.x;
		double y = MathHelper.lerp(tickDelta, player.lastRenderY, player.getY()) - camera.y;
		double z = MathHelper.lerp(tickDelta, player.lastRenderZ, player.getZ()) - camera.z;
		float bodyYaw = MathHelper.lerpAngleDegrees(tickDelta, player.prevBodyYaw, player.bodyYaw);

		matrices.push();
		matrices.translate(x, y, z);
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-bodyYaw));

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.depthMask(false);
		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
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
		if (IceesConfig.jumpCircles && (!player.isOnGround() || player.age % 40 < 12)) {
			drawJumpCircles(matrices, time);
		}

		RenderSystem.enableCull();
		RenderSystem.depthMask(true);
		RenderSystem.disableBlend();
		matrices.pop();
	}

	private static void renderFirstPersonKnife(WorldRenderContext context, ClientPlayerEntity player) {
		if (!IceesConfig.knife) {
			return;
		}
		MatrixStack matrices = context.matrixStack();
		float tickDelta = context.tickCounter().getTickDelta(false);
		float time = player.age + tickDelta;

		matrices.push();
		matrices.translate(0.65, -0.35, -0.55);
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(70));
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-10 + MathHelper.sin(time * 0.25f) * 8));
		float spin = switch (IceesConfig.knifeStyle) {
			case BUTTERFLY, KARAMBIT, CURVED -> time * 8f;
			case BAYONET, LONG_BLADE -> MathHelper.sin(time * 0.2f) * 25f;
			default -> MathHelper.sin(time * 0.35f) * 40f;
		};
		matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(spin));

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
		RenderSystem.disableDepthTest();
		drawKnife(matrices.peek().getPositionMatrix(), IceesConfig.knifeStyle);
		RenderSystem.enableDepthTest();
		RenderSystem.disableBlend();
		matrices.pop();
	}

	private static void drawWings(MatrixStack matrices, float time) {
		WingStyle style = IceesConfig.wingStyle;
		float beat = MathHelper.sin(time * 0.25f) * 0.35f;
		for (int side : new int[]{-1, 1}) {
			matrices.push();
			matrices.translate(side * 0.15, 1.1, 0.05);
			matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(side * (-35 + beat * 20)));
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(side * 18));
			Matrix4f m = matrices.peek().getPositionMatrix();
			Tessellator tess = Tessellator.getInstance();
			BufferBuilder buf = tess.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
			float r = style.r, g = style.g, b = style.b;
			buf.vertex(m, 0, 0, 0).color(r, g, b, 0.85f);
			buf.vertex(m, side * 1.35f, 0.55f, -0.2f).color(r, g, b, 0.15f);
			buf.vertex(m, side * 0.35f, 1.15f, 0.1f).color(1f, 0.9f, 0.95f, 0.55f);
			buf.vertex(m, 0, 0, 0).color(r, g, b, 0.85f);
			buf.vertex(m, side * 0.35f, -0.2f, 0.05f).color(r, g, b, 0.2f);
			buf.vertex(m, side * 1.2f, 0.15f, -0.15f).color(r, g, b, 0.1f);
			BufferRenderer.drawWithGlobalProgram(buf.end());
			matrices.pop();
		}
	}

	private static void drawHalo(MatrixStack matrices, float time) {
		matrices.push();
		matrices.translate(0, 2.15, 0);
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(70));
		Matrix4f m = matrices.peek().getPositionMatrix();
		HaloStyle style = IceesConfig.haloStyle;
		Tessellator tess = Tessellator.getInstance();

		if (style == HaloStyle.ORBIT) {
			BufferBuilder buf = tess.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
			for (int i = 0; i < 3; i++) {
				float a = time * 0.12f + i * ((float) Math.PI * 2 / 3);
				float x = MathHelper.cos(a) * 0.38f;
				float y = MathHelper.sin(a) * 0.38f;
				buf.vertex(m, x, y, 0).color(1f, 0.75f, 0.85f, 0.9f);
				buf.vertex(m, x * 0.85f, y * 0.85f, 0).color(1f, 0.55f, 0.75f, 0.2f);
			}
			BufferRenderer.drawWithGlobalProgram(buf.end());
		}

		BufferBuilder ring = tess.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
		float pulse = style == HaloStyle.PULSE ? 1f + MathHelper.sin(time * 0.3f) * 0.08f : 1f;
		int points = style == HaloStyle.CROWN ? 6 : 40;
		for (int i = 0; i <= points; i++) {
			float a = i / (float) points * ((float) Math.PI * 2);
			float radius = 0.34f * pulse * (style == HaloStyle.CROWN && i % 2 == 0 ? 1.25f : 1f);
			float x = MathHelper.cos(a) * radius;
			float y = MathHelper.sin(a) * radius;
			ring.vertex(m, x, y, 0).color(1f, 0.72f, 0.84f, 0.95f);
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
		Tessellator tess = Tessellator.getInstance();
		BufferBuilder buf = tess.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
		for (int i = 0; i <= 8; i++) {
			float t = i / 8f;
			float y = -t * 1.35f;
			float w = 0.42f + t * 0.12f;
			float xOff = sway * t * 0.4f;
			float r = MathHelper.lerp(t, style.r2, style.r1);
			float g = MathHelper.lerp(t, style.g2, style.g1);
			float b = MathHelper.lerp(t, style.b2, style.b1);
			buf.vertex(m, -w + xOff, y, t * 0.15f).color(r, g, b, 0.85f);
			buf.vertex(m, w + xOff, y, t * 0.15f).color(r, g, b, 0.85f);
		}
		BufferRenderer.drawWithGlobalProgram(buf.end());
		matrices.pop();
	}

	private static void drawAura(MatrixStack matrices, float time) {
		Matrix4f m = matrices.peek().getPositionMatrix();
		Tessellator tess = Tessellator.getInstance();
		BufferBuilder buf = tess.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
		for (int i = 0; i <= 48; i++) {
			float a = i / 48f * ((float) Math.PI * 2) + time * 0.05f;
			float radius = 0.75f + MathHelper.sin(time * 0.2f + i) * 0.05f;
			buf.vertex(m, MathHelper.cos(a) * radius, 1.0f + MathHelper.sin(a * 2) * 0.05f, MathHelper.sin(a) * radius)
				.color(1f, 0.65f, 0.8f, 0.35f);
		}
		BufferRenderer.drawWithGlobalProgram(buf.end());
	}

	private static void drawJumpCircles(MatrixStack matrices, float time) {
		Matrix4f m = matrices.peek().getPositionMatrix();
		Tessellator tess = Tessellator.getInstance();
		float phase = (time % 30f) / 30f;
		for (int ring = 0; ring < 3; ring++) {
			float p = (phase + ring / 3f) % 1f;
			BufferBuilder buf = tess.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
			float radius = 0.3f + p * 1.2f;
			for (int i = 0; i <= 32; i++) {
				float a = i / 32f * ((float) Math.PI * 2);
				buf.vertex(m, MathHelper.cos(a) * radius, 0.05f, MathHelper.sin(a) * radius)
					.color(1f, 0.75f, 0.88f, 1f - p);
			}
			BufferRenderer.drawWithGlobalProgram(buf.end());
		}
	}

	private static void drawKnife(Matrix4f m, KnifeStyle style) {
		Tessellator tess = Tessellator.getInstance();
		BufferBuilder handle = tess.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
		handle.vertex(m, -0.03f, 0f, 0).color(0.35f, 0.12f, 0.22f, 1f);
		handle.vertex(m, 0.03f, 0f, 0).color(0.35f, 0.12f, 0.22f, 1f);
		handle.vertex(m, -0.03f, -0.28f, 0).color(0.25f, 0.08f, 0.16f, 1f);
		handle.vertex(m, 0.03f, -0.28f, 0).color(0.25f, 0.08f, 0.16f, 1f);
		BufferRenderer.drawWithGlobalProgram(handle.end());

		BufferBuilder blade = tess.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
		if (style == KnifeStyle.KARAMBIT || style == KnifeStyle.CURVED) {
			blade.vertex(m, 0f, 0f, 0).color(1f, 0.85f, 0.92f, 1f);
			blade.vertex(m, 0.18f, 0.22f, 0).color(1f, 0.7f, 0.82f, 1f);
			blade.vertex(m, 0.05f, 0.45f, 0).color(0.95f, 0.55f, 0.72f, 0.9f);
		} else {
			blade.vertex(m, -0.025f, 0f, 0).color(1f, 0.88f, 0.94f, 1f);
			blade.vertex(m, 0.025f, 0f, 0).color(1f, 0.88f, 0.94f, 1f);
			blade.vertex(m, 0f, 0.55f, 0).color(1f, 0.65f, 0.8f, 1f);
		}
		BufferRenderer.drawWithGlobalProgram(blade.end());
	}
}
