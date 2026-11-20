package com.icees.visuals.sky;

import com.icees.visuals.config.IceesConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

/**
 * Safe procedural sky overlay — sakura palette, resets GL state after drawing.
 */
public final class SakuraSkyRenderer {
	private SakuraSkyRenderer() {
	}

	public static void render(MatrixStack matrices, float tickDelta) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.world == null) {
			return;
		}

		SkyStyle style = IceesConfig.skyStyle;
		float time = client.world.getTime() + tickDelta;

		matrices.push();
		try {
			RenderSystem.depthMask(false);
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
			RenderSystem.setShader(GameRenderer::getPositionColorProgram);

			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(time * 0.12f));
			Matrix4f matrix = matrices.peek().getPositionMatrix();

			drawDome(matrix, style);
			drawSoftBand(matrix, style, time);
			if (style == SkyStyle.GALAXY || style == SkyStyle.SAKURA) {
				drawPlanet(matrix, style);
			}
			if (IceesConfig.petals || style == SkyStyle.SAKURA) {
				drawPetals(matrix, time);
			}
		} finally {
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
			RenderSystem.depthMask(true);
			RenderSystem.disableBlend();
			RenderSystem.defaultBlendFunc();
			matrices.pop();
		}
	}

	private static void drawDome(Matrix4f matrix, SkyStyle style) {
		BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
		buffer.vertex(matrix, 0f, 80f, 0f).color(style.r * 0.22f, style.g * 0.16f, style.b * 0.28f, 0.55f);
		for (int i = 0; i <= 32; i++) {
			float a = (float) (i * Math.PI * 2 / 32);
			float x = (float) Math.cos(a) * 80f;
			float z = (float) Math.sin(a) * 80f;
			buffer.vertex(matrix, x, -8f, z).color(style.r, style.g, style.b, 0.35f);
		}
		BufferRenderer.drawWithGlobalProgram(buffer.end());
	}

	private static void drawSoftBand(Matrix4f matrix, SkyStyle style, float time) {
		BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
		for (int i = 0; i <= 40; i++) {
			float t = i / 40f;
			float a = t * ((float) Math.PI * 2f) + time * 0.01f;
			float y = 18f + (float) Math.sin(a * 2f + time * 0.03f) * 6f;
			float radius = 55f;
			float x = (float) Math.cos(a) * radius;
			float z = (float) Math.sin(a) * radius;
			float alpha = 0.22f;
			buffer.vertex(matrix, x, y, z).color(style.r, style.g, style.b, alpha);
			buffer.vertex(matrix, x * 0.85f, y - 10f, z * 0.85f).color(style.r, style.g, style.b, 0f);
		}
		BufferRenderer.drawWithGlobalProgram(buffer.end());
	}

	private static void drawPlanet(Matrix4f matrix, SkyStyle style) {
		float cx = 26f;
		float cy = 22f;
		float cz = -8f;
		float radius = 9f;
		BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
		buffer.vertex(matrix, cx, cy, cz).color(0.12f, 0.06f, 0.12f, 0.7f);
		for (int i = 0; i <= 24; i++) {
			float a = (float) (i * Math.PI * 2 / 24);
			buffer.vertex(matrix, cx + (float) Math.cos(a) * radius, cy + (float) Math.sin(a) * radius * 0.85f, cz)
				.color(style.r * 0.55f, style.g * 0.35f, style.b * 0.6f, 0.65f);
		}
		BufferRenderer.drawWithGlobalProgram(buffer.end());
	}

	private static void drawPetals(Matrix4f matrix, float time) {
		BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
		for (int i = 0; i < 24; i++) {
			float seed = i * 17.13f;
			float x = ((seed * 13f) % 80f) - 40f;
			float z = ((seed * 29f) % 80f) - 40f;
			float y = 40f - ((time * 0.3f + seed) % 60f);
			float s = 0.55f + (i % 4) * 0.12f;
			float a = 0.28f;
			buffer.vertex(matrix, x, y, z).color(1f, 0.72f, 0.82f, a);
			buffer.vertex(matrix, x + s, y - s * 0.35f, z).color(1f, 0.55f, 0.72f, a * 0.5f);
			buffer.vertex(matrix, x - s * 0.25f, y - s, z).color(1f, 0.85f, 0.9f, 0f);
		}
		BufferRenderer.drawWithGlobalProgram(buffer.end());
	}
}
